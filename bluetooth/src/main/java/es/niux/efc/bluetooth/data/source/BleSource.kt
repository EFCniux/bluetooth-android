package es.niux.efc.bluetooth.data.source

import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.exceptions.BleScanException
import com.polidea.rxandroidble2.scan.ScanCallbackType
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import es.niux.efc.bluetooth.data.source.BleSource.ScanMode
import es.niux.efc.bluetooth.data.source.device.BleDevice
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

/**
 * A data source for [BleDevice].
 *
 * @see <a href="https://devzone.nordicsemi.com/tutorials/b/bluetooth-low-energy/posts/ble-advertising-a-beginners-tutorial">Advertising tutorial</a>
 * @see <a href="https://devzone.nordicsemi.com/tutorials/b/bluetooth-low-energy/posts/ble-services-a-beginners-tutorial">Service tutorial</a>
 * @see <a href="https://devzone.nordicsemi.com/tutorials/b/bluetooth-low-energy/posts/ble-characteristics-a-beginners-tutorial">Characteristic tutorial</a>
 */
interface BleSource {
    enum class ScanMode {
        /** @see [ScanSettings.SCAN_MODE_LOW_LATENCY] */
        HIGH,
        /** @see [ScanSettings.SCAN_MODE_BALANCED] */
        MEDIUM,
        /** @see [ScanSettings.SCAN_MODE_LOW_POWER] */
        LOW,
        /** @see [ScanSettings.SCAN_MODE_OPPORTUNISTIC] */
        PASSIVE
        ;
    }

    /** If current availability is [BleAvailabilityEvent.Available] */
    val isAvailable: Boolean

    /** Observe changes in [BleAvailabilityEvent]. Will emit first its current state. */
    val availability: Observable<BleAvailabilityEvent>

    /**
     * Scan all Bluetooth device packet emissions.
     *
     * Note that [on some Android devices](https://github.com/Polidea/RxAndroidBle/wiki/FAQ:-Cannot-connect#connect-while-scanning),
     * [BleDevice.connect] will error if also actively scanning, even if said scan is initiated outside the application.
     *
     * @see [BleSource.scanDevicesChange]
     */
    fun scanDevices(
        mode: ScanMode = ScanMode.LOW,
        vararg filters: ScanFilter
    ): Observable<BleScanEvent.Found>

    /**
     * Scan Bluetooth device packet emissions to detect when a device is found or lost.
     *
     * @see [BleSource.scanDevices]
     */
    fun scanDevicesChange(
        mode: ScanMode = ScanMode.LOW,
        vararg filters: ScanFilter
    ): Observable<BleScanEvent>

    /** Obtain a [BleDevice] through its [BleDevice.address]. */
    fun device(macAddress: String): BleDevice

    /** Obtain [BleDevice]s that have been bonded/paired previously. */
    fun devicesBonded(): Set<BleDevice>
}

class BleSourceImpl(
    private val client: RxBleClient,
    private val scheduler: Scheduler,
    private val deviceFactory: (device: RxBleDevice) -> BleDevice
) : BleSource {
    private fun ScanMode.toSettings(): Int = when (this) {
        ScanMode.HIGH -> ScanSettings
            .SCAN_MODE_LOW_LATENCY
        ScanMode.MEDIUM -> ScanSettings
            .SCAN_MODE_BALANCED
        ScanMode.LOW -> ScanSettings
            .SCAN_MODE_LOW_POWER
        ScanMode.PASSIVE -> ScanSettings
            .SCAN_MODE_OPPORTUNISTIC
    }

    override val isAvailable: Boolean
        get() = client.state == RxBleClient.State.READY

    override val availability: Observable<BleAvailabilityEvent> by lazy {
        Observable
            .concat(
                Observable
                    .just(client.state),
                client
                    .observeStateChanges()
            )
            .subscribeOn(scheduler)
            .map {
                return@map when (it) {
                    RxBleClient.State.READY ->
                        BleAvailabilityEvent.Available
                    RxBleClient.State.BLUETOOTH_NOT_AVAILABLE ->
                        BleAvailabilityEvent.Unavailable.Permanent
                    RxBleClient.State.BLUETOOTH_NOT_ENABLED ->
                        BleAvailabilityEvent.Unavailable.Disabled
                    RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED ->
                        BleAvailabilityEvent.Unavailable.Location.Permission
                    RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED ->
                        BleAvailabilityEvent.Unavailable.Location.Services
                }
            }
            .replay(1)
            .refCount()
    }

    override fun scanDevices(
        mode: ScanMode,
        vararg filters: ScanFilter
    ): Observable<BleScanEvent.Found> = client
        .scanBleDevices(
            ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setScanMode(mode.toSettings())
                .build(),
            *filters
        )
        .subscribeOn(scheduler)
        .map {
            BleScanEvent.Found(
                it.timestampNanos, it.rssi, it.scanRecord, deviceFactory(it.bleDevice)
            )
        }

    override fun scanDevicesChange(
        mode: ScanMode,
        vararg filters: ScanFilter
    ): Observable<BleScanEvent> = client
        .scanBleDevices(
            ScanSettings.Builder()
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH or ScanSettings.CALLBACK_TYPE_MATCH_LOST)
                .setScanMode(mode.toSettings())
                .build(),
            *filters
        )
        .subscribeOn(scheduler)
        .map {
            return@map when (it.callbackType) {
                ScanCallbackType.CALLBACK_TYPE_FIRST_MATCH -> BleScanEvent.Found(
                    it.timestampNanos, it.rssi, it.scanRecord, deviceFactory(it.bleDevice)
                )
                ScanCallbackType.CALLBACK_TYPE_MATCH_LOST -> BleScanEvent.Lost(
                    it.timestampNanos, it.rssi, it.scanRecord, deviceFactory(it.bleDevice)
                )
                else -> throw IllegalStateException("Unexpected callbackType: " + it.callbackType)
            }
        }
        .onErrorResumeNext { e: Throwable ->
            return@onErrorResumeNext if (
                e is BleScanException
                && (e.reason == BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED
                        || e.reason == BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES)
            ) scanDevices(mode, *filters)
                .toScanDevicesChangeEmulated()
            else Observable
                .error(e)
        }

    /**
     * Convert a [BleScanEvent.Found] observable to a [BleScanEvent] observable, emitting only the first and last
     * found emission for each scanned device, until it is found again.
     *
     * Theory says ble devices advertise periodically in multiples of 0.625 milliseconds,
     * the minimum time being 20 milliseconds and the maximum being 10.24 seconds.
     *
     * @see <a href="https://stackoverflow.com/a/33692286">StackOverflow answer</a>
     */
    private fun Observable<BleScanEvent.Found>.toScanDevicesChangeEmulated(
        lostTimeout: Long = 11,
        lostTimeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): Observable<BleScanEvent> = this
        .groupBy { it.device.address }
        .flatMap { grouped ->
            val shared = grouped
                .share()
            return@flatMap Observable
                .merge<BleScanEvent>(
                    shared,
                    shared
                        .throttleWithTimeout(lostTimeout, lostTimeoutUnit)
                        .map {
                            BleScanEvent.Lost(
                                it.time, it.rssi, it.record, it.device
                            )
                        }
                )
                .distinctUntilChanged { event: BleScanEvent -> event::class }
        }

    override fun device(macAddress: String): BleDevice =
        deviceFactory(client.getBleDevice(macAddress))

    override fun devicesBonded(): Set<BleDevice> = client
        .bondedDevices
        .map { deviceFactory(it) }
        .toSet()
}
