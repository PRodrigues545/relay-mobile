package ipca.api.relay.presentation.reviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.profile.RatingStars
import ipca.api.relay.presentation.profile.RelayDark
import ipca.api.relay.presentation.profile.RelayTopBar
import ipca.api.relay.presentation.profile.ReviewItem
import ipca.api.relay.presentation.profile.ReviewTab
import ipca.api.relay.presentation.profile.ReviewUi
import ipca.api.relay.presentation.reviews.components.ReviewTabButton
import kotlin.math.roundToInt

@Composable
fun ReviewsScreen(
    userId: Int,
    onBack: () -> Unit,
    selectedTab: ReviewTab,
    viewModel: ReviewsViewModel = viewModel(factory = ReviewsViewModel.factory(userId, selectedTab))
) {
    val state = viewModel.uiState
    val averageRating = state.averageRating
    val reviews = state.reviews

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RelayTopBar(title = "Reviews", onBackClick = onBack)

        Row(modifier = Modifier.fillMaxWidth()) {
            ReviewTabButton(
                text = "Seller",
                selected = state.selectedTab == ReviewTab.SELLER,
                modifier = Modifier.weight(1f)
            ) { viewModel.onTabChange(ReviewTab.SELLER) }

            ReviewTabButton(
                text = "Buyer",
                selected = state.selectedTab == ReviewTab.CLIENT,
                modifier = Modifier.weight(1f)
            ) { viewModel.onTabChange(ReviewTab.CLIENT) }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(Modifier.height(24.dp))

            Text(
                text = String.format("%.1f", averageRating),
                color = Color.Black,
                fontSize = 78.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            RatingStars(
                rating = averageRating.roundToInt().coerceIn(0, 5),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Number of reviews: ${reviews.size}",
                color = RelayDark,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(28.dp))

            if (reviews.isEmpty()) {
                Text(
                    text = "No reviews yet.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                    items(reviews) { review ->
                        ReviewItem(
                            ReviewUi(
                                name = review.reviewerUsername ?: review.reviewerName ?: "Utilizador",
                                rating = review.score,
                                comment = review.comment
                            )
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}