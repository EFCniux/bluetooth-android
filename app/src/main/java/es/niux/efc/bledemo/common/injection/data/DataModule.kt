package es.niux.efc.bledemo.common.injection.data

import dagger.Module
import es.niux.efc.bledemo.common.injection.data.repository.DataRepositoryModule
import es.niux.efc.bledemo.common.injection.data.source.DataSourceModule

@Module(
    includes = [
        DataSourceModule::class,
        DataRepositoryModule::class
    ]
)
class DataModule
