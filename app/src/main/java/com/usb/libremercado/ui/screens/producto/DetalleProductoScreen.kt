package com.usb.libremercado.ui.screens.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.ui.components.RatingBar
import com.usb.libremercado.ui.components.formatPrecio
import com.usb.libremercado.viewmodel.DetalleProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    viewModel: DetalleProductoViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val productoState by viewModel.producto.collectAsState()
    val vendedorState by viewModel.vendedor.collectAsState()
    val chatIdState by viewModel.chatId.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedFullScreenUrl by remember { mutableStateOf("") }

    LaunchedEffect(chatIdState) {
        if (chatIdState is Resource.Success) {
            onNavigateToChat((chatIdState as Resource.Success<String>).data!!)
            viewModel.resetChatState()
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is Resource.Success) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    val producto = (productoState as? Resource.Success)?.data
                    if (producto != null && producto.vendedorId == viewModel.currentUserId) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (productoState) {
                is Resource.Loading -> LoadingScreen()
                is Resource.Error -> ErrorMessage(productoState.message ?: "Error")
                is Resource.Success -> {
                    val producto = productoState.data!!
                    val allImages = if (producto.imagenes.isNotEmpty()) producto.imagenes else listOf(producto.imagen)

                    if (allImages.isNotEmpty() && allImages.first().startsWith("http")) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allImages.size) { index ->
                                val imageUrl = allImages[index]
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = producto.nombre,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(300.dp)
                                        .clickable {
                                            selectedFullScreenUrl = imageUrl
                                            showFullScreenImage = true
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Sin imagen",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${formatPrecio(producto.precio)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Estado: ${if (producto.estado == "nuevo") "Nuevo" else "Usado"}")
                        RatingBar(rating = producto.calificacion)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = producto.descripcion)
                        
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Text(
                            text = "Información del Vendedor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        when (vendedorState) {
                            is Resource.Loading -> CircularProgressIndicator()
                            is Resource.Success -> {
                                val vendedor = vendedorState.data!!
                                Text(text = vendedor.nombre)
                                RatingBar(rating = vendedor.calificacionPromedio)
                            }
                            else -> {}
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.agregarAlCarrito() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Agregar al Carrito")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { 
                                val compradorId = viewModel.currentUserId
                                if (compradorId != null) {
                                    viewModel.contactarVendedor(compradorId, producto.vendedorId)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = chatIdState !is Resource.Loading
                        ) {
                            if (chatIdState is Resource.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Chatear con el Vendedor")
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Producto") },
                text = { Text("¿Estás seguro de que deseas eliminar este producto?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.eliminarProducto()
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showFullScreenImage && selectedFullScreenUrl.isNotBlank()) {
            Dialog(onDismissRequest = { showFullScreenImage = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = selectedFullScreenUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = { showFullScreenImage = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
