package uk.co.droidinactu.ebooklib.library

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.ParcelFileDescriptor
import com.shockwave.pdfium.PdfiumCore
import nl.siegmann.epublib.epub.EpubReader
import org.apache.commons.io.FilenameUtils
import uk.co.droidinactu.ebooklib.MyDebug
import uk.co.droidinactu.ebooklib.R
import uk.co.droidinactu.ebooklib.files.FileUtils
import uk.co.droidinactu.ebooklib.room.EBook
import uk.co.droidinactu.ebooklib.room.EBookAuthorLink
import uk.co.droidinactu.ebooklib.room.FileType
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.SQLException
import java.util.function.Consumer

/**
 * Created by aspela on 31/08/16.
 */

class LibraryScanner {

    private var currReadFiles = 0
    private var prgBrHandler: Handler? = null
    private var libMgr: LibraryManager? = null

    fun scanLibraryForEbooks(ctx: Context, prgBrHandler: Handler, libname: String, rootdir: String) {
        MyDebug.LOG.debug("LibraryScanner::scanLibraryForEbooks() started")
        try {
            libMgr = LibraryManager()
            libMgr!!.open(ctx)
            this.prgBrHandler = prgBrHandler

            readAllFileData(ctx, libname, FileUtils.getFileList(rootdir), rootdir)

//            val intent = Intent(ctx.applicationContext, FileObserverService::class.java)
//            intent.putExtra("file_obs_action", "add")
//            intent.putExtra("libname", libname)
//            intent.putExtra("rootdir", rootdir)
//            ctx.applicationContext.startService(intent)
        } catch (pE: SQLException) {
            MyDebug.LOG.error("Exception opening database", pE)
        }
    }

    private fun readAllFileData(ctx: Context, libname: String, files: List<String>, rootdir: String) {
        files.size
        if (prgBrHandler != null && currReadFiles == 0) {
            val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:${files.size}")
            completeMessage.sendToTarget()
        }
        files.forEach(Consumer<String> { readFileData(ctx, libname, File(it), rootdir, files.size) })
    }

    private fun readFileData(ctx: Context, libname: String, file: File, rootdir: String, maxFiles: Int) {
//        MyDebug.LOG.debug("LibraryScanner::readFileData() started")
        try {
            readFileMetaData(ctx, libname, file, rootdir)
        } catch (e: Exception) {
            MyDebug.LOG.error("Exception reading book file [" + file.name + "] " + e.message)
        }
        currReadFiles++
        if (prgBrHandler != null && currReadFiles % 5 == 0) {
            val completeMessage = prgBrHandler!!.obtainMessage(64, "$libname:$currReadFiles:$maxFiles")
            completeMessage.sendToTarget()
        }
    }

    private fun readFileMetaData(ctx: Context, libname: String, file: File, rootdir: String) {
        //       MyDebug.LOG.debug("LibraryScanner::readFile($file.absolutePath) started")

        val ebk = EBook()
        ebk.inLibraryRowId = libMgr!!.getLibrary(libname).getUniqueId()
        ebk.fileDir = file.parent
        ebk.fileName = file.absolutePath.substring(ebk.fileDir.length + 1)
        ebk.fileName = FilenameUtils.removeExtension(file.absolutePath.substring(ebk.fileDir.length + 1))
        ebk.lastModified = file.lastModified()
        ebk.setCoverImageFromBitmap(
            BitmapFactory.decodeResource(
                ctx.resources,
                R.drawable.generic_book_cover
            )
        )

        try {
            val prntDir = file.parent.substring(rootdir.length + 1)
            val prntDirPath: Path = Paths.get(prntDir)
            for (p in prntDirPath) {
                var tagStr = p.toString().trim().replace("\u0027", "'")
                ebk.addTag(tagStr)
            }
        } catch (oob: StringIndexOutOfBoundsException) {
            ebk.addTag(EBook.TAG_UNCLASSIFIED)
        }
        MyDebug.LOG.debug("parsing file [filename: " + file.name + ", size: " + file.length() + "]")

        when {
            file.name.toLowerCase().endsWith("epub") -> {
                ebk.addFileType(FileType.EPUB)
                readEpubMetadata(file, ebk)
            }
            file.name.toLowerCase().endsWith("pdf") -> { // create a new renderer
                ebk.addFileType(FileType.PDF)
                readPdfMetadata(ctx, file, ebk)
            }
            file.name.toLowerCase().endsWith("mobi") -> // create a new renderer
                ebk.addFileType(FileType.MOBI)
            //readMobiMetadata(file, ebk);
        }
        // addMetadataToEbook(ebk, file)
        addEBookToLibraryStorage(libname, ebk)
    }

