package com.usb.libremercado.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usb.libremercado.data.model.Calificacion
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.repository.AuthRepository
import com.usb.libremercado.data.repository.CalificacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalificacionViewModel @Inject constructor(
    private val repository: CalificacionRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val productoId: String = checkNotNull(savedStateHandle["productoId"])
    val vendedorId: String = checkNotNull(savedStateHandle["vendedorId"])

    private val _calificacionState = MutableStateFlow<Resource<Unit>?>(null)
    val calificacionState = _calificacionState.asStateFlow()

    val currentUserId get() = authRepository.currentUser?.uid

    fun calificar(estrellas: Int, comentario: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _calificacionState.value = Resource.Loading()
            val calificacion = Calificacion(
                productoId = productoId,
                usuarioId = uid,
                vendedorId = vendedorId,
                estrellas = estrellas,
                comentario = comentario
            )
            _calificacionState.value = repository.agregarCalificacion(calificacion)
        }
    }
}
