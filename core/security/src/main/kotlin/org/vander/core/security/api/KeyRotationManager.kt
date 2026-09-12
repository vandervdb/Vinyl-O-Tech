package org.vander.core.security.api

/**
 * Renews the data key so that a compromised key has a bounded window of usefulness.
 *
 * No implementation exists yet. [rotateIfNeeded] is the one called on a normal path and
 * decides on its own whether rotation is due; [forceRotate] rotates unconditionally, for
 * a logout or a suspected compromise.
 */
interface KeyRotationManager {
    suspend fun rotateIfNeeded()

    suspend fun forceRotate()
}
