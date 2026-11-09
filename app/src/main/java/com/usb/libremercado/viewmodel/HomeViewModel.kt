package com.usb.libremercado.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductoRepository
) : ViewModel() {

    private val _productos = MutableStateFlow<Resource<List<Producto>>>(Resource.Loading())
    val productos = _productos.asStateFlow()

    private var productosJob: Job? = null

    fun getProductos(categoria: String? = null, query: String? = null) {
        productosJob?.cancel()
        productosJob = viewModelScope.launch {
            repository.getProductos(categoria, query).collect { nuevoEstado ->
                _productos.value = when (nuevoEstado) {
                    is Resource.Loading -> Resource.Loading(_productos.value.data)
                    is Resource.Error -> Resource.Error(nuevoEstado.message ?: "Error al cargar productos", _productos.value.data)
                    else -> nuevoEstado
                }
            }
        }
    }
}
