package com.usb.libremercado.data.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val tipoUsuario: String = "usuario", // "usuario" | "administrador"
    val calificacionPromedio: Double = 0.0, // como vendedor
    val fechaRegistro: Long = System.currentTimeMillis()
)

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val estado: String = "nuevo", // "nuevo" | "usado"
    val cantidad: Int = 0,
    val imagen: String = "", // URL de Firebase Storage
    val vendedorId: String = "",
    val calificacion: Double = 0.0,
    val activo: Boolean = true // permite moderación/desactivación por admin
)

data class CarritoItem(
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val imagen: String = "",
    val vendedorId: String = ""
)

data class Calificacion(
    val id: String = "",
    val productoId: String = "",
    val usuarioId: String = "",
    val vendedorId: String = "",
    val estrellas: Int = 0, // 1 a 5
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis()
)

data class Chat(
    val id: String = "",
    val compradorId: String = "",
    val vendedorId: String = "",
    val productoId: String = "",
    val ultimoMensaje: String = "",
    val ultimaActualizacion: Long = System.currentTimeMillis()
)

data class Mensaje(
    val id: String = "",
    val chatId: String = "",
    val emisorId: String = "",
    val receptorId: String = "",
    val contenido: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val esIA: Boolean = false
)

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
