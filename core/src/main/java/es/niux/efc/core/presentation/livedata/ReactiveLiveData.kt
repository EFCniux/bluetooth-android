package es.niux.efc.core.presentation.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

fun <T> LiveData<T>.toPublisher(
    mainScheduler: Scheduler = AndroidSchedulers.mainThread()
): Flowable<T> = Flowable
    .create({ emitter: FlowableEmitter<T> ->
        val obs = Observer<T>(emitter::onNext)
        emitter.setCancellable { removeObserver(obs) }
        observeForever(obs)
    }, BackpressureStrategy.LATEST)
    .subscribeOn(mainScheduler)
    .unsubscribeOn(mainScheduler)
    .observeOn(mainScheduler)
