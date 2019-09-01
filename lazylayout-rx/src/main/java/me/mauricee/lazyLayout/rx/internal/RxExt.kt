@file:JvmName("RxLazyLayout")
package me.mauricee.lazyLayout.rx.internal

import androidx.annotation.CheckResult
import io.reactivex.Observable
import me.mauricee.lazyLayout.LazyLayout
import me.mauricee.lazyLayout.rx.RetryObservable
import me.mauricee.lazyLayout.rx.StateObservable

@CheckResult
fun LazyLayout.retries(): Observable<Unit> = RetryObservable(this)
@CheckResult
fun LazyLayout.stateChanges(): Observable<Int> = StateObservable(this)