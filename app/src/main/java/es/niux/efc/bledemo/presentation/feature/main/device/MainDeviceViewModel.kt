package es.niux.efc.bledemo.presentation.feature.main.device

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.polidea.rxandroidble2.exceptions.BleDisconnectedException
import com.polidea.rxandroidble2.exceptions.BleException
import com.polidea.rxandroidble2.exceptions.BleGattCallbackTimeoutException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import es.niux.efc.bledemo.domain.interactor.BleDeviceInteractor
import es.niux.efc.bluetooth.data.source.device.BleDevice
import es.niux.efc.bluetooth.data.source.device.BleService
import es.niux.efc.core.common.Schedulers
import es.niux.efc.core.common.injection.presentation.viewmodel.ViewModelStateFactory
import es.niux.efc.core.presentation.livedata.toPublisher
import es.niux.efc.core.presentation.viewmodel.BaseViewModel
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import java.io.Serializable
import java.util.*

/**
 * todo change to [Parcelable]
 */
data class DeviceUiData(
    val isLoading: Boolean,
    val isConnected: Boolean,
    val device: Device?,
    val services: List<Service>?
) : Serializable {
    companion object {
        val empty = DeviceUiData(
            isLoading = false,
            isConnected = false,
            device = null,
            services = null
        )
    }

    data class Device(
        val address: String,
        val name: String,
        val model: String?,
        val manufacturer: String?,
        val serial: String?
    ) : Serializable

    data class Service(
        val id: UUID,
        val idInstance: Int,
        val isPrimary: Boolean,
        val characteristics: List<Characteristic>
    ) : Serializable

    data class Characteristic(
        val id: UUID,
        val idInstance: Int,
        val descriptors: List<Descriptor>
    ) : Serializable

    data class Descriptor(
        val id: UUID
    ) : Serializable
}

abstract class MainDeviceViewModel(
    savedState: SavedStateHandle
) : BaseViewModel(savedState) {
    companion object {
        const val ARG_BLE_DEVICE_ADDRESS = "arg_bleDeviceAddress"
        const val STATE_DEVICE_UI = "state_deviceUI"
    }

    abstract val deviceUiData: LiveData<DeviceUiData>
}

