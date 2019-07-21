package es.niux.efc.core.common.injection.presentation.viewmodel

import androidx.lifecycle.AbstractSavedStateVMFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A [ViewModelProvider.Factory] for using Dagger2 map multibindings
 * to inject a [ViewModel] dependencies created by [ViewModelStateFactory]
 */
class ViewModelInjectorFactory(
    private val factories: Map<Class<out ViewModel>, @JvmSuppressWildcards ViewModelStateFactory<*>>,
    state: ViewModelStateProvider
) : AbstractSavedStateVMFactory(state.owner, state.default) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String, modelClass: Class<T>, handle: SavedStateHandle
    ): T = (factories[modelClass]
        ?: factories.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
        ?: throw IllegalArgumentException("No provider found for ViewModel $modelClass"))
        .create(handle) as T
}
