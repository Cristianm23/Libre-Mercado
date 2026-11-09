package com.usb.libremercado.ui.screens.carrito

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.usb.libremercado.data.model.CarritoItem
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.ui.components.formatPrecio
import com.usb.libremercado.viewmodel.CarritoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    viewModel: CarritoViewModel,
    onBack: () -> Unit,
    onNavigateToCalificacion: (String, String) -> Unit
) {
    val itemsState by viewModel.items.collectAsState()
    var showPagoDialog by remember { mutableStateOf(false) }
    var lastItemForRating by remember { mutableStateOf<CarritoItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito de Compras") },
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
            when (itemsState) {
                is Resource.Loading -> LoadingScreen()
                is Resource.Error -> ErrorMessage(itemsState.message ?: "Error al cargar carrito")
                is Resource.Success -> {
                    val items = itemsState.data.orEmpty()
                    if (items.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        val total = items.sumOf { it.precio * it.cantidad }
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(items) { item ->
                                    CarritoItemCard(
                                        item = item,
                                        onDelete = { viewModel.eliminarItem(item.productoId) }
                                    )
                                }
                            }
                            Surface(
                                tonalElevation = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total a pagar:", style = MaterialTheme.typography.titleLarge)
                                        Text("$${formatPrecio(total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            lastItemForRating = items.firstOrNull()
                                            showPagoDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Simular Pago")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showPagoDialog) {
            AlertDialog(
                onDismissRequest = { showPagoDialog = false },
                title = { Text("Pago Exitoso") },
                text = { Text("¡Tu pago simulado se ha procesado con éxito! Ahora puedes calificar al vendedor.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPagoDialog = false
                            val item = lastItemForRating
                            viewModel.vaciarCarrito {
                                if (item != null && item.vendedorId.isNotBlank()) {
                                    onNavigateToCalificacion(item.productoId, item.vendedorId)
                                } else {
                                    onBack()
                                }
                            }
                        }
                    ) {
                        Text("Calificar Vendedor")
                    }
                }
            )
        }
    }
}

@Composable
fun CarritoItemCard(item: CarritoItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imagen.startsWith("http")) {
                AsyncImage(
                    model = item.imagen,
                    contentDescription = item.nombre,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Sin imagen",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$${formatPrecio(item.precio)} x ${item.cantidad}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
