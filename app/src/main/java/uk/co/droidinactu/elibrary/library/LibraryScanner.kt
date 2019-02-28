package uk.co.droidinactu.elibrary.library


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.ParcelFileDescriptor

import com.shockwave.pdfium.PdfiumCore

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import nl.siegmann.epublib.epub.EpubReader
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import uk.co.droidinactu.booklib.BookLibApplication
import uk.co.droidinactu.booklib.FileObserverService
import uk.co.droidinactu.booklib.MyDebug
import uk.co.droidinactu.booklib.R
import uk.co.droidinactu.elibrary.BookLibApplication
import uk.co.droidinactu.elibrary.R
import uk.co.droidinactu.elibrary.room.Author
import uk.co.droidinactu.elibrary.room.BookTag
import uk.co.droidinactu.elibrary.room.EBook
import uk.co.droidinactu.elibrary.room.FileType

/**
 * Created by aspela on 31/08/16.
 */

class LibraryScanner : AnkoLogger {

    var maxFiles = 0
    var currReadFiles = 0
    private var librootdir = ""
    private var dirnames: MutableList<String>? = ArrayList()
    private val filenames = ArrayList<String>()
    private val pageNum = 0
    private var pdfiumCore: PdfiumCore? = null
    private var executor: ExecutorService? = null
    private val fileReaderThreads = ArrayList<Thread>()
    private val erdr = EpubReader()
    private val strngBldr = StringBuilder()
    private var prgBrHandler: Handler? = null
    private var libMgr: LibraryManager? = null

    fun readFiles(ctx: Context, prgBrHandler: Handler, libname: String, rootdir: String) {
        libMgr = (ctx.applicationContext as BookLibApplication).getLibManager()
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
    }

    private fun findDirs(dirname: String) {
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
        pdfiumCore = PdfiumCore(ctx.applicationContext)
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1)
        maxFiles = filenames.size
        if (prgBrHandler != null && currReadFiles == 0) {
            val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:$maxFiles")
            completeMessage.sendToTarget()
        }
        for (filename in filenames) {
            readFile(ctx, libname, filename)
            currReadFiles++
            if (prgBrHandler != null && currReadFiles % 5 == 0) {
                val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:$maxFiles")
                completeMessage.sendToTarget()
            }
        }
    }

    private fun readEpubMetadata(filename: String, f: File, ebk: EBook) {
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
                error(LOG_TAG + "Failed to read epub details from [" + filename + "] " + e.message)
            } catch (npe: NullPointerException) {
                error(LOG_TAG + "NullPointerException reading epub details from [" + filename + "] " + npe.message)
            }

        } else {
            debug(LOG_TAG + "Skipping Large epub [filename: " + filename + ", size: " + f.length() + "]")
        }
    }

    private fun readPdfMetadata(filename: String, f: File, ebk: EBook) {
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
            val pdfTitle = meta.getTitle().trim()
            if (pdfTitle.length > 0 && pdfTitle.toLowerCase() != "untitled") {
                ebk.bookTitle = pdfTitle
            }
            ebk.addAuthor(Author(meta.getAuthor()))
            pdfiumCore!!.closeDocument(pdfDocument)

        } catch (e: FileNotFoundException) {
            error(LOG_TAG + "FileNotFoundException reading pdf file [" + filename + "] " + e.message)
        } catch (e1: IOException) {
            error(LOG_TAG + "IOException reading pdf file [" + filename + "] " + e1.message)
        }

    }

    //    private void readMobiMetadata(final String filename, final FileTreeNode f, final EBook ebk) {
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
    //    }

    @Synchronized
    private fun addEBookToLibraryStorage(ctx: Context, libName: String, ebk: EBook) {
        libMgr!!.addBookToLibrary(libName, ebk)
        for (t in ebk.bookTags) {
            libMgr!!.addTagToBook(t.getTag(), ebk)
        }
        for (ft in ebk.filetypes) {
            libMgr!!.addFileTypeToBook(ft, ebk)
        }
    }

    fun readFile(ctx: Context, libname: String, filename: String) {
        val f = File(filename)

        var tagStrs = arrayOfNulls<String>(0)
        val ebk = EBook()
        ebk.inLibrary = libname
        ebk.fileDir = f.parent
        ebk.fileName = filename.substring(ebk.fileDir.length() + 1)
        ebk.lastModified = f.lastModified()
        ebk.setCoverImageFromBitmap(BitmapFactory.decodeResource(ctx.resources, R.drawable.generic_book_cover))

        try {
            tagStrs =
                f.parent.substring(librootdir.length + 1).split(File.separator.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            var prevBookTag: BookTag? = null
            for (s in tagStrs) {
                val t = libMgr!!.addTag(s)
                if (prevBookTag != null) {
                    t.setParentTagId(prevBookTag.getId())
                }
                prevBookTag = t
                ebk.addTag(t)
            }
        } catch (oob: StringIndexOutOfBoundsException) {
            ebk.addTag("unclassified")
        }
        debug(LOG_TAG + "parsing file [filename: " + filename + ", size: " + f.length() + "]");

        if (filename.toLowerCase().endsWith("epub")) {
            ebk.addFileType(FileType.EPUB)
            readEpubMetadata(filename, f, ebk)
        } else if (filename.toLowerCase().endsWith("pdf")) { // create a new renderer
            ebk.addFileType(FileType.PDF)
            readPdfMetadata(filename, f, ebk)
        } else if (filename.toLowerCase().endsWith("mobi")) { // create a new renderer
            ebk.addFileType(FileType.MOBI)
            //readMobiMetadata(filename, f, ebk);
        }
        addEBookToLibraryStorage(ctx, libname, ebk)
    }

    fun printPdfInfo(core: PdfiumCore, doc: com.shockwave.pdfium.PdfDocument) {
        val meta = core.getDocumentMeta(doc)
        debug(LOG_TAG + "title = " + meta.getTitle())
        debug(LOG_TAG + "author = " + meta.getAuthor())
        debug(LOG_TAG + "subject = " + meta.getSubject())
        debug(LOG_TAG + "keywords = " + meta.getKeywords())
        debug(LOG_TAG + "creator = " + meta.getCreator())
        debug(LOG_TAG + "producer = " + meta.getProducer())
        debug(LOG_TAG + "creationDate = " + meta.getCreationDate())
        debug(LOG_TAG + "modDate = " + meta.getModDate())
    }

    companion object {
        private val LOG_TAG = LibraryScanner::class.java.simpleName + ":"
    }
}