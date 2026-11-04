package com.usb.libremercado.ui.screens.producto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.ui.components.RatingBar
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

    LaunchedEffect(chatIdState) {
        if (chatIdState is Resource.Success) {
            onNavigateToChat((chatIdState as Resource.Success<String>).data!!)
            viewModel.resetChatState()
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
                    AsyncImage(
                        model = producto.imagen,
                        contentDescription = producto.nombre,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${producto.precio}",
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
                            onClick = { /* TODO: Agregar al carrito */ },
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
    }
}
