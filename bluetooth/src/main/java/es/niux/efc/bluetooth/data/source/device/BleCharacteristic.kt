package es.niux.efc.bluetooth.data.source.device

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import es.niux.efc.bluetooth.data.source.BleConnection
import es.niux.efc.bluetooth.data.source.device.BleCharacteristic.Permission
import es.niux.efc.bluetooth.data.source.device.BleCharacteristic.Property
import java.util.*

/**
 * A [BleService] characteristic, which represents a particular characteristic of said service through a value
 * and any [BleDescriptor] describing the value.
 */
interface BleCharacteristic {
    /**
     * A [BleCharacteristic] permission.
     *
     * @see [BleCharacteristic.has]
     */
    sealed class Permission {
        sealed class Read : Permission() {
            /** @see [BluetoothGattCharacteristic.PERMISSION_READ] */
            object Default : Read()

            sealed class Encrypted : Read() {
                /** @see [BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED] */
                object Default : Encrypted()

                /** @see [BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM] */
                object MITM : Encrypted()
            }
        }

        sealed class Write : Permission() {
            /** @see [BluetoothGattCharacteristic.PERMISSION_WRITE] */
            object Default : Write()

            sealed class Encrypted : Write() {
                /** @see [BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED] */
                object Default : Encrypted()

                /** @see [BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM] */
                object MITM : Encrypted()
            }

            sealed class Signed : Write() {
                /** @see [BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED] */
                object Default : Signed()

                /** @see [BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM] */
                object MITM : Signed()
            }
        }

        fun toBitmask(): Int {
            return when (this) {
                is Read -> when (this) {
                    is Read.Default -> BluetoothGattCharacteristic
                        .PERMISSION_READ
                    is Read.Encrypted -> when (this) {
                        is Read.Encrypted.Default -> BluetoothGattCharacteristic
                            .PERMISSION_READ_ENCRYPTED
                        is Read.Encrypted.MITM -> BluetoothGattCharacteristic
                            .PERMISSION_READ_ENCRYPTED_MITM
                    }
                }
                is Write -> when (this) {
                    is Write.Default -> BluetoothGattCharacteristic
                        .PERMISSION_WRITE
                    is Write.Encrypted -> when (this) {
                        is Write.Encrypted.Default -> BluetoothGattCharacteristic
                            .PERMISSION_WRITE_ENCRYPTED
                        is Write.Encrypted.MITM -> BluetoothGattCharacteristic
                            .PERMISSION_WRITE_ENCRYPTED_MITM
                    }
                    is Write.Signed -> when (this) {
                        is Write.Signed.Default -> BluetoothGattCharacteristic
                            .PERMISSION_WRITE_SIGNED
                        is Write.Signed.MITM -> BluetoothGattCharacteristic
                            .PERMISSION_WRITE_SIGNED_MITM
                    }
                }
            }
        }
    }

    /**
     * A [BleCharacteristic] property.
     *
     * @see [BleCharacteristic.has]
     */
    sealed class Property {
        /**
         * The characteristic's value can be written.
         *
         * @see [BleConnection.write]
         */
        sealed class Write : Property() {
            /**
             * The characteristic's value can be written, with a response from the peripheral indicating success.
             *
             * @see [BluetoothGattCharacteristic.PROPERTY_WRITE]
             * @see [BleConnection.write]
             */
            object Response : Write()

            /**
             * The characteristic's value can be written, without a response from the peripheral indicating success.
             *
             * @see [BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE]
             * @see [BleConnection.write]
             */
            object NoResponse : Write()

            /**
             * The characteristic's value can be written with authentication signature, without a response from the peripheral indicating success.
             *
             * @see [BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE]
             * @see [BleConnection.write]
             */
            object Signed : Write()
        }

        /**
         * The characteristic's value can be read.
         *
         * @see [BluetoothGattCharacteristic.PROPERTY_READ]
         * @see [BleConnection.read]
         */
        object Read : Property()

