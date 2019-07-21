package es.niux.efc.bledemo.common.injection.domain.interactor

import dagger.Module
import dagger.Provides
import es.niux.efc.bledemo.domain.interactor.*
import javax.inject.Singleton

@Module
class DomainInteractorModule {
    @Provides
    @Singleton
    fun bleAvailabilityInteractor(
        bleAvailabilityInteractor: BleAvailabilityInteractorImpl
    ): BleAvailabilityInteractor = bleAvailabilityInteractor

    @Provides
    @Singleton
    fun bleScanInteractor(
        bleScanInteractor: BleScanInteractorImpl
    ): BleScanInteractor = bleScanInteractor

    @Provides
    @Singleton
    fun bleDeviceInteractor(
        bleDeviceInteractor: BleDeviceInteractorImpl
    ): BleDeviceInteractor = bleDeviceInteractor
}
