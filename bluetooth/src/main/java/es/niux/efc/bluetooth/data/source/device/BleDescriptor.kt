package es.niux.efc.bluetooth.data.source.device

import android.bluetooth.BluetoothGattDescriptor
import es.niux.efc.bluetooth.data.source.BleConnection
import es.niux.efc.bluetooth.data.source.device.BleDescriptor.Permission
import java.util.*

/**
 * A [BleCharacteristic] descriptor, which describes additional information about it's characteristic value.
 */
interface BleDescriptor {
    /**
     * A [BleDescriptor] permission.
     *
     * @see [BleDescriptor.has]
     */
    sealed class Permission {
        sealed class Read : Permission() {
            /** @see [BluetoothGattDescriptor.PERMISSION_READ] */
            object Default : Read()

            sealed class Encrypted : Read() {
                /** @see [BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED] */
                object Default : Encrypted()

                /** @see [BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM] */
                object MITM : Encrypted()
            }
        }

        sealed class Write : Permission() {
            /** @see [BluetoothGattDescriptor.PERMISSION_WRITE] */
            object Default : Write()

            sealed class Encrypted : Write() {
                /** @see [BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED] */
                object Default : Encrypted()

                /** @see [BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM] */
                object MITM : Encrypted()
            }

            sealed class Signed : Write() {
                /** @see [BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED] */
                object Default : Signed()

                /** @see [BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM] */
                object MITM : Signed()
            }
        }

        fun toBitmask(): Int {
            return when (this) {
                is Read -> when (this) {
                    is Read.Default -> BluetoothGattDescriptor
                        .PERMISSION_READ
                    is Read.Encrypted -> when (this) {
                        is Read.Encrypted.Default -> BluetoothGattDescriptor
                            .PERMISSION_READ_ENCRYPTED
                        is Read.Encrypted.MITM -> BluetoothGattDescriptor
                            .PERMISSION_READ_ENCRYPTED_MITM
                    }
                }
                is Write -> when (this) {
                    is Write.Default -> BluetoothGattDescriptor
                        .PERMISSION_WRITE
                    is Write.Encrypted -> when (this) {
                        is Write.Encrypted.Default -> BluetoothGattDescriptor
                            .PERMISSION_WRITE_ENCRYPTED
                        is Write.Encrypted.MITM -> BluetoothGattDescriptor
                            .PERMISSION_WRITE_ENCRYPTED_MITM
                    }
                    is Write.Signed -> when (this) {
                        is Write.Signed.Default -> BluetoothGattDescriptor
                            .PERMISSION_WRITE_SIGNED
                        is Write.Signed.MITM -> BluetoothGattDescriptor
                            .PERMISSION_WRITE_SIGNED_MITM
                    }
                }
            }
        }
    }

    /** The [BleCharacteristic] whose this descriptor is for */
    val characteristic: BleCharacteristic

    /**
     * The universally unique identifier for this descriptor.
     *
     * @see [BluetoothGattDescriptor.getUuid]
     */
    val id: UUID

    /** Check this descriptors Permissions */
    fun has(vararg permissions: Permission): Boolean

    /**
     * The raw [BluetoothGattDescriptor]
     *
     * @see [BleConnection.read]
     * @see [BleConnection.write]
     */
    val rawDescriptor: BluetoothGattDescriptor
}

class BleDescriptorImpl(
    override val characteristic: BleCharacteristic,
    override val rawDescriptor: BluetoothGattDescriptor
) : BleDescriptor {
    override val id: UUID = rawDescriptor
        .uuid

    override fun has(vararg permissions: Permission): Boolean = permissions
        .firstOrNull { rawDescriptor.permissions and it.toBitmask() <= 0 } != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BleDescriptorImpl) return false

        if (characteristic != other.characteristic) return false
        if (rawDescriptor != other.rawDescriptor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = characteristic.hashCode()
        result = 31 * result + rawDescriptor.hashCode()
        return result
    }
}
