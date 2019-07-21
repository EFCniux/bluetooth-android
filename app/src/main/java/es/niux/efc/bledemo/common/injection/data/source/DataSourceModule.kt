package es.niux.efc.bledemo.common.injection.data.source

import android.content.Context
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import dagger.Module
import dagger.Provides
import es.niux.efc.bledemo.BuildConfig
import es.niux.efc.bluetooth.data.source.BleConnectionImpl
import es.niux.efc.bluetooth.data.source.BleSource
import es.niux.efc.bluetooth.data.source.BleSourceImpl
import es.niux.efc.bluetooth.data.source.device.BleCharacteristicImpl
import es.niux.efc.bluetooth.data.source.device.BleDescriptorImpl
import es.niux.efc.bluetooth.data.source.device.BleDeviceImpl
import es.niux.efc.bluetooth.data.source.device.BleServiceImpl
import es.niux.efc.core.common.Schedulers
import timber.log.Timber
import javax.inject.Singleton

@Module
class DataSourceModule {
    @Provides
    @Singleton
    fun rxBleClient(
        context: Context
    ): RxBleClient {
        RxBleClient
            .updateLogOptions(
                LogOptions.Builder()
                    .setLogLevel(
                        when {
                            BuildConfig.DEBUG -> LogConstants
                                .VERBOSE
                            else -> LogConstants
                                .INFO
                        }
                    )
                    .setShouldLogAttributeValues(true)
                    .setLogger { level: Int, tag: String, msg: String ->
                        when (level) {
                            LogConstants.VERBOSE -> Timber
                                .tag(tag).v(msg)
                            LogConstants.DEBUG -> Timber
                                .tag(tag).d(msg)
                            LogConstants.INFO -> Timber
                                .tag(tag).i(msg)
                            LogConstants.WARN -> Timber
                                .tag(tag).w(msg)
                            LogConstants.ERROR -> Timber
                                .tag(tag).e(msg)
                        }
                    }
                    .build()
            )
        return RxBleClient
            .create(context)
    }

    @Provides
    @Singleton
    fun bleSource(
        rxBleClient: RxBleClient,
        schedulers: Schedulers
    ): BleSource = BleSourceImpl(
        rxBleClient,
        schedulers.io
    ) { rxDevice ->
        BleDeviceImpl(
            rxDevice,
            schedulers.io
        ) { device, rxConnection ->
            BleConnectionImpl(
                device,
                rxConnection,
                schedulers.io
            ) { connection, rawService ->
                BleServiceImpl(
                    connection.device,
                    rawService
                ) { service, rawCharacteristic ->
                    BleCharacteristicImpl(
                        service,
                        rawCharacteristic
                    ) { characteristic, rawDescriptor ->
                        BleDescriptorImpl(
                            characteristic,
                            rawDescriptor
                        )
                    }
                }
            }
        }
    }
}
