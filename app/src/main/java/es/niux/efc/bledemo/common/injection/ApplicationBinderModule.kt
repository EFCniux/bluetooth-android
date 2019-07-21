package es.niux.efc.bledemo.common.injection

import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import es.niux.efc.bledemo.common.injection.presentation.feature.main.MainActivityComponent
import es.niux.efc.bledemo.presentation.feature.main.MainActivity

@Module(
    subcomponents = [
        MainActivityComponent::class
    ]
)
abstract class ApplicationBinderModule {
    @Binds
    @IntoMap
    @ClassKey(MainActivity::class)
    abstract fun mainActivityInjectorFactory(
        factory: MainActivityComponent.Factory
    ): AndroidInjector.Factory<*>
}