        /**
         * The characteristic's value supports notifications.
         *
         * @see [BluetoothGattCharacteristic.PROPERTY_NOTIFY]
         * @see [BleConnection.notify]
         */
        object Notify : Property()

        /**
         * The characteristic's value supports indications.
         *
         * @see [BluetoothGattCharacteristic.PROPERTY_INDICATE]
         * @see [BleConnection.indicate]
         */
        object Indicate : Property()

        /**
         * The characteristic's value supports broadcasts, through an included Characteristic Configuration [BleDescriptor].
         *
         * @see [BluetoothGattCharacteristic.PROPERTY_BROADCAST]
         * @see [BleCharacteristic.descriptors]
         */
        object Broadcast : Property()

        /**
         * The characteristic's has more properties through an included Extended Properties [BleDescriptor].
         *
         * @see [BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS]
         * @see [BleCharacteristic.descriptors]
         */
        object Extended : Property()

        fun toBitmask(): Int = when (this) {
            is Write -> when (this) {
                is Write.Response -> BluetoothGattCharacteristic
                    .PROPERTY_WRITE
                is Write.NoResponse -> BluetoothGattCharacteristic
                    .PROPERTY_WRITE_NO_RESPONSE
                is Write.Signed -> BluetoothGattCharacteristic
                    .PROPERTY_SIGNED_WRITE
            }
            is Read -> BluetoothGattCharacteristic
                .PROPERTY_READ
            is Notify -> BluetoothGattCharacteristic
                .PROPERTY_NOTIFY
            is Indicate -> BluetoothGattCharacteristic
                .PROPERTY_INDICATE
            is Broadcast -> BluetoothGattCharacteristic
                .PROPERTY_BROADCAST
            is Extended -> BluetoothGattCharacteristic
                .PROPERTY_EXTENDED_PROPS
        }
    }

    /** The [BleService] whose this characteristic is for */
    val service: BleService

    /**
     * The universally unique identifier for this characteristic.
     *
     * @see [BluetoothGattCharacteristic.getUuid]
     */
    val id: UUID

    /**
     * Am identifier for differentiating characteristics on the same service with the same id.
     *
     * @see [BluetoothGattCharacteristic.getInstanceId]
     */
    val idInstance: Int

    /**
     * A list of the descriptors inside this characteristic.
     *
     * @see [BluetoothGattCharacteristic.getDescriptors]
     */
    val descriptors: List<BleDescriptor>

    /**
     * The raw [BluetoothGattCharacteristic]
     *
     * @see [BleConnection.read]
     * @see [BleConnection.write]
     */
    val rawCharacteristic: BluetoothGattCharacteristic

    /** Check this characteristics Permissions */
    fun has(vararg permissions: Permission): Boolean

    /** Check this characteristics Properties */
    fun has(vararg properties: Property): Boolean
}

class BleCharacteristicImpl(
    override val service: BleService,
    override val rawCharacteristic: BluetoothGattCharacteristic,
    descriptorFactory: (characteristic: BleCharacteristic, rawDescriptor: BluetoothGattDescriptor) -> BleDescriptor
) : BleCharacteristic {
    override val id: UUID = rawCharacteristic
        .uuid

    override val idInstance: Int = rawCharacteristic
        .instanceId

    override val descriptors: List<BleDescriptor> = rawCharacteristic
        .descriptors
        .map { descriptorFactory(this, it) }

    override fun has(vararg permissions: Permission): Boolean = permissions
        .firstOrNull { rawCharacteristic.permissions and it.toBitmask() <= 0 } != null

    override fun has(vararg properties: Property): Boolean = properties
        .firstOrNull { rawCharacteristic.properties and it.toBitmask() <= 0 } != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleCharacteristicImpl) return false

        if (service != other.service) return false
        if (rawCharacteristic != other.rawCharacteristic) return false

        return true
    }

    override fun hashCode(): Int {
        var result = service.hashCode()
        result = 31 * result + rawCharacteristic.hashCode()
        return result
    }
}
