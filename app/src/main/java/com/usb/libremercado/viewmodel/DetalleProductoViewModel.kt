package com.usb.libremercado.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.model.Usuario
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.CarritoRepository
import com.usb.libremercado.data.repository.ChatRepository
import com.usb.libremercado.data.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetalleProductoViewModel @Inject constructor(
    private val productoRepository: ProductoRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val carritoRepository: CarritoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val productoId: String = checkNotNull(savedStateHandle["productoId"])
    
    private val _producto = MutableStateFlow<Resource<Producto>>(Resource.Loading())
    val producto = _producto.asStateFlow()

    private val _vendedor = MutableStateFlow<Resource<Usuario>>(Resource.Loading())
    val vendedor = _vendedor.asStateFlow()

    private val _chatId = MutableStateFlow<Resource<String>?>(null)
    val chatId = _chatId.asStateFlow()

    init {
        getProducto()
    }

    private fun getProducto() {
        viewModelScope.launch {
            _producto.value = Resource.Loading()
            val result = productoRepository.getProductoById(productoId)
            _producto.value = result
            if (result is Resource.Success) {
                result.data?.vendedorId?.let { getVendedor(it) }
            }
        }
    }

    private fun getVendedor(vendedorId: String) {
        viewModelScope.launch {
            _vendedor.value = authRepository.getUsuario(vendedorId)
        }
    }

    fun contactarVendedor(compradorId: String, vendedorId: String) {
        viewModelScope.launch {
            _chatId.value = Resource.Loading()
            _chatId.value = chatRepository.createOrGetChat(compradorId, vendedorId, productoId)
        }
    }
    
    fun resetChatState() {
        _chatId.value = null
    }

    val currentUserId get() = authRepository.currentUser?.uid
}
