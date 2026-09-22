package org.vander.core.security.di

import javax.inject.Qualifier

/**
 * The Android-Keystore-backed [com.google.crypto.tink.Aead] used as the master key (KEK).
 *
 * It only ever encrypts the data keyset, never app data — qualifying it keeps that role
 * explicit at every injection point, and leaves room for a second `Aead` in the graph.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MasterKeyAead
