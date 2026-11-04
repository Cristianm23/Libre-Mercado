package com.usb.libremercado.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Mensaje
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])
    
    private val _mensajes = MutableStateFlow<Resource<List<Mensaje>>>(Resource.Loading())
    val mensajes = _mensajes.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.uid

    init {
        getMensajes()
    }

    private fun getMensajes() {
        viewModelScope.launch {
            repository.getMensajes(chatId).collect {
                _mensajes.value = it
            }
        }
    }

    fun enviarMensaje(contenido: String, receptorId: String) {
        val emisorId = currentUserId ?: return
        if (contenido.isBlank()) return

        val mensaje = Mensaje(
            chatId = chatId,
            emisorId = emisorId,
            receptorId = receptorId,
            contenido = contenido,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.enviarMensaje(mensaje)
        }
    }
}
