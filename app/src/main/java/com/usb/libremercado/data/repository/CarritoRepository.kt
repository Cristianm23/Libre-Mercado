package com.usb.libremercado.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.usb.libremercado.data.model.CarritoItem
import com.usb.libremercado.data.model.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarritoRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getItems(uid: String): Flow<Resource<List<CarritoItem>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = db.collection("carritos").document(uid).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al cargar carrito"))
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(CarritoItem::class.java) ?: emptyList()
                trySend(Resource.Success(items))
            }
        awaitClose { listener.remove() }
    }

    suspend fun addItem(uid: String, item: CarritoItem): Resource<Unit> {
        return try {
            db.collection("carritos").document(uid).collection("items")
                .document(item.productoId).set(item).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al agregar item")
        }
    }

    suspend fun removeItem(uid: String, productoId: String): Resource<Unit> {
        return try {
            db.collection("carritos").document(uid).collection("items")
                .document(productoId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al eliminar item")
        }
    }

    suspend fun clearCarrito(uid: String): Resource<Unit> {
        return try {
            val items = db.collection("carritos").document(uid).collection("items").get().await()
            for (doc in items.documents) {
                doc.reference.delete()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al vaciar carrito")
        }
    }
}
