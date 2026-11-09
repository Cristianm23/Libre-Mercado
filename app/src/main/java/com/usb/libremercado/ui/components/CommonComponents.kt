package com.usb.libremercado.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun RatingBar(rating: Double, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        androidx.compose.foundation.layout.Row {
            repeat(5) { index ->
                val icon = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.Star
                val color = if (index < rating.toInt()) Color(0xFFFFD700) else Color.Gray
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(2.dp)
                )
            }
        }
    }
}

fun formatPrecio(precio: Double): String {
    return try {
        val formatter = NumberFormat.getNumberInstance(Locale("es", "CO"))
        formatter.maximumFractionDigits = 0
        formatter.format(precio)
    } catch (e: Exception) {
        precio.toLong().toString()
    }
}
