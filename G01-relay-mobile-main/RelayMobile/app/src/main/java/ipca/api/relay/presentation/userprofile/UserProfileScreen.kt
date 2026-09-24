package ipca.api.relay.presentation.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import ipca.api.relay.presentation.components.StarRating
import ipca.api.relay.presentation.home.components.ResourceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Int,
    onBack: () -> Unit,
    onResourceClick: (resourceId: Int) -> Unit,
    onReviewsClick: () -> Unit = {},
    viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.factory(userId))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val primary = Color(0xFF6666FF)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(state.profile?.displayName ?: "Profile")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primary)
                    }
                }
                state.profile == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            state.error ?: "Could not load this profile",
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                else -> {
                    val profile = state.profile!!
                    Column(modifier = Modifier.fillMaxSize()) {
                        // ── Header ──────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (profile.profilePicture != null) {
                                AsyncImage(
                                    model = profile.profilePicture,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFEFF6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = profile.displayName,
                                color = primary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                            )

                            if (profile.username.isNotBlank() && profile.username != profile.displayName) {
                                Text(
                                    text = "@${profile.username}",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clickable(onClick = onReviewsClick)
                                    .padding(vertical = 4.dp)
                            ) {
                                StarRating(rating = profile.reputation / 20f)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "(${String.format("%.1f", profile.reputation / 20f)})",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                )
                            }

                            Text(
                                text = "View Reviews",
                                color = primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable(onClick = onReviewsClick)
                                    .padding(top = 2.dp, bottom = 4.dp)
                            )

                            // University / course shown when present.
                            val edu = listOfNotNull(profile.universityName, profile.courseName)
                                .joinToString(" — ")
                            if (edu.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(edu, color = Color.Gray, fontSize = 13.sp)
                            }
                        }

                        HorizontalDivider()

                        // ── Listings ────────────────────────────────────────
                        Text(
                            text = "Listings",
                            color = primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )

                        if (state.error != null) {
                            Text(
                                state.error!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else if (state.resources.isEmpty()) {
                            Text(
                                "This user has no listings yet.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 4.dp
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.resources, key = { it.id }) { resource ->
                                    ResourceCard(
                                        resource = resource,
                                        onClick = { onResourceClick(resource.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
