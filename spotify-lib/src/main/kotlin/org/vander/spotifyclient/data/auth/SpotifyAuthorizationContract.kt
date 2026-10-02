package org.vander.spotifyclient.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.vander.core.domain.auth.AuthorizationOptions
import org.vander.core.domain.auth.AuthorizationOutcome
import org.vander.spotifyclient.domain.auth.SpotifyAuthConfig

class SpotifyAuthorizationContract(
    private val config: SpotifyAuthConfig,
) : ActivityResultContract<AuthorizationOptions, AuthorizationOutcome>() {
    override fun createIntent(
        context: Context,
        input: AuthorizationOptions,
    ): Intent {
        val activity =
            context as? Activity ?: error("SpotifyAuthorizationContract must be launched from an Activity")
        val request =
            AuthorizationRequest
                .Builder(config.clientId, AuthorizationResponse.Type.CODE, config.redirectUri)
                .setScopes(SpotifyScope.required)
                .setShowDialog(input.forceApprovalDialog)
                .build()
        return AuthorizationClient.createLoginActivityIntent(activity, request)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): AuthorizationOutcome {
        if (resultCode == Activity.RESULT_CANCELED) return AuthorizationOutcome.Cancelled
        val response = AuthorizationClient.getResponse(resultCode, intent)
        return toOutcome(response.type, response.code, response.error)
    }

    fun toOutcome(
        type: AuthorizationResponse.Type,
        code: String?,
        error: String?,
    ): AuthorizationOutcome =
        when (type) {
            AuthorizationResponse.Type.CODE -> AuthorizationOutcome.Code(code ?: "")
            AuthorizationResponse.Type.ERROR -> AuthorizationOutcome.Failed(error ?: "")
            else -> AuthorizationOutcome.Failed("Unknown error")
        }
}
