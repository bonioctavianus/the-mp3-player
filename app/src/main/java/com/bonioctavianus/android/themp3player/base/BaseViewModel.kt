package com.bonioctavianus.android.themp3player.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel<I : Mp3Intent, S : Mp3ViewState> : ViewModel(), Mp3ViewModel<I, S> {

    private val disposables = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    private fun destroyDisposable() {
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
    }

    override fun onCleared() {
        super.onCleared()
        destroyDisposable()
    }
}
