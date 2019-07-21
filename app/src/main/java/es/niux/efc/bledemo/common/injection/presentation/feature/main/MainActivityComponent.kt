package es.niux.efc.bledemo.common.injection.presentation.feature.main

import dagger.Subcomponent
import dagger.android.AndroidInjector
import es.niux.efc.bledemo.presentation.feature.main.MainActivity
import es.niux.efc.core.common.injection.scope.PerActivity

@Subcomponent(
    modules = [
        MainActivityBinderModule::class
    ]
)
@PerActivity
interface MainActivityComponent : AndroidInjector<MainActivity> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<MainActivity>
}
