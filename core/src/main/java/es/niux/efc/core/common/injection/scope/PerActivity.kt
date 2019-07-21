package es.niux.efc.core.common.injection.scope

import android.app.Activity
import javax.inject.Scope

/**
 * A dagger2 scoping annotation for those objects whose lifetime should be that of an [Activity].
 */
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity
