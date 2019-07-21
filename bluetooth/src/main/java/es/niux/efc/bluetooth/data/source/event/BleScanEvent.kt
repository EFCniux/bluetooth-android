package es.niux.efc.bluetooth.data.source.event

import com.polidea.rxandroidble2.scan.ScanRecord
import es.niux.efc.bluetooth.data.source.device.BleDevice

sealed class BleScanEvent(
    /** Timestamp in nanoseconds */
    open val time: Long,
    /**
     * Received Signal Strength Indicator.
     *
     * The relative quality of the received signal, without an absolute value,
     * but the closer to 0 the better it is.
     */
    open val rssi: Int,
    open val record: ScanRecord,
    open val device: BleDevice
) {
    data class Found(
        override val time: Long,
        override val rssi: Int,
        override val record: ScanRecord,
        override val device: BleDevice
    ) : BleScanEvent(
        time, rssi, record, device
    )

    data class Lost(
        override val time: Long,
        override val rssi: Int,
        override val record: ScanRecord,
        override val device: BleDevice
    ) : BleScanEvent(
        time, rssi, record, device
    )
}