    //#region read ebook metadata
//    val parser = AutoDetectParser()
//    val handler = BodyContentHandler(-1)
//    val context = ParseContext()
//    var metadata = Metadata()

    fun addMetadataToEbook(ebk: EBook, file: File) {
//        try {
//            val inputstream = FileInputStream(file)
//            parser.parse(inputstream, handler, metadata, context)
//
//            //add ebook metadata
//            for (name in metadata.names()) {
//                ebk.addMetadataEntry(name.toLowerCase(), metadata.get(name))
//                if (name.toLowerCase().contains("title") && metadata.get(name).trim().length > 1) {
//                    ebk.bookTitle = metadata.get(name)
//                } else if (name.toLowerCase().contains("author")) {
//                    ebk.addAuthor(metadata.get(name))
//                }
//            }
//        } catch (e: Throwable) {
//            MyDebug.LOG.error("Apache Tika exception [${ebk.bookTitle}] : ${e.localizedMessage}", e)
//        }
    }

    private fun readEpubMetadata(file: File, ebk: EBook) {
        //       MyDebug.LOG.debug("LibraryScanner::readEpubMetadata($file.name) started")
        ebk.addFileType(FileType.EPUB)
        ebk.bookTitle = (file.name.substring(0, file.name.length - 5))
        ebk.fullFileDirName = ebk.fileDir + File.separator + ebk.bookTitle
        val flen = file.length()
        if (flen < 24500000) {
            try {
                val epubInputStream = FileInputStream(file)
                val erdr = EpubReader()
                val book = erdr.readEpub(epubInputStream)

                ebk.addAuthors(book.metadata.authors)
                ebk.bookTitle = book.title
                val cvrImg = book.coverImage
                if (cvrImg != null) {
                    ebk.setCoverImageFromBitmap(BitmapFactory.decodeStream(cvrImg.inputStream))
                }
                epubInputStream.close()
            } catch (e: IOException) {
                MyDebug.LOG.error("Failed to read epub details from [" + file.name + "] " + e.message)
            } catch (npe: NullPointerException) {
                MyDebug.LOG.error("NullPointerException reading epub details from [" + file.name + "] " + npe.message)
            }

        } else {
            MyDebug.LOG.debug("Skipping Large epub [filename: " + file.name + ", size: " + file.length() + "]")
        }
    }

    private fun readPdfMetadata(ctx: Context, file: File, ebk: EBook) {
        //       MyDebug.LOG.debug("LibraryScanner::readPdfMetadata() started")
        ebk.addFileType(FileType.PDF)
        ebk.bookTitle = file.name.substring(0, file.name.length - 4)
        ebk.fullFileDirName = ebk.fileDir + File.separator + ebk.bookTitle

        try {
            val fileDesc = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

            var pdfiumCore = PdfiumCore(ctx.applicationContext)
            val pdfDocument = pdfiumCore!!.newDocument(fileDesc)
            val meta = pdfiumCore!!.getDocumentMeta(pdfDocument)
            pdfiumCore!!.openPage(pdfDocument, 0)
            val pageNum = 0
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
            MyDebug.LOG.error("FileNotFoundException reading pdf file [" + file.name + "] " + e.message)
        } catch (e1: IOException) {
            MyDebug.LOG.error("IOException reading pdf file [" + file.name + "] " + e1.message)
        }

    }

    //#region read mobi metadata
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
    //endregion Mobi

    //endregion

    @Synchronized
    private fun addEBookToLibraryStorage(libName: String, ebk: EBook) {
        MyDebug.LOG.debug("LibraryScanner::addEBookToLibraryStorage(" + ebk.bookTitle + ") started")
        var dbEbk = libMgr!!.getBookFromFullFilename(ebk.fullFileDirName)
        if (dbEbk == null || dbEbk.id < 1) {
            dbEbk = libMgr!!.addBookToLibrary(libName, ebk)
        } else {
            dbEbk.addFileTypes(ebk.filetypes)
            libMgr!!.updateBook(dbEbk)
        }
        for (a in ebk.authors) {
            val t = libMgr!!.addAuthor(a)
            val ebkAuth = EBookAuthorLink()
            ebkAuth.ebookId = dbEbk.getUniqueId()
            ebkAuth.authorId = t!!.getUniqueId()
            libMgr?.addEbookAuthorLink(ebkAuth)
        }
    }

}
