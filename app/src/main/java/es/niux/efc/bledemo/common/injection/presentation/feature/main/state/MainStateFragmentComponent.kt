package es.niux.efc.bledemo.common.injection.presentation.feature.main.state

import dagger.Subcomponent
import dagger.android.AndroidInjector
import es.niux.efc.bledemo.presentation.feature.main.state.MainStateFragment
import es.niux.efc.core.common.injection.scope.PerFragment

@Subcomponent
@PerFragment
interface MainStateFragmentComponent : AndroidInjector<MainStateFragment> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<MainStateFragment>
}
