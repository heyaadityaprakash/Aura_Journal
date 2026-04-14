package com.aadi.aurajournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel (private val authRepository: AuthRepository): ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    fun handleGGoogleLogin(){
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.signInWithGoogle()

            result.onSuccess {
                _loginSuccess.value = true
            }
            result.onFailure {
                error ->
                error.printStackTrace()  // add a snackbar to show failed state
            }
            _isLoading.value = false
        }
    }

    fun handleLogOut(){
        viewModelScope.launch {
            authRepository.signOut()
            _loginSuccess.value = false
        }
    }

    fun continueAsGuest(){
        authRepository.setGuestMode(true)
        _loginSuccess.value = true
    }
}