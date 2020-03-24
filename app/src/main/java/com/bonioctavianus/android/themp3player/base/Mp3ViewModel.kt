package com.bonioctavianus.android.themp3player.base

import io.reactivex.Observable

interface Mp3ViewModel<I : Mp3Intent, S : Mp3ViewState> {
    fun bindIntent(intent: Observable<I>)
}