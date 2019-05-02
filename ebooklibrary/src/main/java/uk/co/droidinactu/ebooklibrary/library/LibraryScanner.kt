package uk.co.droidinactu.ebooklibrary.library

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import nl.siegmann.epublib.epub.EpubReader
import org.apache.commons.io.FilenameUtils
import org.jetbrains.anko.doAsync
import uk.co.droidinactu.ebooklibrary.MyDebug
import uk.co.droidinactu.ebooklibrary.R
import uk.co.droidinactu.ebooklibrary.room.EBook
import uk.co.droidinactu.ebooklibrary.room.EBookAuthorLink
import uk.co.droidinactu.ebooklibrary.room.EBookTagLink
import uk.co.droidinactu.ebooklibrary.room.FileType
import uk.co.droidinactu.ebooklibrary.room.Tag
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by aspela on 31/08/16.
 */

class LibraryScanner {

    private var maxFiles = 0
    private var currReadFiles = 0
    private var librootdir = ""
    private var dirnames: MutableList<String>? = ArrayList()
    private val filenames = ArrayList<String>()
    private val pageNum = 0
    private var pdfiumCore: PdfiumCore? = null
    private var executor: ExecutorService? = null
    private val erdr = EpubReader()
    private var prgBrHandler: Handler? = null
    private var libMgr: LibraryManager? = null

    fun scanLibraryForEbooks(ctx: Context, prgBrHandler: Handler, libname: String, rootdir: String) {
//        MyDebug.LOG.debug("LibraryScanner::scanLibraryForEbooks() started")
        try {
            libMgr = LibraryManager()
            libMgr!!.open(ctx)
            this.prgBrHandler = prgBrHandler
            librootdir = rootdir
            findDirs(rootdir)
            findFiles()
            dirnames = null
            readFileData(ctx, libname)

            val intent = Intent(ctx.applicationContext, FileObserverService::class.java)
            intent.putExtra("file_obs_action", "add")
            intent.putExtra("libname", libname)
            intent.putExtra("rootdir", rootdir)
            ctx.applicationContext.startService(intent)
        } catch (pE: SQLException) {
            MyDebug.LOG.error("Exception opening database", pE)
        }
    }

    private fun findDirs(dirname: String) {
//        MyDebug.LOG.debug("LibraryScanner::findDirs() started")
        var f = File(dirname.trim { it <= ' ' })
        val listOfFiles = f.list()

        dirnames!!.add(dirname)
        for (dname in listOfFiles) {
            f = File(dirname + File.separator + dname)
            if (f.isDirectory) {
                dirnames!!.add(dirname + File.separator + dname)
                findDirs(dirname + File.separator + dname)
            }
        }
    }

    private fun findFiles() {
//        MyDebug.LOG.debug("LibraryScanner::findFiles() started")
        for (dir in dirnames!!) {
            var f = File(dir.trim { it <= ' ' })
            val listOfFiles = f.list()

            for (filename in listOfFiles) {
                f = File(dir + File.separator + filename)
                if (f.isFile && (filename.toLowerCase().endsWith("epub") || filename.toLowerCase().endsWith("pdf"))) {
                    filenames.add(dir + File.separator + filename)
                }
            }
        }
    }

    private fun readFileData(ctx: Context, libname: String) {
//        MyDebug.LOG.debug("LibraryScanner::readFileData() started")
        pdfiumCore = PdfiumCore(ctx.applicationContext)
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
        maxFiles = filenames.size
        if (prgBrHandler != null && currReadFiles == 0) {
            val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:$maxFiles")
            completeMessage.sendToTarget()
        }
        for (filename in filenames) {
            try {
                readFile(ctx, libname, filename)
            } catch (e: Exception) {
                MyDebug.LOG.error("Exception reading book file [" + filename + "] " + e.message)
            }
            currReadFiles++
            if (prgBrHandler != null && currReadFiles % 5 == 0) {
                val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:$maxFiles")
                completeMessage.sendToTarget()
            }
        }
    }

