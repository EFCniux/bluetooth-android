package es.niux.efc.bledemo.domain.interactor

import es.niux.efc.bledemo.data.repositoy.DeviceRepository
import es.niux.efc.bluetooth.data.source.device.BleDevice
import es.niux.efc.core.domain.interactor.reactivex.SingleInteractor
import io.reactivex.Single
import javax.inject.Inject

interface BleDeviceInteractor : SingleInteractor<String, BleDevice>

class BleDeviceInteractorImpl @Inject constructor(
    private val deviceRepository: DeviceRepository
) : BleDeviceInteractor {
    override fun interact(input: String): Single<BleDevice> = deviceRepository
        .bleDevice(input)
}
