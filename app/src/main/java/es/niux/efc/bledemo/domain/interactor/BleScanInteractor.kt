package es.niux.efc.bledemo.domain.interactor

import com.polidea.rxandroidble2.exceptions.BleScanException
import es.niux.efc.bledemo.data.repositoy.DeviceRepository
import es.niux.efc.bluetooth.data.source.BleSource
import es.niux.efc.bluetooth.data.source.event.BleAvailabilityEvent
import es.niux.efc.bluetooth.data.source.event.BleScanEvent
import es.niux.efc.core.common.Schedulers
import es.niux.efc.core.domain.interactor.reactivex.ObservableInteractor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface BleScanInteractor : ObservableInteractor<Unit, List<BleScanEvent.Found>>

class BleScanInteractorImpl @Inject constructor(
    schedulers: Schedulers,
    deviceRepository: DeviceRepository
) : BleScanInteractor {
    private val interaction: Observable<List<BleScanEvent.Found>> by lazy {
        deviceRepository
            .observeBleAvailability()
            .distinctUntilChanged { previous, current ->
                return@distinctUntilChanged when (previous) {
                    is BleAvailabilityEvent.Available -> when (current) {
                        is BleAvailabilityEvent.Available -> true
                        else -> false
                    }
                    is BleAvailabilityEvent.Unavailable -> when (current) {
                        is BleAvailabilityEvent.Unavailable -> true
                        else -> false
                    }
                }
            }
            .switchMap { bleAvailabilityEvent ->
                return@switchMap when (bleAvailabilityEvent) {
                    is BleAvailabilityEvent.Unavailable -> Observable
                        .just(emptyList())
                    is BleAvailabilityEvent.Available -> deviceRepository
                        .scanBleDevices(BleSource.ScanMode.MEDIUM)
                        .subscribeOn(schedulers.io)
                        .doOnSubscribe { Timber.i("Scan operation start") }
                        .doOnDispose { Timber.i("Scan operation stop") }
                        .doOnError { e: Throwable ->
                            when {
                                e is BleScanException && e.reason == BleScanException.UNDOCUMENTED_SCAN_THROTTLE ->
                                    Timber.w("Scan operation stop:\n%s", e.message)
                                else ->
                                    Timber.e(e, "Scan operation stop")
                            }
                        }
                        /** Retry on [BleScanException.UNDOCUMENTED_SCAN_THROTTLE] occurrences after the suggested time */
                        .retryWhen { errObservable: Observable<Throwable> ->
                            errObservable
                                .flatMapSingle<Any> { e: Throwable ->
                                    return@flatMapSingle when {
                                        e is BleScanException && e.reason == BleScanException.UNDOCUMENTED_SCAN_THROTTLE -> Single
                                            // The extra millis seem to help to not immediately trigger again
                                            .just((e.retryDateSuggestion!!.time - System.currentTimeMillis()) + 100)
                                            .doOnSuccess { Timber.w("Retrying in %s milliseconds", it) }
                                            .flatMap { Single.timer(it, TimeUnit.MILLISECONDS) }
                                            .doOnSuccess { Timber.w("Retrying now") }
                                        else -> Single
                                            .error(e)
                                    }
                                }
                        }
                        .scan(mutableListOf()) { container: MutableList<BleScanEvent.Found>, item: BleScanEvent ->
                            when (item) {
                                is BleScanEvent.Found ->
                                    container.add(item)
                                is BleScanEvent.Lost ->
                                    container.removeAll { it.device.address == item.device.address }
                            }
                            return@scan container
                        }
                        .map { it.toList() }
                }
            }
            .publish()
            /**
             * Do not dispose immediately, so that the subscription, is maintained through quick
             * observer disposal (for example, activity/fragment recreation), in an attempt to mitigate
             * [BleScanException.UNDOCUMENTED_SCAN_THROTTLE] occurrences.
             */
            .refCount(2, TimeUnit.SECONDS)
    }

    override fun interact(input: Unit): Observable<List<BleScanEvent.Found>> =
        interaction
}
