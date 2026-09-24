package ipca.api.relay.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ipca.api.relay.navigation.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import ipca.api.relay.presentation.home.components.ResourceCard
import ipca.api.relay.presentation.profile.components.ResourceThumbnail

@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    val user = viewModel.user

    // Refresh the pending-review count whenever Profile becomes visible so the
    // badge stays in sync after the user submits a review on another screen.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPendingReviewCount()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Removido: navBackStackEntry, currentRoute e currentRouteTyped.
    // Removido: RelayNavigationBar e o Box de alinhamento.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        RelayTopBar(title = "My profile")

        Spacer(Modifier.height(16.dp))

        // --- AVATAR E NOME ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (user.profilePicture != null) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(1.dp, RelayDark, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                AvatarPlaceholder(modifier = Modifier.size(76.dp))
            }

            Spacer(Modifier.width(24.dp))

            Text(
                text = user.name,
                color = RelayPurple
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- RATINGS (VENDEDOR E CLIENTE) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(SellerReviews) }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "Seller",
                color = RelayPurple
            )

            RatingStars(rating = user.sellerRating)
        }

        Spacer(Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(ClientReviews) }
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = "Buyer",
                color = RelayPurple
            )

            RatingStars(rating = user.clientRating)
        }

        Spacer(Modifier.height(48.dp)) // Ajustado ligeiramente para caber melhor

        // --- INFO E OPÇÕES ---
        ProfileInfoRow("Location", user.location)
        ProfileInfoRow("University", user.university)

        ProfileOptionRow("History") {
            navController.navigate(History)
        }

        // Avaliações pendentes — only shown when there's something to do, so the
        // row doesn't become permanent dead space for users who stay on top of
        // their reviews. The count is loaded by ProfileViewModel on init and on
        // resume; submitting a review elsewhere updates it via the same loader.
        if (viewModel.pendingReviewCount > 0) {
            ProfileOptionRow(
                "Pending Reviews (${viewModel.pendingReviewCount})"
            ) {
                navController.navigate(PendingReviews)
            }
        }

        ProfileOptionRow("Edit Profile") {
            navController.navigate(EditProfile)
        }

        ProfileOptionRow("Settings") {
            navController.navigate(Settings)
        }

        Spacer(Modifier.height(16.dp))

        // --- PUBLICAÇÕES ---
        Text(
            text = "Listings",
            color = RelayPurple
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(viewModel.userResources) { resource ->
                ResourceThumbnail(
                    resource = resource,
                    // ownerId hint = the resource's owner. On Profile this is
                    // always the current user, so ProductScreen will render
                    // the Owner bottom bar (Edit/Delete) immediately, with no
                    // flash of the Buy/Offer bar while the fetch resolves.
                    onClick = { navController.navigate(Product(id = resource.id, ownerId = resource.ownerId)) },
                    modifier = Modifier.width(126.dp)
                )
            }
        }
    }
}