package es.niux.efc.bledemo.common.injection

import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module

@AssistedModule
@Module(
    includes = [
        AssistedInject_ApplicationAssistedModule::class
    ]
)
abstract class ApplicationAssistedModule
