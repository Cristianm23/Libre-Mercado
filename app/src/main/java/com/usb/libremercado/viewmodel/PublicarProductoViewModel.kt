package com.usb.libremercado.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicarProductoViewModel @Inject constructor(
    private val repository: ProductoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _publicarState = MutableStateFlow<Resource<String>?>(null)
    val publicarState = _publicarState.asStateFlow()

    fun publicar(
        nombre: String,
        descripcion: String,
        precio: String,
        categoria: String,
        estado: String,
        cantidad: String,
        imageUris: List<Uri>
    ) {
        val vendedorId = authRepository.currentUser?.uid ?: return

        val cleanPrecio = precio.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0

        val producto = Producto(
            nombre = nombre,
            descripcion = descripcion,
            precio = cleanPrecio,
            categoria = categoria,
            estado = estado,
            cantidad = cantidad.toIntOrNull() ?: 0,
            vendedorId = vendedorId
        )
        
        viewModelScope.launch {
            _publicarState.value = Resource.Loading()
            _publicarState.value = repository.publicarProducto(producto, imageUris)
        }
    }
    
    fun resetState() {
        _publicarState.value = null
    }
}
