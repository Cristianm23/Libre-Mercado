package com.usb.libremercado.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.usb.libremercado.data.model.Calificacion
import com.usb.libremercado.data.model.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalificacionRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun agregarCalificacion(calificacion: Calificacion): Resource<Unit> {
        return try {
            val id = db.collection("calificaciones").document().id
            val finalCalificacion = calificacion.copy(id = id)
            db.collection("calificaciones").document(id).set(finalCalificacion).await()
            
            // Actualizar promedio del producto
            actualizarPromedioProducto(calificacion.productoId)
            // Actualizar promedio del vendedor
            actualizarPromedioVendedor(calificacion.vendedorId)
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al calificar")
        }
    }

    private suspend fun actualizarPromedioProducto(productoId: String) {
        val query = db.collection("calificaciones").whereEqualTo("productoId", productoId).get().await()
        if (!query.isEmpty) {
            val total = query.documents.size
            val suma = query.documents.sumOf { it.getLong("estrellas")?.toDouble() ?: 0.0 }
            val promedio = suma / total
            db.collection("productos").document(productoId).update("calificacion", promedio).await()
        }
    }

    private suspend fun actualizarPromedioVendedor(vendedorId: String) {
        val query = db.collection("calificaciones").whereEqualTo("vendedorId", vendedorId).get().await()
        if (!query.isEmpty) {
            val total = query.documents.size
            val suma = query.documents.sumOf { it.getLong("estrellas")?.toDouble() ?: 0.0 }
            val promedio = suma / total
            db.collection("usuarios").document(vendedorId).update("calificacionPromedio", promedio).await()
        }
    }

    suspend fun getCalificacionesProducto(productoId: String): Resource<List<Calificacion>> {
        return try {
            val query = db.collection("calificaciones").whereEqualTo("productoId", productoId).get().await()
            val lista = query.toObjects(Calificacion::class.java)
            Resource.Success(lista)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener calificaciones")
        }
    }
}
