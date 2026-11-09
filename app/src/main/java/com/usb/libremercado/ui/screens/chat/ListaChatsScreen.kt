package com.usb.libremercado.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.usb.libremercado.data.model.Chat
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.viewmodel.ListaChatsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaChatsScreen(
    viewModel: ListaChatsViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val chatsState by viewModel.chats.collectAsState()
    val currentUserId = viewModel.currentUserId ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Mensajes (Compradores / Vendedores)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (chatsState) {
                is Resource.Loading -> LoadingScreen()
                is Resource.Error -> ErrorMessage(chatsState.message ?: "Error al cargar chats")
                is Resource.Success -> {
                    val chats = chatsState.data.orEmpty()
                    if (chats.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tienes conversaciones activas.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(chats) { chat ->
                                val otherUser = if (chat.compradorId == currentUserId) "Vendedor" else "Comprador"
                                ChatItemRow(
                                    chat = chat,
                                    currentUserId = currentUserId,
                                    subtitle = "$otherUser • ${chat.ultimoMensaje.ifBlank { "Sin mensajes aún" }}",
                                    onClick = { onNavigateToChat(chat.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItemRow(chat: Chat, currentUserId: String, subtitle: String, onClick: () -> Unit) {
    val isUnread = chat.ultimoMensaje.isNotBlank() && !chat.leidoPor.contains(currentUserId)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chat de Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isUnread) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Nuevo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.ultimaActualizacion)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
