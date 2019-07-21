package es.niux.efc.core

import android.app.Activity
import android.app.Application
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

abstract class BaseApplication : Application(), HasActivityInjector {
    @Inject
    protected lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    final override fun onCreate() {
        super.onCreate()
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
            onPostCreate()
        }
    }

    abstract fun onPostCreate()

    final override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector
}
