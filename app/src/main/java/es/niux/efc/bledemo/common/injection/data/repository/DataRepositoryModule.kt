package es.niux.efc.bledemo.common.injection.data.repository

import dagger.Module
import dagger.Provides
import es.niux.efc.bledemo.data.repositoy.DeviceRepository
import es.niux.efc.bledemo.data.repositoy.DeviceRepositoryImpl
import javax.inject.Singleton

@Module
class DataRepositoryModule {
    @Provides
    @Singleton
    fun deviceRepository(
        deviceRepository: DeviceRepositoryImpl
    ): DeviceRepository = deviceRepository
}
