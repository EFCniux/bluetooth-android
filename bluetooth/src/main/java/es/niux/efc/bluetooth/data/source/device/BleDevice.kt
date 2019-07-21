package es.niux.efc.bluetooth.data.source.device

import android.bluetooth.BluetoothDevice
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.Timeout
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import es.niux.efc.bluetooth.data.source.BleConnection
import es.niux.efc.bluetooth.data.source.event.BleConnectionEvent
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A remote bluetooth capable peripheral acting as a (GATT) server.
 */
interface BleDevice {
    companion object {
        /**
         * org.bluetooth.service.generic_access
         *
         * The generic_access service contains generic information about the device.
         * All available Characteristics are readonly.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Services/org.bluetooth.service.generic_access.xml">Specification</a>
         */
        val GAP_SERVICE: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.characteristic.gap.device_name
         *
         * The device name, in UTF-8.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.gap.device_name.xml">Specification</a>
         */
        val GAP_NAME_CHARACTERISTIC: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.characteristic.gap.appearance
         *
         * The external appearance of a device.
         * The values are composed of a category (10-bits) and sub-categories (6-bits).
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.gap.appearance.xml">Specification</a>
         */
        val GAP_APPEARANCE_CHARACTERISTIC: UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.service.device_information
         *
         * The Device Information Service exposes manufacturer and/or vendor information about a device.
         * All available Characteristics are readonly.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Services/org.bluetooth.service.device_information.xml">Specification</a>
         */
        val DEV_INFO_SERVICE: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.characteristic.manufacturer_name_string
         *
         * This characteristic represents the name of the manufacturer of the device, in UTF-8.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.manufacturer_name_string.xml">Specification</a>
         */
        val DEV_INFO_MANUFACTURER_CHARACTERISTIC: UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.characteristic.model_number_string
         *
         * This characteristic represents the model number that is assigned by the device vendor, in UTF-8.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.model_number_string.xml">Specification</a>
         */
        val DEV_INFO_MODEL_CHARACTERISTIC: UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")

        /**
         * org.bluetooth.characteristic.serial_number_string
         *
         * This characteristic represents the serial number for a particular instance of the device, in UTF-8.
         *
         * @see <a href="https://www.bluetooth.com/wp-content/uploads/Sitecore-Media-Library/Gatt/Xml/Characteristics/org.bluetooth.characteristic.serial_number_string.xml">Specification</a>
         */
        val DEV_INFO_SERIAL_CHARACTERISTIC: UUID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")
    }

    /** @see [BluetoothDevice.getAddress] */
    val address: String

    /** @see [BluetoothDevice.getName] */
    val name: String?

    /** If connection state is [BleConnectionEvent.Connected] */
    val isConnected: Boolean

    /** Observe changes in [BleConnectionEvent]. Will emit first its current state. */
    val connection: Observable<BleConnectionEvent>

    /**
     * Initiate a connection to this [BleDevice].
     *
     * @param auto If false, will attempt connection immediately, otherwise,
     * will wait until device is scanned to attempt connection.
     * See [android.bluetooth.BluetoothDevice.connectGatt].
     *
     * @param timeout The timeout after any operation on [BleConnection] will be considered broken.
     * May leave the OS bluetooth in an inconsistent state.
     *
     * @param timeUnit The [TimeUnit] to be used on the timeout.
     *
     * @return Am [Observable] of [BleConnection] on this [BleDevice].
     *
     * When subscribed, a connection will be started, and will emit a single [BleConnection].
     *
     * Will stay connected as long as there is at least one active subscription,
     * otherwise the connection will be stopped.
     *
     * If the OS interrupts the connection, a [BleDisconnectedException] will be emitted.
     *
     * @see <a href="https://github.com/Polidea/RxAndroidBle/wiki/Tutorial:-Connection-Observable-sharing">Connection sharing</a>
     */
    fun connect(
        auto: Boolean = false,
        timeout: Long? = null,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): Observable<BleConnection>
}

class BleDeviceImpl constructor(
    private val rxDevice: RxBleDevice,
    private val scheduler: Scheduler,
    private val connectionFactory: (device: BleDevice, connection: RxBleConnection) -> BleConnection
) : BleDevice {
    override val address: String
        get() = rxDevice.macAddress

    override val name: String?
        get() = rxDevice.name

    override val isConnected: Boolean
        get() = rxDevice.connectionState == RxBleConnection.RxBleConnectionState.CONNECTED

    override val connection: Observable<BleConnectionEvent> by lazy {
        Observable
            .concat(
                Observable
                    .just(rxDevice.connectionState),
                rxDevice
                    .observeConnectionStateChanges()
            )
            .subscribeOn(scheduler)
            .map {
                return@map when (it) {
                    RxBleConnection.RxBleConnectionState.CONNECTING ->
                        BleConnectionEvent.Connecting(this)
                    RxBleConnection.RxBleConnectionState.CONNECTED ->
                        BleConnectionEvent.Connected(this)
                    RxBleConnection.RxBleConnectionState.DISCONNECTED ->
                        BleConnectionEvent.Disconnected(this)
                    RxBleConnection.RxBleConnectionState.DISCONNECTING ->
                        BleConnectionEvent.Disconnecting(this)
                }
            }
            .replay(1)
            .refCount()
    }

    override fun connect(
        auto: Boolean,
        timeout: Long?,
        timeUnit: TimeUnit
    ): Observable<BleConnection> = when (timeout) {
        null -> rxDevice
            .establishConnection(auto)
        else -> rxDevice
            .establishConnection(auto, Timeout(timeout, timeUnit))
    }.subscribeOn(scheduler)
        .map { connectionFactory(this, it) }
        .replay(1)
        .refCount()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleDeviceImpl) return false

        if (rxDevice != other.rxDevice) return false

        return true
    }

    override fun hashCode(): Int {
        return rxDevice.hashCode()
    }
}