    //#region read ebook metadata
    private fun readEpubMetadata(filename: String, f: File, ebk: EBook) {
        //       MyDebug.LOG.debug("LibraryScanner::readEpubMetadata() started")
        ebk.addFileType(FileType.EPUB)
        ebk.bookTitle = (f.name.substring(0, f.name.length - 5))
        ebk.fullFileDirName = ebk.fileDir + File.separator + ebk.bookTitle
        val flen = f.length()
        if (flen < 24500000) {
            try {
                val epubInputStream = FileInputStream(filename)
                val book = erdr.readEpub(epubInputStream)

                ebk.addAuthors(book.metadata.authors)
                ebk.bookTitle = book.title
                val cvrImg = book.coverImage
                if (cvrImg != null) {
                    ebk.setCoverImageFromBitmap(BitmapFactory.decodeStream(cvrImg.inputStream))
                }
                epubInputStream.close()
            } catch (e: IOException) {
                MyDebug.LOG.error("Failed to read epub details from [" + filename + "] " + e.message)
            } catch (npe: NullPointerException) {
                MyDebug.LOG.error("NullPointerException reading epub details from [" + filename + "] " + npe.message)
            }

        } else {
            MyDebug.LOG.debug("Skipping Large epub [filename: " + filename + ", size: " + f.length() + "]")
        }
    }

    private fun readPdfMetadata(filename: String, f: File, ebk: EBook) {
        //       MyDebug.LOG.debug("LibraryScanner::readPdfMetadata() started")
        ebk.addFileType(FileType.PDF)
        ebk.bookTitle = f.name.substring(0, f.name.length - 4)
        ebk.fullFileDirName = ebk.fileDir + File.separator + ebk.bookTitle

        try {
            val fileDesc = ParcelFileDescriptor.open(File(filename), ParcelFileDescriptor.MODE_READ_ONLY)

            val pdfDocument = pdfiumCore!!.newDocument(fileDesc)
            val meta = pdfiumCore!!.getDocumentMeta(pdfDocument)
            pdfiumCore!!.openPage(pdfDocument, 0)
            val width = pdfiumCore!!.getPageWidthPoint(pdfDocument, pageNum)
            val height = pdfiumCore!!.getPageHeightPoint(pdfDocument, pageNum)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore!!.renderPageBitmap(pdfDocument, bmp, pageNum, 0, 0, width, height)
            ebk.setCoverImageFromBitmap(bmp)
            val pdfTitle = meta.title.trim()
            if (pdfTitle.isNotEmpty() && pdfTitle.toLowerCase() != "untitled") {
                ebk.bookTitle = pdfTitle
            }
            //   ebk.addAuthor(Author(meta.getAuthor()))
            pdfiumCore!!.closeDocument(pdfDocument)

        } catch (e: FileNotFoundException) {
            MyDebug.LOG.error("FileNotFoundException reading pdf file [" + filename + "] " + e.message)
        } catch (e1: IOException) {
            MyDebug.LOG.error("IOException reading pdf file [" + filename + "] " + e1.message)
        }

    }

    //   private fun readMobiMetadata( filename:String, f:FileTreeNode , ebk:EBook)
//    {
    //        ebk.addFileType("mobi");
    //        ebk.setBook_title(f.getName().substring(0, f.getName().length() - 4));
    //        ebk.setFull_file_dir_name(ebk.getFile_dir() + FileTreeNode.separator + ebk.getBook_title());
    //        MobiMeta meta = null;
    //        try {
    //            meta = new MobiMeta(f);
    //        } catch (MobiMetaException e) {
    //            BookLibApplication.e(LOG_TAG + "readMobiMetadata() Error: " + e.getMessage());
    //            return;
    //        }
    //        BookLibApplication.e(LOG_TAG + "readMobiMetadata() Fullname: " + meta.getFullName());
    //        List<EXTHRecord> exthList = meta.getEXTHRecords();
    //        String encoding = meta.getCharacterEncoding();
    //        String author = null;
    //        String isbn = null;
    //        String oldasin = null;
    //        for (EXTHRecord rec : exthList) {
    //            switch (rec.getRecordType()) {
    //                case 100: {
    //                    author = StreamUtils.byteArrayToString(rec.getData(), encoding);
    //                    break;
    //                }
    //                case 104: {
    //                    isbn = StreamUtils.byteArrayToString(rec.getData(), encoding);
    //                    break;
    //                }
    //                case 113: {
    //                    oldasin = StreamUtils.byteArrayToString(rec.getData(), encoding);
    //                    break;
    //                }
    //            }
    //        }
    //        if (author != null) {
    //            BookLibApplication.e(LOG_TAG + "readMobiMetadata() Author: " + author);
    //            // ebk.addAuthor(new Author(author));
    //        }
    //        if (isbn != null) {
    //            BookLibApplication.e(LOG_TAG + "readMobiMetadata() ISBN: " + isbn);
    //            ebk.setBook_isbn(isbn);
    //        }
    //        String asin = "";
    //        //asin = oldasin == null ? this.getInput("ASIN: ") : this.getInput("ASIN [" + oldasin + "]: ");
    //        if (asin.length() == 0) {
    //            asin = oldasin != null ? oldasin : null;
    //        }
    //        Iterator<EXTHRecord> it = exthList.iterator();
    //        while (it.hasNext()) {
    //            EXTHRecord rec2 = it.next();
    //            int recType = rec2.getRecordType();
    //            if (recType == 113) {
    //                if (asin == null) continue;
    //                it.remove();
    //                continue;
    //            }
    //            if (recType != 501) continue;
    //            it.remove();
    //        }
    //        if (asin != null) {
    //            exthList.add(new EXTHRecord(113, asin, encoding));
    //        }
    //        exthList.add(new EXTHRecord(501, "EBOK", encoding));
    //        meta.setEXTHRecords();
    ////            try {
    ////                meta.saveToNewFile(new FileTreeNode(this.outDir, f.getName()));
    ////            }
    ////            catch (MobiMetaException e) {
    ////                BookLibApplication.e(LOG_TAG + "readMobiMetadata() Error saving file: " + e.getMessage());
    ////            }
    //  }

