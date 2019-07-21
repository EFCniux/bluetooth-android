package es.niux.efc.bledemo.domain.interactor

import es.niux.efc.bledemo.data.repositoy.DeviceRepository
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.core.domain.interactor.reactivex.ObservableInteractor
import io.reactivex.Observable
import javax.inject.Inject

interface BleAvailabilityInteractor : ObservableInteractor<Unit, BleAvailabilityEvent>

class BleAvailabilityInteractorImpl @Inject constructor(
    private val deviceRepository: DeviceRepository
) : BleAvailabilityInteractor {
    override fun interact(input: Unit): Observable<BleAvailabilityEvent> = deviceRepository
        .observeBleAvailability()
}
