package com.usb.libremercado.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.ui.screens.admin.AdminPanelScreen
import com.usb.libremercado.ui.screens.auth.LoginScreen
import com.usb.libremercado.ui.screens.auth.RegistroScreen
import com.usb.libremercado.ui.screens.calificacion.CalificacionScreen
import com.usb.libremercado.ui.screens.carrito.CarritoScreen
import com.usb.libremercado.ui.screens.chat.ChatScreen
import com.usb.libremercado.ui.screens.chat.ListaChatsScreen
import com.usb.libremercado.ui.screens.home.InicioScreen
import com.usb.libremercado.ui.screens.perfil.PerfilScreen
import com.usb.libremercado.ui.screens.producto.DetalleProductoScreen
import com.usb.libremercado.ui.screens.publicar.PublicarProductoScreen
import com.usb.libremercado.viewmodel.*

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()

    val startDestination = if (authViewModel.currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegistro = { navController.navigate(Screen.Registro.route) },
                onLoginSuccess = { 
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegistroSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DetalleProducto.route) {
            val detailViewModel: DetalleProductoViewModel = hiltViewModel()
            DetalleProductoScreen(
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId -> 
                    navController.navigate(Screen.Chat.createRoute(chatId))
                }
            )
        }

        composable(Screen.Home.route) {
            val carritoViewModel: CarritoViewModel = hiltViewModel()
            val carritoState by carritoViewModel.items.collectAsState()
            val cartCount = (carritoState as? Resource.Success)?.data?.size ?: 0

            InicioScreen(
                viewModel = homeViewModel,
                cartItemCount = cartCount,
                onNavigateToDetalle = { id -> navController.navigate(Screen.DetalleProducto.createRoute(id)) },
                onNavigateToPublicar = { navController.navigate(Screen.Publicar.route) },
                onNavigateToCarrito = { navController.navigate(Screen.Carrito.route) },
                onNavigateToPerfil = { navController.navigate(Screen.Perfil.route) },
                onNavigateToChats = { navController.navigate(Screen.ListaChats.route) }
            )
        }

        composable(Screen.Publicar.route) {
            val publicarViewModel: PublicarProductoViewModel = hiltViewModel()
            PublicarProductoScreen(
                viewModel = publicarViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Carrito.route) {
            val carritoViewModel: CarritoViewModel = hiltViewModel()
            CarritoScreen(
                viewModel = carritoViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToCalificacion = { productoId, vendedorId ->
                    navController.navigate(Screen.Calificacion.createRoute(productoId, vendedorId))
                }
            )
        }

        composable(Screen.Calificacion.route) {
            val calificacionViewModel: CalificacionViewModel = hiltViewModel()
            CalificacionScreen(
                viewModel = calificacionViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.Perfil.route) {
            PerfilScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onNavigateToAdmin = { navController.navigate(Screen.AdminPanel.route) }
            )
        }

        composable(Screen.AdminPanel.route) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminPanelScreen(
                viewModel = adminViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ListaChats.route) {
            val listaChatsViewModel: ListaChatsViewModel = hiltViewModel()
            ListaChatsScreen(
                viewModel = listaChatsViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                }
            )
        }

        composable(Screen.Chat.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