    //endregion

    @Synchronized
    private fun addEBookToLibraryStorage(libName: String, ebk: EBook) {
        MyDebug.LOG.debug("LibraryScanner::addEBookToLibraryStorage(" + ebk.bookTitle + ") started")
        var dbEbk = libMgr!!.getBookFromFullFilename(ebk.fullFileDirName)
        if (dbEbk == null) {
            dbEbk = libMgr!!.addBookToLibrary(libName, ebk)
        } else {
            dbEbk.addFileTypes(ebk.filetypes)
            libMgr!!.updateBook(dbEbk)
        }
        for (t in ebk.tags) {
            val ebkTg = EBookTagLink()
            ebkTg.ebookId = dbEbk.getUniqueId()
            ebkTg.tagId = t.getUniqueId()
            libMgr?.addEbookTagLink(ebkTg)
        }
        for (a in ebk.authors) {
            val t = libMgr!!.addAuthor(a)
            val ebkAuth = EBookAuthorLink()
            ebkAuth.ebookId = dbEbk.getUniqueId()
            ebkAuth.authorId = t!!.getUniqueId()
            libMgr?.addEbookAuthorLink(ebkAuth)
        }
    }

    private fun readFile(ctx: Context, libname: String, filename: String) {
        //       MyDebug.LOG.debug("LibraryScanner::readFile($filename) started")
        val f = File(filename)

        val ebk = EBook()
        ebk.inLibraryRowId = libMgr!!.getLibrary(libname).getUniqueId()
        ebk.fileDir = f.parent
        ebk.fileName = filename.substring(ebk.fileDir.length + 1)
        ebk.fileName = FilenameUtils.removeExtension(filename.substring(ebk.fileDir.length + 1))
        ebk.lastModified = f.lastModified()
        ebk.setCoverImageFromBitmap(
            BitmapFactory.decodeResource(
                ctx.resources,
                R.drawable.generic_book_cover
            )
        )

        try {
            val tagStrs =
                f.parent.substring(librootdir.length + 1).split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            var prevBookTag: Tag? = null
            for (s in tagStrs) {
                val t = libMgr!!.addTag(s)
                t.parentTagId = prevBookTag?.getUniqueId()
                doAsync { libMgr!!.updateTag(t) }
                ebk.addTag(t)
                prevBookTag = t
            }
        } catch (oob: StringIndexOutOfBoundsException) {
            ebk.addTag(libMgr!!.addTag(Tag.UNCLASSIFIED))
        }
        MyDebug.LOG.debug("parsing file [filename: " + filename + ", size: " + f.length() + "]")

// FIXME: add when I can work out how
//        try {
//            val metadata = TikaAnalysis.extractMetadata(FileInputStream(f))
//        } catch (e: Throwable) {
//             MyDebug.LOG.error( "Apache Tika exception : ${e.localizedMessage}", e)
//        }

        when {
            filename.toLowerCase().endsWith("epub") -> {
                ebk.addFileType(FileType.EPUB)
                readEpubMetadata(filename, f, ebk)
            }
            filename.toLowerCase().endsWith("pdf") -> { // create a new renderer
                ebk.addFileType(FileType.PDF)
                readPdfMetadata(filename, f, ebk)
            }
            filename.toLowerCase().endsWith("mobi") -> // create a new renderer
                ebk.addFileType(FileType.MOBI)
            //readMobiMetadata(filename, f, ebk);
        }
        addEBookToLibraryStorage(libname, ebk)
    }

}