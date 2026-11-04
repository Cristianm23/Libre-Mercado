package com.usb.libremercado.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.components.ErrorMessage
import com.usb.libremercado.ui.components.LoadingScreen
import com.usb.libremercado.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Usuarios", "Productos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
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
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> UsuariosTab(viewModel)
                1 -> ProductosTab(viewModel)
            }
        }
    }
}

@Composable
fun UsuariosTab(viewModel: AdminViewModel) {
    val usuariosState by viewModel.usuarios.collectAsState()

    when (usuariosState) {
        is Resource.Loading -> LoadingScreen()
        is Resource.Error -> ErrorMessage(usuariosState.message ?: "Error")
        is Resource.Success -> {
            val usuarios = (usuariosState as Resource.Success).data ?: emptyList()
            LazyColumn {
                items(usuarios) { usuario ->
                    ListItem(
                        headlineContent = { Text(usuario.nombre) },
                        supportingContent = { Text("${usuario.correo} - ${usuario.tipoUsuario}") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ProductosTab(viewModel: AdminViewModel) {
    val productosState by viewModel.productos.collectAsState()

    when (productosState) {
        is Resource.Loading -> LoadingScreen()
        is Resource.Error -> ErrorMessage(productosState.message ?: "Error")
        is Resource.Success -> {
            val productos = (productosState as Resource.Success).data ?: emptyList()
            LazyColumn {
                items(productos) { producto ->
                    ListItem(
                        headlineContent = { Text(producto.nombre) },
                        supportingContent = { Text("Vendedor ID: ${producto.vendedorId}") },
                        leadingContent = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (producto.activo) "Activo" else "Inactivo")
                                Switch(
                                    checked = producto.activo,
                                    onCheckedChange = { viewModel.toggleProducto(producto.id, it) }
                                )
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
