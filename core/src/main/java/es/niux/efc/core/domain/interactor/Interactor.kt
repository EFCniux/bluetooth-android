package es.niux.efc.core.domain.interactor

interface Interactor<in Input, out Output> {
    fun interact(input: Input): Output
}

/** @see [Interactor.interact] */
fun <Output> Interactor<Unit, Output>.interact(): Output =
    interact(Unit)
