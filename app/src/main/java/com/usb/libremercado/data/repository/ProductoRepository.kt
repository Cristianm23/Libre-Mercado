package com.usb.libremercado.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.usb.libremercado.data.model.Producto
import com.usb.libremercado.data.model.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductoRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun getProductos(categoria: String? = null, query: String? = null): Flow<Resource<List<Producto>>> = callbackFlow {
        trySend(Resource.Loading())
        
        var baseQuery: Query = db.collection("productos").whereEqualTo("activo", true)
        
        if (!categoria.isNullOrBlank()) {
            baseQuery = baseQuery.whereEqualTo("categoria", categoria)
        }
        
        val listener = baseQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message ?: "Error al cargar productos"))
                return@addSnapshotListener
            }
            
            val productos = snapshot?.toObjects(Producto::class.java) ?: emptyList()
            val filtered = if (!query.isNullOrBlank()) {
                productos.filter { it.nombre.contains(query, ignoreCase = true) }
            } else {
                productos
            }
            trySend(Resource.Success(filtered))
        }
        awaitClose { listener.remove() }
    }

    suspend fun getProductoById(id: String): Resource<Producto> {
        return try {
            val doc = db.collection("productos").document(id).get().await()
            val producto = doc.toObject(Producto::class.java) ?: throw Exception("Producto no encontrado")
            Resource.Success(producto)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun publicarProducto(producto: Producto, imageUri: Uri?): Resource<String> {
        return try {
            val id = db.collection("productos").document().id
            var imageUrl = ""
            
            if (imageUri != null) {
                val ref = storage.reference.child("productos/$id/${UUID.randomUUID()}.jpg")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
            }
            
            val finalProducto = producto.copy(id = id, imagen = imageUrl)
            db.collection("productos").document(id).set(finalProducto).await()
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al publicar producto")
        }
    }

    suspend fun updateProducto(producto: Producto): Resource<Unit> {
        return try {
            db.collection("productos").document(producto.id).set(producto).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar producto")
        }
    }

    suspend fun getAllProductosAdmin(): Resource<List<Producto>> {
        return try {
            val snapshot = db.collection("productos").get().await()
            val productos = snapshot.toObjects(Producto::class.java)
            Resource.Success(productos)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener productos")
        }
    }

    suspend fun toggleProductoActivo(productoId: String, activo: Boolean): Resource<Unit> {
        return try {
            db.collection("productos").document(productoId).update("activo", activo).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al moderar producto")
        }
    }

    suspend fun deleteProducto(productoId: String): Resource<Unit> {
        return try {
            db.collection("productos").document(productoId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar producto")
        }
    }
}
