package es.niux.efc.bledemo.common.injection.presentation.feature.main.devices

import dagger.Subcomponent
import dagger.android.AndroidInjector
import es.niux.efc.bledemo.presentation.feature.main.devices.MainDevicesFragment
import es.niux.efc.core.common.injection.scope.PerFragment

@Subcomponent
@PerFragment
interface MainDevicesFragmentComponent : AndroidInjector<MainDevicesFragment> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<MainDevicesFragment>
}
