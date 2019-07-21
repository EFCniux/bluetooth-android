package es.niux.efc.core.common.injection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.MapKey
import dagger.multibindings.ClassKey
import kotlin.reflect.KClass

/**
 * A [MapKey] annotation constrained to [ViewModel] classes.
 * @see [ClassKey]
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
