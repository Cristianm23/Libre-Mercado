package com.usb.libremercado.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.model.Usuario
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<Resource<List<Usuario>>>(Resource.Loading())
    val usuarios = _usuarios.asStateFlow()

    private val _productos = MutableStateFlow<Resource<List<Producto>>>(Resource.Loading())
    val productos = _productos.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _usuarios.value = Resource.Loading()
            _usuarios.value = authRepository.getUsuarios()
            
            _productos.value = Resource.Loading()
            _productos.value = productoRepository.getAllProductosAdmin()
        }
    }

    fun toggleProducto(productoId: String, activo: Boolean) {
        viewModelScope.launch {
            productoRepository.toggleProductoActivo(productoId, activo)
            _productos.value = productoRepository.getAllProductosAdmin()
        }
    }
}
