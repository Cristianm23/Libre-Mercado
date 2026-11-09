package com.usb.libremercado.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usb.libremercado.data.model.Mensaje
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val mensajesState by viewModel.mensajes.collectAsState()
    val chatState by viewModel.chat.collectAsState()
    val sendError by viewModel.sendError.collectAsState()
    val currentUserId = viewModel.currentUserId ?: ""
    val receptorId = viewModel.otherUserId

    var textMessage by remember { mutableStateOf("") }
    var defaultInitialized by remember { mutableStateOf(false) }
    val sending = remember { mutableStateOf(false) }

    LaunchedEffect(chatState, mensajesState) {
        if (!defaultInitialized) {
            val chat = (chatState as? Resource.Success)?.data
            val mensajes = (mensajesState as? Resource.Success)?.data
            if (chat != null && mensajes != null) {
                val isBuyer = chat.compradorId == currentUserId
                if (isBuyer && mensajes.isEmpty()) {
                    textMessage = "Hola, me interesa. ¿Cuánto es lo mínimo?"
                }
                defaultInitialized = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentPadding = PaddingValues(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = textMessage,
                        onValueChange = { textMessage = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un mensaje...") },
                        maxLines = 3
                    )
                    IconButton(
                        enabled = textMessage.isNotBlank() && !sending.value,
                        onClick = {
                            sending.value = true
                            viewModel.enviarMensaje(textMessage, receptorId) {
                                sending.value = false
                                textMessage = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (chatState is Resource.Error) {
                ErrorMessage(chatState.message ?: "No se pudo cargar la conversación")
            }
            sendError?.let { ErrorMessage(it) }
            when (mensajesState) {
                is Resource.Loading -> LoadingScreen()
                is Resource.Error -> ErrorMessage(mensajesState.message ?: "Error")
                is Resource.Success -> {
                    val mensajes = (mensajesState as Resource.Success).data ?: emptyList()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = false,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(mensajes) { mensaje ->
                            ChatBubble(mensaje = mensaje, isMine = mensaje.emisorId == currentUserId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(mensaje: Mensaje, isMine: Boolean) {
    val alignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
    val color = if (isMine) MaterialTheme.colorScheme.secondary else Color.LightGray
    val contentColor = if (isMine) Color.White else Color.Black
    val shape = if (isMine) RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp) else RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)

    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = alignment) {
        Surface(
            color = color,
            shape = shape,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = mensaje.contenido, color = contentColor)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(mensaje.timestamp)),
                    fontSize = 10.sp,
                    color = contentColor.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
