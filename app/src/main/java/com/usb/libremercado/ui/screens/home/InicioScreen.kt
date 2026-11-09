package com.usb.libremercado.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.ui.components.ProductCard
import com.usb.libremercado.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    viewModel: HomeViewModel,
    cartItemCount: Int,
    onNavigateToDetalle: (String) -> Unit,
    onNavigateToPublicar: () -> Unit,
    onNavigateToCarrito: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToChats: () -> Unit
) {
    val productosState by viewModel.productos.collectAsState()
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(viewModel) {
        viewModel.getProductos(query = searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (searchVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { 
                                searchQuery = it
                                viewModel.getProductos(query = it)
                            },
                            placeholder = { Text("Buscar en Libre Mercado") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text("Libre Mercado")
                    }
                },
                actions = {
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onNavigateToChats) {
                        Icon(Icons.Default.Chat, contentDescription = "Mensajes")
                    }
                    IconButton(onClick = onNavigateToCarrito) {
                        BadgedBox(
                            badge = {
                                if (cartItemCount > 0) {
                                    Badge { Text("$cartItemCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                    IconButton(onClick = onNavigateToPerfil) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToPublicar) {
                Icon(Icons.Default.Add, contentDescription = "Publicar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (productosState) {
                is Resource.Loading -> {
                    val productos = productosState.data.orEmpty()
                    if (productos.isEmpty()) LoadingScreen() else ProductGrid(productos, onNavigateToDetalle)
                }
                is Resource.Error -> {
                    val productos = productosState.data.orEmpty()
                    if (productos.isEmpty()) ErrorMessage(productosState.message ?: "Error")
                    else ProductGrid(productos, onNavigateToDetalle)
                }
                is Resource.Success -> {
                    val productos = productosState.data.orEmpty()
                    ProductGrid(productos, onNavigateToDetalle)
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun ProductGrid(productos: List<com.usb.libremercado.data.model.Producto>, onNavigateToDetalle: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(productos) { producto ->
            ProductCard(producto = producto) {
                onNavigateToDetalle(producto.id)
            }
        }
    }
}
