/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 16/05/2019 07:53
 * Author     : aaspellc
 *
 */
package uk.co.droidinactu.elibrary.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import uk.co.droidinactu.elibrary.BookLibApplication

/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 16/05/2019 07:53
 * Author     : aaspellc
 *
 */
class InfoBarViewModel : ViewModel() {

    private val libInfo: MutableLiveData<LibraryInfo> by lazy {
        MutableLiveData<LibraryInfo>().also {
            loadEBooks()
        }
    }

    fun getInfo(): LiveData<LibraryInfo> {
        return libInfo
    }

    private fun loadEBooks() {
        val libInfo = LibraryInfo()
        doAsync {
            val libTitle = BookLibApplication.instance.getLibManager().getLibrary().libraryTitle
            val nbrBooks = BookLibApplication.instance.getLibManager().getBookCount()
            uiThread {
                libInfo.libTitle = libTitle
                libInfo.nbrBooks = nbrBooks
            }
        }
    }
}
