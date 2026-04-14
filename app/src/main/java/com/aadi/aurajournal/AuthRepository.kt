package com.aadi.aurajournal

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    
    // google OAuth id
    private val WEB_CLIENT_ID = "690457020645-v2inh46ir1ggu00fdv9pc0huqcd36gge.apps.googleusercontent.com"


    //guest users (offiline)
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Show all accounts
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false) // Disable auto-select to force account picker for testing
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("AuthRepository", "Requesting credentials...")
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            Log.d("AuthRepository", "Credential received: ${credential.type}")

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential =
                    GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

                firebaseAuth.signInWithCredential(firebaseCredential).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Unrecognized credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthRepository", "Credential Manager error: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            setGuestMode(false) //clear guest mode on signout
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to clear credential state", e)
        }
    }

    fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null
    fun getUserId(): String? = firebaseAuth.currentUser?.uid

//    saave that the user chose offline mode
    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean("is_guest_mode", isGuest).apply()
    }
    //fun to let user access as guest
    fun isGuestMode(): Boolean {
        return prefs.getBoolean("guest_mode", false)
    }


}
