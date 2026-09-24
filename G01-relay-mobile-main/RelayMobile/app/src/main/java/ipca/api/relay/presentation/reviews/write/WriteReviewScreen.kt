package ipca.api.relay.presentation.reviews.write

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ipca.api.relay.presentation.reviews.components.StarRatingInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    transactionId: Int,
    transactionType: String,
    reviewedName: String,
    reviewedPicture: String?,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: WriteReviewViewModel = viewModel(
        factory = WriteReviewViewModel.factory(
            transactionId = transactionId,
            transactionType = transactionType,
            reviewedName = reviewedName,
            reviewedPicture = reviewedPicture,
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val primary = Color(0xFF6666FF)

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(color = Color.White, tonalElevation = 8.dp, shadowElevation = 8.dp) {
                Button(
                    onClick = viewModel::onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Submit", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reviewed user header
            if (state.reviewedPicture != null) {
                AsyncImage(
                    model = state.reviewedPicture,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFEFF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = state.reviewedName.ifBlank { "Transaction Counterpart" },
                color = primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = transactionTypeLabel(state.transactionType),
                color = Color.Gray,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(32.dp))

            Text("How did it go?", color = Color.Black, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            StarRatingInput(
                rating = state.score,
                onRatingChange = viewModel::onScoreChange,
                starSize = 40
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Comment (optional)",
                color = primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::onCommentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Leave a comment…", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
            )

            state.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }
    }
}

private fun transactionTypeLabel(type: String): String = when (type.uppercase()) {
    "PURCHASE" -> "Compra"
    "BORROW" -> "Loan"
    else -> type
}