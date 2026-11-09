package com.usb.libremercado.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Chat
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

    private val _chat = MutableStateFlow<Resource<Chat>>(Resource.Loading())
    val chat = _chat.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError = _sendError.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.uid

    val otherUserId: String
        get() = (_chat.value as? Resource.Success)?.data?.let { chat ->
            when (currentUserId) {
                chat.compradorId -> chat.vendedorId
                chat.vendedorId -> chat.compradorId
                else -> if (chat.compradorId.isNotBlank() && chat.compradorId != currentUserId) chat.compradorId else chat.vendedorId
            }
        }.orEmpty()

    fun clearSendError() {
        _sendError.value = null
    }

    init {
        getChat()
        getMensajes()
    }

    private fun getChat() {
        viewModelScope.launch {
            val result = repository.getChatById(chatId)
            _chat.value = result
            if (result is Resource.Success) {
                currentUserId?.let { uid ->
                    repository.markAsRead(chatId, uid)
                }
            }
        }
    }

    private fun getMensajes() {
        viewModelScope.launch {
            repository.getMensajes(chatId).collect {
                _mensajes.value = it
            }
        }
    }

    fun enviarMensaje(contenido: String, receptorId: String, onComplete: () -> Unit) {
        val emisorId = currentUserId ?: return
        if (contenido.isBlank()) {
            onComplete()
            return
        }

        val recId = receptorId.ifBlank { otherUserId }

        val mensaje = Mensaje(
            chatId = chatId,
            emisorId = emisorId,
            receptorId = recId,
            contenido = contenido,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _sendError.value = null
            val result = repository.enviarMensaje(mensaje)
            if (result is Resource.Error) {
                _sendError.value = result.message ?: "No se pudo enviar el mensaje"
            }
            onComplete()
        }
    }
}
