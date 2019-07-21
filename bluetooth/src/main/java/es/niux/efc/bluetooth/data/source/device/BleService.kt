package es.niux.efc.bluetooth.data.source.device

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

/**
 * A [BleDevice] service, which represents a collection of data and its associated behaviors to accomplish
 * a particular function or feature.
 */
interface BleService {
    /** The [BleDevice] whose this service is for */
    val device: BleDevice

    /**
     * The universally unique identifier for this service.
     *
     * @see [BluetoothGattService.getUuid]
     */
    val id: UUID

    /**
     * Am identifier for differentiating services on the same device with the same id,
     * (For example, a different battery service for each individual battery)
     *
     * @see [BluetoothGattService.getInstanceId]
     */
    val idInstance: Int

    /**
     * If this service is [BluetoothGattService.SERVICE_TYPE_PRIMARY] or [BluetoothGattService.SERVICE_TYPE_SECONDARY]
     *
     * @see [BluetoothGattService.getType]
     */
    val isPrimary: Boolean

    /**
     * A list of the secondary services inside this service.
     *
     * @see [BluetoothGattService.getIncludedServices]
     */
    val services: List<BleService>

    /**
     * A list of the characteristics inside this service.
     *
     * @see [BluetoothGattService.getCharacteristics]
     */
    val characteristics: List<BleCharacteristic>
}

class BleServiceImpl(
    override val device: BleDevice,
    private val rawService: BluetoothGattService,
    characteristicFactory: (service: BleService, rawCharacteristic: BluetoothGattCharacteristic) -> BleCharacteristic
) : BleService {
    override val id: UUID = rawService
        .uuid

    override val idInstance: Int = rawService
        .instanceId

    override val isPrimary: Boolean = rawService
        .type == BluetoothGattService.SERVICE_TYPE_PRIMARY

    override val services: List<BleService> = rawService
        .includedServices
        .map { BleServiceImpl(device, it, characteristicFactory) }

    override val characteristics: List<BleCharacteristic> = rawService
        .characteristics
        .map { characteristicFactory(this, it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleServiceImpl) return false

        if (device != other.device) return false
        if (rawService != other.rawService) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + rawService.hashCode()
        return result
    }
}
