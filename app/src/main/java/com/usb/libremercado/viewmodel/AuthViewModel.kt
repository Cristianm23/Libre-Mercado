package com.usb.libremercado.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.model.Usuario
import com.usb.libremercado.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Usuario>?>(null)
    val authState = _authState.asStateFlow()

    private val _userData = MutableStateFlow<Resource<Usuario>>(Resource.Loading())
    val userData = _userData.asStateFlow()

    val currentUser get() = repository.currentUser

    init {
        fetchUserData()
    }

    fun fetchUserData() {
        currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                _userData.value = repository.getUsuario(uid)
            }
        }
    }

    fun login(correo: String, contrasena: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.login(correo, contrasena)
            _authState.value = result
            if (result is Resource.Success) {
                fetchUserData()
            }
        }
    }

    fun registro(nombre: String, correo: String, contrasena: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.registro(nombre, correo, contrasena)
            _authState.value = result
            if (result is Resource.Success) {
                fetchUserData()
            }
        }
    }

    fun logout() {
        repository.logout()
        _authState.value = null
    }

    fun resetState() {
        _authState.value = null
    }
}
