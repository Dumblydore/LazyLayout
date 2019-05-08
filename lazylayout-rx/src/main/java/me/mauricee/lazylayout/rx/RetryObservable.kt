@file:JvmName("RxLazyLayout")

package me.mauricee.lazylayout.rx

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import me.mauricee.lazylayout.rx.internal.checkMainThread
import me.mauricee.lazylayout.widget.LazyLayout

@CheckResult
fun LazyLayout.retries(): Observable<Unit> = RetryObservable(this)

private class RetryObservable(private val lazyLayout: LazyLayout) : Observable<Unit>() {

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