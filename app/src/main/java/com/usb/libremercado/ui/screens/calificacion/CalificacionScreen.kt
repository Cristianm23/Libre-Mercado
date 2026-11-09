package com.usb.libremercado.ui.screens.calificacion

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.usb.libremercado.data.model.Resource
import com.usb.libremercado.viewmodel.CalificacionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificacionScreen(
    viewModel: CalificacionViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var estrellas by remember { mutableStateOf(5) }
    var comentario by remember { mutableStateOf("") }
    val calificacionState by viewModel.calificacionState.collectAsState()

    LaunchedEffect(calificacionState) {
        if (calificacionState is Resource.Success) {
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calificar Vendedor") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo fue tu experiencia con el vendedor?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row {
                repeat(5) { index ->
                    val starIndex = index + 1
                    IconButton(onClick = { estrellas = starIndex }) {
                        Icon(
                            imageVector = if (starIndex <= estrellas) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$starIndex estrellas",
                            tint = if (starIndex <= estrellas) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = comentario,
                onValueChange = { comentario = it },
                label = { Text("Comentario sobre la compra") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.calificar(estrellas, comentario) },
                modifier = Modifier.fillMaxWidth(),
                enabled = calificacionState !is Resource.Loading
            ) {
                if (calificacionState is Resource.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Enviar Calificación")
                }
            }

            if (calificacionState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (calificacionState as Resource.Error).message ?: "Error al calificar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
