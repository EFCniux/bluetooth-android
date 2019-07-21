package es.niux.efc.bledemo.common.injection.presentation.feature.main.device

import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import es.niux.efc.bledemo.presentation.feature.main.device.MainDeviceFragment
import es.niux.efc.bledemo.presentation.feature.main.device.MainDeviceViewModel
import es.niux.efc.bledemo.presentation.feature.main.device.MainDeviceViewModelImpl
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelInjectorFactory
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelKey
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateFactory
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateProvider
import es.niux.efc.core.common.injection.qualifier.For
import es.niux.efc.core.common.injection.scope.PerFragment

@Module
abstract class MainDeviceFragmentBinderModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainDeviceViewModel::class)
    abstract fun bindMainDeviceViewModelFactory(
        factory: MainDeviceViewModelImpl.Factory
    ): ViewModelStateFactory<*>

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PerFragment
        @For(MainDeviceViewModel::class)
        fun viewModelInjectorFactory(
            factories: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModelStateFactory<*>>,
            fragment: MainDeviceFragment
        ): ViewModelInjectorFactory = ViewModelInjectorFactory(
            factories, ViewModelStateProvider(
                fragment, bundleOf(
                    MainDeviceViewModel.ARG_BLE_DEVICE_ADDRESS to fragment.args.bleDeviceAddress
                )
            )
        )
    }
}
