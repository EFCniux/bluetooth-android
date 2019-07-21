package es.niux.efc.core.common.injection.presentation.viewmodel

import android.os.Bundle
import androidx.savedstate.SavedStateRegistryOwner

data class ViewModelStateProvider(
    /** The state owner. */
    val owner: SavedStateRegistryOwner,
    /** The default state. */
    val default: Bundle? = null
)
