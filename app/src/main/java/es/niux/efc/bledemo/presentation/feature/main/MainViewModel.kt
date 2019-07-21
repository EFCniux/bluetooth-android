package es.niux.efc.bledemo.presentation.feature.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import es.niux.efc.bledemo.domain.interactor.BleAvailabilityInteractor
import es.niux.efc.bledemo.domain.interactor.BleScanInteractor
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import es.niux.efc.core.common.Schedulers
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateFactory
import es.niux.efc.core.domain.interactor.interact
import es.niux.efc.core.presentation.livedata.Consumable
import es.niux.efc.core.presentation.livedata.ConsumableLiveData
import es.niux.efc.core.presentation.viewmodel.BaseViewModel
import io.reactivex.disposables.Disposable

sealed class MainEvent {
    sealed class Nav : MainEvent() {
        object Status : Nav()
        object Devices : Nav()
        data class Device(
            val deviceAddress: String
        ) : Nav()
    }
}

abstract class MainViewModel(
    savedState: SavedStateHandle
) : BaseViewModel(savedState) {
    abstract val mainConsumables: LiveData<List<Consumable<MainEvent>>>
    abstract val bleAvailability: LiveData<BleAvailabilityEvent>
    abstract val bleScan: LiveData<List<BleScanEvent.Found>>

    abstract fun navigate(event: MainEvent.Nav)
}

class MainViewModelImpl @AssistedInject constructor(
    @Assisted arg0: SavedStateHandle,
    schedulers: Schedulers,
    bleAvailabilityInteractor: BleAvailabilityInteractor,
    bleScanInteractor: BleScanInteractor
) : MainViewModel(arg0) {
    @AssistedInject.Factory
    interface Factory : ViewModelStateFactory<MainViewModelImpl>

    private val bleAvailabilityObservable = bleAvailabilityInteractor
        .interact()
        .replay(1)
        .refCount()

    override val mainConsumables: ConsumableLiveData<MainEvent> by lazy {
        ConsumableLiveData<MainEvent>()
            .apply {
                bleAvailabilityObservable
                    .observeOn(schedulers.main)
                    .subscribe { bleAvailabilityEvent ->
                        when (bleAvailabilityEvent) {
                            is BleAvailabilityEvent.Available -> navigate(
                                MainEvent.Nav.Devices
                            )
                            is BleAvailabilityEvent.Unavailable -> navigate(
                                MainEvent.Nav.Status
                            )
                        }
                    }
                    .disposeOnClear()
            }
    }

    override val bleAvailability: LiveData<BleAvailabilityEvent> by lazy {
        MutableLiveData<BleAvailabilityEvent>()
            .apply {
                bleAvailabilityObservable
                    .observeOn(schedulers.main)
                    .subscribe { value = it }
                    .disposeOnClear()
            }
    }

    override val bleScan: LiveData<List<BleScanEvent.Found>> by lazy {
        object : LiveData<List<BleScanEvent.Found>>() {
            var disposable: Disposable? = null

            override fun onActive() {
                disposable = bleScanInteractor
                    .interact()
                    .map { list ->
                        list.sortedByDescending { it.rssi }
                    }
                    .observeOn(schedulers.main)
                    .subscribe { value = it }
            }

            override fun onInactive() {
                disposable?.dispose()
            }
        }
    }

    override fun navigate(event: MainEvent.Nav): Unit = mainConsumables
        .mutate {
            removeAll { it is MainEvent.Nav }
            add(event)
        }
}
