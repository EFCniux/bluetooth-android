package es.niux.efc.core.common.injection.qualifier

import javax.inject.Named
import javax.inject.Qualifier
import kotlin.reflect.KClass

/**
 * A Class based [Qualifier]
 *
 * @see [Named]
 */
@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class For(val value: KClass<*>)
