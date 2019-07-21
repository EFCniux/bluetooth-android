package es.niux.efc.bledemo.common.injection

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import es.niux.efc.bledemo.TheApplication
import javax.inject.Singleton

@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        ApplicationBinderModule::class,
        ApplicationAssistedModule::class
    ]
)
@Singleton
interface ApplicationComponent : AndroidInjector<TheApplication> {
    @Component.Factory
    interface Factory : AndroidInjector.Factory<TheApplication>
}
