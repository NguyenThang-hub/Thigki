package com.example.thigki.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thigki.data.AuthRepository
import com.example.thigki.data.RoleRepository
import com.example.thigki.data.authStateFlow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val roleRepository: RoleRepository = RoleRepository(),
) : ViewModel() {

    val currentUser = auth.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), auth.currentUser)

    val role = currentUser.flatMapLatest { u ->
        if (u == null) flowOf<String?>(null)
        else roleRepository.observeRole(u.uid).map { it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiMessage = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    fun clearMessage() {
        uiMessage.value = null
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.signIn(email, password)
                uiMessage.value = null
            } catch (e: Exception) {
                uiMessage.value = e.message ?: "Đăng nhập thất bại"
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.signUp(email, password)
                uiMessage.value = null
            } catch (e: Exception) {
                uiMessage.value = e.message ?: "Đăng ký thất bại"
            }
        }
    }
}
