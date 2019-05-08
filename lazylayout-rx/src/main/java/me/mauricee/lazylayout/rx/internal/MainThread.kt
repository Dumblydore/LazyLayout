package me.mauricee.lazylayout.rx.internal

import android.os.Looper
import androidx.annotation.RestrictTo
import io.reactivex.Observer
import io.reactivex.disposables.Disposables

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun checkMainThread(observer: Observer<*>): Boolean {
    if (Looper.myLooper() != Looper.getMainLooper()) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().name))
        return false
    }
    return true
}