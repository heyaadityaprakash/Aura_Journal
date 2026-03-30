package com.aadi.aurajournal.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

fun authenticateWithBiometrics(
    context: Context,
    title: String="Unlock Journal",
    onSuccess:()->Unit,
    onError:()->Unit

){
    val activity=context.findActivity()?:return
    val executor= ContextCompat.getMainExecutor(activity)
    val biometricPrompt= BiometricPrompt(activity,executor,
        object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError()
            }
        }
    )

    val promptInfo= BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    biometricPrompt.authenticate(promptInfo)

}