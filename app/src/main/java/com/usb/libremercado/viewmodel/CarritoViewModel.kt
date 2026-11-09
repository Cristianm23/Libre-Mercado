package com.usb.libremercado.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.CarritoItem
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.CarritoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarritoViewModel @Inject constructor(
    private val repository: CarritoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _items = MutableStateFlow<Resource<List<CarritoItem>>>(Resource.Loading())
    val items = _items.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.uid

    init {
        cargarCarrito()
    }

    fun cargarCarrito() {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            repository.getItems(uid).collect {
                _items.value = it
            }
        }
    }

    fun agregarProducto(producto: Producto) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val item = CarritoItem(
                productoId = producto.id,
                nombre = producto.nombre,
                precio = producto.precio,
                cantidad = 1,
                imagen = producto.imagen,
                vendedorId = producto.vendedorId
            )
            repository.addItem(uid, item)
        }
    }

    fun eliminarItem(productoId: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            repository.removeItem(uid, productoId)
        }
    }

    fun vaciarCarrito(onComplete: () -> Unit) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            repository.clearCarrito(uid)
            onComplete()
        }
    }
}
