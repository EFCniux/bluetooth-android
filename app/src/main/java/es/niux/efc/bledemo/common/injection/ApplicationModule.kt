package es.niux.efc.bledemo.common.injection

import android.content.Context
import dagger.Module
import dagger.Provides
import es.niux.efc.bledemo.TheApplication
import es.niux.efc.bledemo.common.injection.data.DataModule
import es.niux.efc.bledemo.common.injection.domain.DomainModule
import es.niux.efc.core.common.Schedulers
import es.niux.efc.core.common.SchedulersImpl
import javax.inject.Singleton

@Module(
    includes = [
        DataModule::class,
        DomainModule::class
    ]
)
class ApplicationModule {
    @Provides
    @Singleton
    fun applicationContext(
        application: TheApplication
    ): Context = application

    @Provides
    @Singleton
    fun applicationSchedulers(): Schedulers =
        SchedulersImpl()
}
