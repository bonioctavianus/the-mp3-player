package com.bonioctavianus.android.themp3player.base

import io.reactivex.Observable

interface Mp3View<I : Mp3Intent, S : Mp3ViewState> {
    fun intents(): Observable<I>
    fun bindIntent(intent: Observable<I>)
    fun observeState()
}