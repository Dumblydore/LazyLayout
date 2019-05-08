@file:JvmName("RxLazyLayout")

package me.mauricee.lazylayout.rx

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import me.mauricee.lazylayout.rx.internal.checkMainThread
import me.mauricee.lazylayout.widget.LazyLayout

@CheckResult
fun LazyLayout.stateChanges(): Observable<Int> = StateObservable(this)

private class StateObservable(private val lazyLayout: LazyLayout) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!checkMainThread(observer)) {
            return
        }
        lazyLayout.stateUpdateListener = Listener(lazyLayout, observer).also(observer::onSubscribe)
    }

    private class Listener(private val lazyLayout: LazyLayout, private val observer: Observer<in Int>) :
        MainThreadDisposable(), LazyLayout.StateUpdateListener {
        override fun onDispose() {
            lazyLayout.stateUpdateListener = null
        }

        override fun onStateUpdated(state: Int) {
            if (!isDisposed) {
                observer.onNext(state)
            }
        }

    }
}