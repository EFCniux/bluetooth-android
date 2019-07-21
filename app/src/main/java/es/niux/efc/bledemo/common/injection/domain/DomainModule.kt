package es.niux.efc.bledemo.common.injection.domain

import dagger.Module
import es.niux.efc.bledemo.common.injection.domain.interactor.DomainInteractorModule

@Module(
    includes = [
        DomainInteractorModule::class
    ]
)
class DomainModule
