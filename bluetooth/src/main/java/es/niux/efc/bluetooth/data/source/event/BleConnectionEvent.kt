package es.niux.efc.bluetooth.data.source.event

import es.niux.efc.bluetooth.data.source.device.BleDevice

sealed class BleConnectionEvent(
    open val device: BleDevice
) {
    data class Connecting(
        override val device: BleDevice
    ) : BleConnectionEvent(device)

    class Connected(
        override val device: BleDevice
    ) : BleConnectionEvent(device)

    class Disconnecting(
        override val device: BleDevice
    ) : BleConnectionEvent(device)

    class Disconnected(
        override val device: BleDevice
    ) : BleConnectionEvent(device)
}
