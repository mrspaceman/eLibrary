/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 06/05/2019 15:12
 * Author     : aaspellc
 *
 */
package uk.co.droidinactu.elibrary.library

import android.content.Context
import android.os.Handler
import uk.co.droidinactu.elibrary.MyDebug
import uk.co.droidinactu.elibrary.room.EBook
import java.io.File

/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 06/05/2019 15:12
 * Author     : aaspellc
 *
 */
class ReactiveLibraryScanner {

    fun scanLibraryForEbooks(ctx: Context, prgBrHandler: Handler, libname: String, rootdir: String) {
        MyDebug.LOG.debug("ReactiveLibraryScanner::scanLibraryForEbooks() started")

        findDirs(rootdir)
            .parallelStream()
            .forEach { t ->
                findFiles(t)
                    .parallelStream()
                    .forEach { t -> readFileData(t) }
            }
    }

    private fun findDirs(dirname: String): List<String> {
        MyDebug.LOG.debug("ReactiveLibraryScanner::findDirs() started")
        var dirnames = ArrayList<String>()
        var f = File(dirname.trim { it <= ' ' })
        val listOfFiles = f.list()

        dirnames.add(dirname)
        for (dname in listOfFiles) {
            f = File(dirname + File.separator + dname)
            if (f.isDirectory) {
                dirnames!!.add(dirname + File.separator + dname)
                findDirs(dirname + File.separator + dname)
            }
        }
        return dirnames
    }

    private fun findFiles(dir: String): List<File> {
        MyDebug.LOG.debug("ReactiveLibraryScanner::findFiles($dir) started")
        var files = ArrayList<File>()
        var f = File(dir)
        val listOfFiles = f.list()

        for (filename in listOfFiles) {
            f = File(dir + File.separator + filename)
            if (f.isFile && (filename.toLowerCase().endsWith("epub") || filename.toLowerCase().endsWith("pdf"))) {
                files.add(File(dir + File.separator + filename))
            }
        }
        return files
    }

    private fun readFileData(file: File): EBook {
        val ebk = EBook()

        return ebk
    }
}
