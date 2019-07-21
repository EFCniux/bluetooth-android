package es.niux.efc.core.common.injection.scope

import androidx.fragment.app.Fragment
import javax.inject.Scope

/**
 * A dagger2 scoping annotation for those objects whose lifetime should be that of a [Fragment].
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerFragment
