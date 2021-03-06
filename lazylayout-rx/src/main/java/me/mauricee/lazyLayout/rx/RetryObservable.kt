package me.mauricee.lazyLayout.rx

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import me.mauricee.lazyLayout.LazyLayout
import me.mauricee.lazyLayout.rx.internal.checkMainThread

internal class RetryObservable(private val lazyLayout: LazyLayout) : Observable<Unit>() {

    override fun subscribeActual(observer: Observer<in Unit>) {
        if (!checkMainThread(observer)) {
            return
        }
        lazyLayout.retryListener = Listener(lazyLayout, observer).also(observer::onSubscribe)
    }

    private class Listener(private val lazyLayout: LazyLayout, private val observer: Observer<in Unit>) :
        MainThreadDisposable(), LazyLayout.RetryListener {
        override fun onRetry() {
            if (!isDisposed) {
                observer.onNext(Unit)
            }
        }

        override fun onDispose() {
            lazyLayout.stateUpdateListener = null
        }

    }
}