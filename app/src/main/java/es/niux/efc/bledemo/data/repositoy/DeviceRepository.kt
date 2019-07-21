package es.niux.efc.bledemo.data.repositoy

import es.niux.efc.bluetooth.data.source.BleSource
import es.niux.efc.bluetooth.data.source.device.BleDevice
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import es.niux.efc.core.common.Schedulers
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

interface DeviceRepository {
    /** Operates on [Schedulers.io] */
    fun observeBleAvailability(): Observable<BleAvailabilityEvent>

    /** Operates on [Schedulers.io] */
    fun scanBleDevices(mode: BleSource.ScanMode = BleSource.ScanMode.LOW): Observable<BleScanEvent>

    fun bleDevice(address: String): Single<BleDevice>
}

class DeviceRepositoryImpl @Inject constructor(
    private val schedulers: Schedulers,
    private val bleSource: BleSource
) : DeviceRepository {
    override fun observeBleAvailability(): Observable<BleAvailabilityEvent> = bleSource
        .availability
        .subscribeOn(schedulers.io)

    override fun scanBleDevices(mode: BleSource.ScanMode): Observable<BleScanEvent> = bleSource
        .scanDevicesChange(
            mode = BleSource.ScanMode.MEDIUM
        )
        .subscribeOn(schedulers.io)

    override fun bleDevice(address: String): Single<BleDevice> = Single
        .just(bleSource.device(address))
}
