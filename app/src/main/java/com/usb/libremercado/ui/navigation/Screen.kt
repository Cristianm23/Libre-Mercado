package com.usb.libremercado.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Registro : Screen("registro")
    object Home : Screen("home")
    object Catalogo : Screen("catalogo")
    object DetalleProducto : Screen("detalle/{productoId}") {
        fun createRoute(productoId: String) = "detalle/$productoId"
    }
    object Carrito : Screen("carrito")
    object Calificacion : Screen("calificacion/{productoId}/{vendedorId}") {
        fun createRoute(productoId: String, vendedorId: String) = "calificacion/$productoId/$vendedorId"
    }
    object ListaChats : Screen("lista_chats")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object Perfil : Screen("perfil")
    object Publicar : Screen("publicar")
    object AdminPanel : Screen("admin_panel")
}
