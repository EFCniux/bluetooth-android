package es.niux.efc.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel(
    val savedState: SavedStateHandle
) : ViewModel() {
    private val containerDisposable: CompositeDisposable by lazy { CompositeDisposable() }

    /** Automatically dispose when the containing [BaseViewModel] is cleared */
    fun Disposable.disposeOnClear(): Boolean = containerDisposable.add(this)

    /** Cancel a previous [Disposable.disposeOnClear] */
    fun Disposable.disposeOnClearCancel(): Boolean = containerDisposable.delete(this)

    override fun onCleared() {
        containerDisposable.dispose()
        super.onCleared()
    }
}
