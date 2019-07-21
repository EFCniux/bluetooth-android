package es.niux.efc.core.domain.interactor.reactivex

import es.niux.efc.core.domain.interactor.Interactor
import io.reactivex.*

interface CompletableInteractor<in Input> : Interactor<Input, Completable>

interface SingleInteractor<in Input, Output> : Interactor<Input, Single<Output>>

interface MaybeInteractor<in Input, Output> : Interactor<Input, Maybe<Output>>

interface ObservableInteractor<in Input, Output> : Interactor<Input, Observable<Output>>

interface FlowableInteractor<in Input, Output> : Interactor<Input, Flowable<Output>>
