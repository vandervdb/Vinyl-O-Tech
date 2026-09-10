package org.vander.core.security.api

interface KeyRotationManager {
    suspend fun rotateIfNeeded()

    suspend fun forceRotate()
}
