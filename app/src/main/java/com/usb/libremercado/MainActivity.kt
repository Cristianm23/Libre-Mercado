package com.usb.libremercado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.usb.libremercado.ui.navigation.NavGraph
import com.usb.libremercado.ui.theme.LibreMercadoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibreMercadoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
