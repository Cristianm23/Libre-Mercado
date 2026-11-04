package com.usb.libremercado.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.usb.libremercado.data.model.Chat
import com.usb.libremercado.data.model.Mensaje
import com.usb.libremercado.data.model.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getChats(usuarioId: String): Flow<Resource<List<Chat>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = db.collection("Chats")
            .whereArrayContainsAny("participantes", listOf(usuarioId))
            .orderBy("ultimaActualizacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al cargar chats"))
                    return@addSnapshotListener
                }
                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(Resource.Success(chats))
            }
        awaitClose { listener.remove() }
    }

    fun getMensajes(chatId: String): Flow<Resource<List<Mensaje>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = db.collection("Chats").document(chatId).collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error al cargar mensajes"))
                    return@addSnapshotListener
                }
                val mensajes = snapshot?.toObjects(Mensaje::class.java) ?: emptyList()
                trySend(Resource.Success(mensajes))
            }
        awaitClose { listener.remove() }
    }

    suspend fun enviarMensaje(mensaje: Mensaje): Resource<Unit> {
        return try {
            val docRef = db.collection("Chats").document(mensaje.chatId).collection("mensajes").document()
            val finalMensaje = mensaje.copy(id = docRef.id)
            docRef.set(finalMensaje).await()
            
            db.collection("Chats").document(mensaje.chatId).update(
                "ultimoMensaje", mensaje.contenido,
                "ultimaActualizacion", mensaje.timestamp
            ).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al enviar mensaje")
        }
    }

    suspend fun createOrGetChat(compradorId: String, vendedorId: String, productoId: String): Resource<String> {
        return try {
            val query = db.collection("Chats")
                .whereEqualTo("compradorId", compradorId)
                .whereEqualTo("vendedorId", vendedorId)
                .whereEqualTo("productoId", productoId)
                .get().await()
            
            if (!query.isEmpty) {
                Resource.Success(query.documents[0].id)
            } else {
                val chatId = db.collection("Chats").document().id
                val chat = Chat(
                    id = chatId,
                    compradorId = compradorId,
                    vendedorId = vendedorId,
                    productoId = productoId
                )
                // Add a participants list for easier querying
                val chatData = hashMapOf(
                    "id" to chatId,
                    "compradorId" to compradorId,
                    "vendedorId" to vendedorId,
                    "productoId" to productoId,
                    "ultimoMensaje" to "",
                    "ultimaActualizacion" to System.currentTimeMillis(),
                    "participantes" to listOf(compradorId, vendedorId)
                )
                db.collection("Chats").document(chatId).set(chatData).await()
                Resource.Success(chatId)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear chat")
        }
    }

    suspend fun getChatById(chatId: String): Resource<Chat> {
        return try {
            val doc = db.collection("Chats").document(chatId).get().await()
            val chat = doc.toObject(Chat::class.java) ?: throw Exception("Chat no encontrado")
            Resource.Success(chat)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener chat")
        }
    }
}
