package com.usb.libremercado.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Chat
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListaChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<Resource<List<Chat>>>(Resource.Loading())
    val chats = _chats.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.uid

    init {
        cargarChats()
    }

    fun cargarChats() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            chatRepository.getChats(uid).collect {
                _chats.value = it
            }
        }
    }
}
