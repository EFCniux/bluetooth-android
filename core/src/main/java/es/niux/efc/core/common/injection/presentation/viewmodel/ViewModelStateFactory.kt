package es.niux.efc.core.common.injection.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * A factory for [ViewModel] to add [SavedStateHandle] to their dependencies.
 */
interface ViewModelStateFactory<out T : ViewModel> {
    fun create(savedState: SavedStateHandle): T
}
