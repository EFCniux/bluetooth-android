package es.niux.efc.bluetooth.data.source.event

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import com.polidea.rxandroidble2.RxBleClient

@Suppress("unused")
sealed class BleAvailabilityEvent {
    /** @see [RxBleClient.State.READY] */
    object Available : BleAvailabilityEvent()

    sealed class Unavailable : BleAvailabilityEvent() {
        /** @see [RxBleClient.State.BLUETOOTH_NOT_AVAILABLE] */
        object Permanent : Unavailable()

        /** @see [RxBleClient.State.BLUETOOTH_NOT_ENABLED] */
        object Disabled : Unavailable() {
            /** @see [BluetoothAdapter.ACTION_REQUEST_ENABLE] */
            val enable: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        }

        sealed class Location : Unavailable() {
            /** @see [RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED] */
            object Permission : Location()

            /** @see [RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED] */
            object Services : Location()
        }
    }
}
