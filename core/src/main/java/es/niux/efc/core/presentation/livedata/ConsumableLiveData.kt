@file:Suppress("unused")

package es.niux.efc.core.presentation.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData

class Consumable<T>(
    private val data: T,
    private val liveData: ConsumableLiveData<T>
) {
    fun peek(): T = data

    @MainThread
    fun consume(): T = data
        .also { liveData.mutate { remove(it) } }
}

open class ConsumableLiveData<T> : LiveData<List<Consumable<T>>>() {
    protected open val mutListValue: MutableList<T> = mutableListOf()
    val listValue: List<T> by lazy { mutListValue }

    @MainThread
    fun mutate(mutation: MutableList<T>.() -> Unit) {
        mutation(mutListValue)
        value = mutListValue.map { Consumable(it, this) }
    }
}
