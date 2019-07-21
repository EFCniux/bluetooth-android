package es.niux.efc.bledemo

import es.niux.efc.bledemo.common.injection.DaggerApplicationComponent
import es.niux.efc.core.BaseApplication
import timber.log.Timber

class TheApplication : BaseApplication() {
    override fun onPostCreate() {
        DaggerApplicationComponent
            .factory()
            .create(this)
            .inject(this)

        Timber
            .plant(
                Timber.DebugTree()
            )
    }
}
