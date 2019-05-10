/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 06/05/2019 15:12
 * Author     : aaspellc
 *
 */
package uk.co.droidinactu.elibrary.library

import io.reactivex.Observable
import io.reactivex.Observer
import uk.co.droidinactu.elibrary.room.EBook

/*
 * Copyright 2019 (C) Andy Aspell-Clark
 *
 * Created on : 06/05/2019 15:12
 * Author     : aaspellc
 *
 */
class ReactiveFileMetadataReader : Observable<EBook>() {

    override fun subscribeActual(observer: Observer<in EBook>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}
