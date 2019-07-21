package es.niux.efc.bledemo.common.injection.presentation.feature.main

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import es.niux.efc.bledemo.common.injection.presentation.feature.main.device.MainDeviceFragmentComponent
import es.niux.efc.bledemo.common.injection.presentation.feature.main.devices.MainDevicesFragmentComponent
import es.niux.efc.bledemo.common.injection.presentation.feature.main.state.MainStateFragmentComponent
import es.niux.efc.bledemo.presentation.feature.main.MainActivity
import es.niux.efc.bledemo.presentation.feature.main.MainViewModel
import es.niux.efc.bledemo.presentation.feature.main.MainViewModelImpl
import es.niux.efc.bledemo.presentation.feature.main.device.MainDeviceFragment
import es.niux.efc.bledemo.presentation.feature.main.devices.MainDevicesFragment
import es.niux.efc.bledemo.presentation.feature.main.state.MainStateFragment
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelKey
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateFactory
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateProvider
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.common.injection.scope.PerActivity

@Module(
    subcomponents = [
        MainStateFragmentComponent::class,
        MainDevicesFragmentComponent::class,
        MainDeviceFragmentComponent::class
    ]
)
abstract class MainActivityBinderModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun mainViewModelFactory(
        factory: MainViewModelImpl.Factory
    ): ViewModelStateFactory<*>

    @Binds
    @IntoMap
    @ClassKey(MainStateFragment::class)
    abstract fun mainStateFragmentInjectorFactory(
        factory: MainStateFragmentComponent.Factory
    ): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(MainDevicesFragment::class)
    abstract fun mainDevicesFragmentInjectorFactory(
        factory: MainDevicesFragmentComponent.Factory
    ): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(MainDeviceFragment::class)
    abstract fun mainDeviceFragmentInjectorFactory(
        factory: MainDeviceFragmentComponent.Factory
    ): AndroidInjector.Factory<*>

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerActivity
        @For(MainViewModel::class)
        fun viewModelInjectorFactory(
            factories: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModelStateFactory<*>>,
            activity: MainActivity
        ): ViewModelInjectorFactory = ViewModelInjectorFactory(
            factories, ViewModelStateProvider(activity)
        )
    }
}
