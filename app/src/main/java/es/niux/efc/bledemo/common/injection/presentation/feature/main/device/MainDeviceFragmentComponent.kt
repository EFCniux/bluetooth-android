package es.niux.efc.bledemo.common.injection.presentation.feature.main.device

import dagger.Subcomponent
import dagger.android.AndroidInjector
import es.niux.efc.bledemo.presentation.feature.main.device.MainDeviceFragment
import es.niux.efc.core.common.injection.scope.PerFragment

@Subcomponent(
    modules = [
        MainDeviceFragmentBinderModule::class
    ]
)
@PerFragment
interface MainDeviceFragmentComponent : AndroidInjector<MainDeviceFragment> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<MainDeviceFragment>
}
