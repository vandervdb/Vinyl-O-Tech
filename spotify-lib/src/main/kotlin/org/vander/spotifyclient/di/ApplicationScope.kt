package org.vander.spotifyclient.di

import javax.inject.Qualifier

/**
 * A `CoroutineScope` that lives as long as the process, for work that outlives any screen —
 * the player controller's collectors, typically.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
