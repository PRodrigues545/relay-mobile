package ipca.api.relay.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ipca.api.relay.domain.model.ResourceRequest

@Composable
fun CreateRequestScreen(
    navController: NavHostController,
    viewModel: CreateRequestViewModel // ViewModel injetado
) {
    // collectAsStateWithLifecycle unsubscribes the upstream flow when the
    // screen is in the background (STOPPED), avoiding wasted work and
    // network traffic when the user is elsewhere in the app.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Request?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    enabled = !uiState.deleteInProgress
                ) {
                    Text(
                        if (uiState.deleteInProgress) "Deleting…" else "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.cancelDelete() },
                    enabled = !uiState.deleteInProgress
                ) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }

            Text(
                text = "Requests",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // ÁREA DE PUBLICAÇÃO
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                placeholder = { Text("What are you looking for?") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading, // Bloqueia durante o envio
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.publishRequest() },
                modifier = Modifier.align(Alignment.End),
                enabled = uiState.title.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading && uiState.title.isEmpty()) {
                    // Opcional: Feedback visual de publicação
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Publish")
                }
            }
        }

        // MENSAGEM DE ERRO (Caso exista)
        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "An error occurred",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // LISTA DE PEDIDOS (LazyColumn)
        if (uiState.isLoading && uiState.requests.isEmpty()) {
            // Loading inicial
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.requests) { request ->
                    val canDelete = request.requesterId == uiState.currentUserId &&
                            uiState.currentUserId != -1
                    RequestCard(
                        request = request,
                        canDelete = canDelete,
                        onDeleteClick = { viewModel.requestDelete(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RequestCard(
    request: ResourceRequest,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Por ${request.userName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = request.createdAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
            if (canDelete) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete request",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}