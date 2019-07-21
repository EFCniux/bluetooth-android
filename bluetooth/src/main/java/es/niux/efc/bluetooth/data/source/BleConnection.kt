package es.niux.efc.bluetooth.data.source

import android.bluetooth.BluetoothGattService
import android.os.Build
import androidx.annotation.RequiresApi
import com.polidea.rxandroidble2.NotificationSetupMode
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleConnection.WriteOperationAckStrategy
import com.polidea.rxandroidble2.RxBleConnection.WriteOperationRetryStrategy
import es.niux.efc.bluetooth.data.source.device.BleCharacteristic
import es.niux.efc.bluetooth.data.source.device.BleDescriptor
import es.niux.efc.bluetooth.data.source.device.BleDevice
import es.niux.efc.bluetooth.data.source.device.BleService
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit

/**
 * A connection to a remove [BleDevice].
 */
interface BleConnection {
    /** The [BleDevice] whose this connection is for */
    val device: BleDevice

    /** Maximum transfer unit. On pre sdk 21 it will always be 23. */
    val mtu: Int

    /** MTU change request. Requires sdk 21+ */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun mtu(mtu: Int): Single<Int>

    /**
     * Received signal strength indicator.
     *
     * @see [BleScanEvent.rssi]
     */
    val rssi: Single<Int>

    /** GATT service discovery. Results will be cached. */
    fun services(
        timeout: Long? = null,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): Single<List<BleService>>

    /** Read a given [BleCharacteristic]. */
    fun read(
        characteristic: BleCharacteristic
    ): Single<ByteArray>

    /** Read a given [BleDescriptor]. */
    fun read(
        descriptor: BleDescriptor
    ): Single<ByteArray>

    /** Write a given [BleCharacteristic]. */
    fun write(
        characteristic: BleCharacteristic,
        data: ByteArray
    ): Single<ByteArray>

    /**
     * Write a given [BleCharacteristic], with the data sent in batches.
     *
     * Useful for when the BLE peripheral does not handle long writes at the firmware level.
     */
    fun writeLong(
        characteristic: BleCharacteristic,
        data: ByteArray,
        batchSize: Int? = null,
        acknowledgeStrategy: WriteOperationAckStrategy? = null,
        retryStrategy: WriteOperationRetryStrategy? = null
    ): Observable<ByteArray>

    /** Write a given [BleDescriptor]. */
    fun write(
        descriptor: BleDescriptor,
        data: ByteArray
    ): Completable

    /**
     * Setup a notification for a given [BleCharacteristic].
     *
     * @see [BleConnection.indicate]
     *
     * @return An [Observable] of [Observable] of [ByteArray].
     *
     * When subscribed, will setup the notification, then emit a single [Observable],
     * which in turn emits the changes in the [BleCharacteristic]
     *
     * Will maintain notification as long as there is at least one active subscription,
     * otherwise the notification will be stopped.
     */
    fun notify(
        characteristic: BleCharacteristic,
        mode: NotificationSetupMode = NotificationSetupMode.DEFAULT
    ): Observable<Observable<ByteArray>>

    /**
     * Setup an indication for a given [BleCharacteristic].
     *
     * Normally each indication should be answered with an acknowledgement,
     * however this is already automatically done by the OS.
     * So basically use indications only if notifications cannot be used.
     *
     * @see [BleConnection.notify]
     */
    fun indicate(
        characteristic: BleCharacteristic,
        mode: NotificationSetupMode = NotificationSetupMode.DEFAULT
    ): Observable<Observable<ByteArray>>
}

class BleConnectionImpl(
    override val device: BleDevice,
    private val rxConnection: RxBleConnection,
    private val scheduler: Scheduler,
    private val serviceFactory: (connection: BleConnection, rawService: BluetoothGattService) -> BleService
) : BleConnection {
    override val mtu: Int
        get() = rxConnection.mtu

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun mtu(mtu: Int): Single<Int> = rxConnection
        .requestMtu(mtu)
        .subscribeOn(scheduler)

    override val rssi: Single<Int> = rxConnection
        .readRssi()
        .observeOn(scheduler)

    override fun services(
        timeout: Long?,
        timeUnit: TimeUnit
    ): Single<List<BleService>> = when (timeout) {
        null -> rxConnection
            .discoverServices()
        else -> rxConnection
            .discoverServices(timeout, timeUnit)
    }.subscribeOn(scheduler)
        .map { rxServices ->
            rxServices.bluetoothGattServices
                .map { serviceFactory(this, it) }
        }

    override fun read(
        characteristic: BleCharacteristic
    ): Single<ByteArray> = rxConnection
        .readCharacteristic(characteristic.rawCharacteristic)
        .subscribeOn(scheduler)

    override fun read(
        descriptor: BleDescriptor
    ): Single<ByteArray> = rxConnection
        .readDescriptor(descriptor.rawDescriptor)
        .subscribeOn(scheduler)

    override fun write(
        characteristic: BleCharacteristic,
        data: ByteArray
    ): Single<ByteArray> = rxConnection
        .writeCharacteristic(characteristic.rawCharacteristic, data)
        .subscribeOn(scheduler)

    override fun writeLong(
        characteristic: BleCharacteristic,
        data: ByteArray,
        batchSize: Int?,
        acknowledgeStrategy: WriteOperationAckStrategy?,
        retryStrategy: WriteOperationRetryStrategy?
    ): Observable<ByteArray> {
        val builder: RxBleConnection.LongWriteOperationBuilder = rxConnection
            .createNewLongWriteBuilder()
            .setCharacteristic(characteristic.rawCharacteristic)
            .setBytes(data)
        if (batchSize != null) builder
            .setMaxBatchSize(batchSize)
        if (acknowledgeStrategy != null) builder
            .setWriteOperationAckStrategy(acknowledgeStrategy)
        if (retryStrategy != null) builder
            .setWriteOperationRetryStrategy(retryStrategy)
        return builder
            .build()
            .subscribeOn(scheduler)
    }

    override fun write(
        descriptor: BleDescriptor,
        data: ByteArray
    ): Completable = rxConnection
        .writeDescriptor(descriptor.rawDescriptor, data)

    override fun notify(
        characteristic: BleCharacteristic,
        mode: NotificationSetupMode
    ): Observable<Observable<ByteArray>> = rxConnection
        .setupNotification(characteristic.rawCharacteristic, mode)
        .subscribeOn(scheduler)

    override fun indicate(
        characteristic: BleCharacteristic,
        mode: NotificationSetupMode
    ): Observable<Observable<ByteArray>> = rxConnection
        .setupIndication(characteristic.rawCharacteristic, mode)
        .subscribeOn(scheduler)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleConnectionImpl) return false

        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        return device.hashCode()
    }
}
