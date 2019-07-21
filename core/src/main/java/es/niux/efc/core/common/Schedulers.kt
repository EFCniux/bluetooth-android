package es.niux.efc.core.common

import android.os.Looper
import io.reactivex.Scheduler
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

interface Schedulers {
    /** @see [AndroidSchedulers.mainThread] */
    val main: Scheduler
    /** @see [Schedulers.io] */
    val io: Scheduler
    /** @see [Schedulers.computation] */
    val comp: Scheduler
    /** @see [Schedulers.trampoline] */
    val tramp: Scheduler
    /** @see [Schedulers.single] */
    val single: Scheduler
}

class SchedulersImpl : es.niux.efc.core.common.Schedulers {
    override val main: Scheduler by lazy {
        RxAndroidPlugins
            .setInitMainThreadSchedulerHandler {
                AndroidSchedulers
                    .from(Looper.getMainLooper(), true)
            }
        return@lazy AndroidSchedulers.mainThread()
    }

    override val io: Scheduler by lazy { Schedulers.io() }

    override val comp: Scheduler by lazy { Schedulers.computation() }

    override val tramp: Scheduler by lazy { Schedulers.trampoline() }

    override val single: Scheduler by lazy { Schedulers.single() }
}
