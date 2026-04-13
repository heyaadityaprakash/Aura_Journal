package com.aadi.aurajournal

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest

class AuthRepository (private val context: Context){

//    Initializes Firebase Authentication
    private val firebaseAuth = FirebaseAuth.getInstance()
//    initializes Android Credential Manager
    private val credentialManager = CredentialManager.create(context)
//google OAuth id
    private val WEB_CLIENT_ID = "690457020645-v2inh46ir1ggu00fdv9pc0huqcd36gge.apps.googleusercontent.com"

//sign in with google
    suspend fun signInWithGoogle(): Result<Unit> {
        return try{
            //buidling google signing request
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) //show all acc
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(true) // auto signin returning users
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()


            //open account picker ui
            val result = credentialManager.getCredential(
                request= request,
                context = context
            )
//            extract credentials
            val credential = result.credential

//            extract token and pass to firebase
            if(credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL){

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken,null)


                //signin to firebase
                firebaseAuth.signInWithCredential(firebaseCredential).await()
                Result.success(Unit)
            }else{
                Result.failure(Exception("Unrecognized credential type"))
            }
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }


    suspend fun signOut(){
        firebaseAuth.signOut()
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
    // Quick check to see if the user is already logged in
    fun isUserSignedIn(): Boolean = firebaseAuth.currentUser != null

    // Get current user ID
    fun getUserId(): String? = firebaseAuth.currentUser?.uid
}

