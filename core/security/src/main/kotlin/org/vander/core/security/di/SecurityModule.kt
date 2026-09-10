package org.vander.core.security.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private val Context.securityDataStore by preferencesDataStore(
    name = "security_store",
)

class SecurityModule
