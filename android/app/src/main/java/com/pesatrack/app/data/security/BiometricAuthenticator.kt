package com.pesatrack.app.data.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

private const val ALLOWED_AUTHENTICATORS = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

enum class AuthResult { SUCCESS, CANCELLED, UNAVAILABLE, FAILED }

// Wraps BiometricPrompt with device-credential fallback (PIN/pattern/password),
// so it works the same whether the device has biometrics enrolled or not --
// canAuthenticate/authenticate only fail if the device has no secure lock
// screen configured at all.
class BiometricAuthenticator(private val activity: FragmentActivity) {

    fun isAvailable(): Boolean =
        BiometricManager.from(activity).canAuthenticate(ALLOWED_AUTHENTICATORS) ==
            BiometricManager.BIOMETRIC_SUCCESS

    suspend fun authenticate(title: String, subtitle: String? = null): AuthResult {
        if (!isAvailable()) return AuthResult.UNAVAILABLE

        return suspendCancellableCoroutine { continuation ->
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .apply { subtitle?.let { setSubtitle(it) } }
                // No setNegativeButtonText() -- disallowed once DEVICE_CREDENTIAL is
                // among the allowed authenticators, since the system prompt already
                // provides its own way out (a system Cancel/dismiss affordance).
                .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
                .build()

            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) continuation.resume(AuthResult.SUCCESS)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (!continuation.isActive) return
                        val cancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == BiometricPrompt.ERROR_CANCELED
                        continuation.resume(if (cancelled) AuthResult.CANCELLED else AuthResult.FAILED)
                    }

                    override fun onAuthenticationFailed() {
                        // One failed attempt (e.g. an unrecognised fingerprint) -- the
                        // system prompt stays open and lets the user retry on its own.
                    }
                }
            )

            prompt.authenticate(promptInfo)
            continuation.invokeOnCancellation { prompt.cancelAuthentication() }
        }
    }
}

tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
