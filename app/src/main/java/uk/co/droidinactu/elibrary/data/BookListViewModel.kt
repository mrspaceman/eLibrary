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
import uk.co.droidinactu.ebooklib.room.EBook
import uk.co.droidinactu.elibrary.BookLibApplication

/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 16/05/2019 07:53
 * Author     : aaspellc
 *
 */
class BookListViewModel : ViewModel() {

    private var currentTag: String = "Unknown"

    private val books: MutableLiveData<List<EBook>> by lazy {
        MutableLiveData<List<EBook>>().also {
            loadEBooks()
        }
    }

    fun getBooks(): LiveData<List<EBook>> {
        return books
    }

    private fun loadEBooks() {
        if (currentTag != "Unknown") {
            books.value =
                BookLibApplication.instance.getLibManager()
                    .getBooksForTag(currentTag)
        }
    }

    fun setTag(newTag: String) {
        this.currentTag = newTag
    }
}