class MainDeviceViewModelImpl @AssistedInject constructor(
    @Assisted arg0: SavedStateHandle,
    private val schedulers: Schedulers,
    private val bleDeviceInteractor: BleDeviceInteractor
) : MainDeviceViewModel(arg0) {
    @AssistedInject.Factory
    interface Factory : ViewModelStateFactory<MainDeviceViewModelImpl>

    private val bleDeviceAddress: LiveData<String>  by lazy {
        savedState.getLiveData<String>(ARG_BLE_DEVICE_ADDRESS)
    }

    override val deviceUiData: LiveData<DeviceUiData> by lazy {
        savedState.getLiveData<DeviceUiData>(STATE_DEVICE_UI)
            .apply {
                bleDeviceAddress
                    .toPublisher(schedulers.main)
                    .observeOn(schedulers.io)
                    .firstOrError()
                    .flatMap { bleDeviceInteractor.interact(it) }
                    .flatMapObservable { device ->
                        device
                            .connect()
                            .flatMap<DeviceAsyncData> { connection ->
                                val servicesSingle = connection
                                    .services()
                                    .cache()

                                return@flatMap Observable
                                    .mergeArray(
                                        servicesSingle
                                            .flatMap { services ->
                                                val gapServiceSingle = Single
                                                    .fromCallable {
                                                        services
                                                            .first { it.id == BleDevice.GAP_SERVICE }
                                                    }
                                                    .cache()

                                                val devInfoMaybe = Maybe
                                                    .fromCallable {
                                                        services
                                                            .firstOrNull { it.id == BleDevice.DEV_INFO_SERVICE }
                                                    }
                                                    .cache()

                                                @Suppress("LABEL_NAME_CLASH")
                                                return@flatMap Single
                                                    .zip(listOf<Single<String>>(
                                                        gapServiceSingle
                                                            .flatMap { service ->
                                                                connection
                                                                    .read(service
                                                                        .characteristics
                                                                        .first { it.id == BleDevice.GAP_NAME_CHARACTERISTIC }
                                                                    )
                                                            }
                                                            .map { it.toString(Charsets.UTF_8) },
                                                        devInfoMaybe
                                                            .flatMap { service ->
                                                                Maybe
                                                                    .fromCallable {
                                                                        service
                                                                            .characteristics
                                                                            .firstOrNull { it.id == BleDevice.DEV_INFO_MODEL_CHARACTERISTIC }
                                                                    }
                                                                    .flatMapSingleElement { characteristic ->
                                                                        connection
                                                                            .read(characteristic)
                                                                    }
                                                            }
                                                            .map { it.toString(Charsets.UTF_8) }
                                                            .toSingle(""),
                                                        devInfoMaybe
                                                            .flatMap { service ->
                                                                Maybe
                                                                    .fromCallable {
                                                                        service
                                                                            .characteristics
                                                                            .firstOrNull { it.id == BleDevice.DEV_INFO_MANUFACTURER_CHARACTERISTIC }
                                                                    }
                                                                    .flatMapSingleElement { characteristic ->
                                                                        connection
                                                                            .read(characteristic)
                                                                    }
                                                            }
                                                            .map { it.toString(Charsets.UTF_8) }
                                                            .toSingle(""),
                                                        devInfoMaybe
                                                            .flatMap { service ->
                                                                Maybe
                                                                    .fromCallable {
                                                                        service
                                                                            .characteristics
                                                                            .firstOrNull { it.id == BleDevice.DEV_INFO_SERIAL_CHARACTERISTIC }
                                                                    }
                                                                    .flatMapSingleElement { characteristic ->
                                                                        connection
                                                                            .read(characteristic)
                                                                    }
                                                            }
                                                            .map { it.toString(Charsets.UTF_8) }
                                                            .toSingle("")
                                                    )) { strings: Array<Any> ->
                                                        return@zip DeviceAsyncData.Device(
                                                            isLoading = false,
                                                            address = connection.device.address,
                                                            name = strings[0] as String,
                                                            model = (strings[1] as String)
                                                                .let { if (it.isNotEmpty()) it else null },
                                                            manufacturer = (strings[2] as String)
                                                                .let { if (it.isNotEmpty()) it else null },
                                                            serial = (strings[3] as String)
                                                                .let { if (it.isNotEmpty()) it else null }
                                                        )
                                                    }
                                            }
                                            .toObservable()
                                            .startWith(
                                                DeviceAsyncData.Device(isLoading = true)
                                            ),
                                        servicesSingle
                                            .map<DeviceAsyncData> {
                                                DeviceAsyncData.Services(isLoading = false, services = it)
                                            }
                                            .toObservable()
                                            .startWith(
                                                DeviceAsyncData.Services(isLoading = true)
                                            )
                                    )
                                    .startWith(
                                        DeviceAsyncData.Connection(isLoading = false)
                                    )
                            }
                            .startWith(
                                DeviceAsyncData.Connection(isLoading = true)
                            )
                            .onErrorResumeNext { e: Throwable ->
                                return@onErrorResumeNext when (e) {
                                    is BleDisconnectedException -> Observable
                                        .just(DeviceAsyncData.Connection(exception = e))
                                    is BleGattCallbackTimeoutException -> Observable
                                        .just(DeviceAsyncData.Connection(exception = e))
                                    else -> Observable
                                        .error(e)
                                }
                            }
                    }
                    .scan(Pair(0, DeviceUiData.empty)) { data: Pair<Int, DeviceUiData>, event: DeviceAsyncData ->
                        val loading = data.first + if (event.isLoading) +1 else -1
                        return@scan when (event) {
                            is DeviceAsyncData.Connection -> when {
                                event.exception != null -> Pair(
                                    0, data.second.copy(
                                        isLoading = false,
                                        isConnected = false
                                    )
                                )
                                else -> Pair(
                                    loading, data.second.copy(
                                        isLoading = loading != 0,
                                        isConnected = !event.isLoading
                                    )
                                )
                            }
                            is DeviceAsyncData.Device -> Pair(
                                loading, data.second.copy(
                                    isLoading = loading != 0,
                                    device = event
                                        .let { device ->
                                            return@let if (device.isLoading)
                                                null
                                            else
                                                DeviceUiData.Device(
                                                    address = device.address!!,
                                                    name = device.name!!,
                                                    model = device.model,
                                                    manufacturer = device.manufacturer,
                                                    serial = device.serial
                                                )
                                        }
                                )
                            )
                            is DeviceAsyncData.Services -> Pair(
                                loading, data.second.copy(
                                    isLoading = loading != 0,
                                    services = event
                                        .services
                                        ?.map { service ->
                                            DeviceUiData.Service(
                                                service.id,
                                                service.idInstance,
                                                service.isPrimary,
                                                service.characteristics
                                                    .map { characteristic ->
                                                        DeviceUiData.Characteristic(
                                                            characteristic.id,
                                                            characteristic.idInstance,
                                                            characteristic.descriptors
                                                                .map { descriptor ->
                                                                    DeviceUiData.Descriptor(
                                                                        descriptor.id
                                                                    )
                                                                }
                                                        )
                                                    }
                                            )
                                        }
                                )
                            )
                        }
                    }
                    .map { it.second }
                    .observeOn(schedulers.main)
                    .subscribe { value = it }
                    .disposeOnClear()
            }
    }
}

internal sealed class DeviceAsyncData(
    open val isLoading: Boolean
) {
    data class Connection(
        override val isLoading: Boolean = false,
        val exception: BleException? = null
    ) : DeviceAsyncData(isLoading)

    data class Device(
        override val isLoading: Boolean = false,
        val address: String? = null,
        val name: String? = null,
        val model: String? = null,
        val manufacturer: String? = null,
        val serial: String? = null
    ) : DeviceAsyncData(isLoading)

    data class Services(
        override val isLoading: Boolean = false,
        val services: List<BleService>? = null
    ) : DeviceAsyncData(isLoading)
}
