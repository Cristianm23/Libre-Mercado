package com.usb.libremercado.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.data.model.Usuario
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val currentUser get() = auth.currentUser

    suspend fun login(correo: String, contrasena: String): Resource<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val uid = result.user?.uid ?: throw Exception("Error al obtener UID")
            val doc = db.collection("usuarios").document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java) ?: throw Exception("Usuario no encontrado")
            Resource.Success(usuario)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun registro(nombre: String, correo: String, contrasena: String): Resource<Usuario> {
        return try {
            val result = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val uid = result.user?.uid ?: throw Exception("Error al crear usuario")
            val usuario = Usuario(
                uid = uid,
                nombre = nombre,
                correo = correo,
                tipoUsuario = "usuario"
            )
            db.collection("usuarios").document(uid).set(usuario).await()
            Resource.Success(usuario)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getUsuario(uid: String): Resource<Usuario> {
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            val usuario = doc.toObject(Usuario::class.java) ?: throw Exception("Usuario no encontrado")
            Resource.Success(usuario)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getUsuarios(): Resource<List<Usuario>> {
        return try {
            val snapshot = db.collection("usuarios").get().await()
            val usuarios = snapshot.toObjects(Usuario::class.java)
            Resource.Success(usuarios)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener usuarios")
        }
    }
}
