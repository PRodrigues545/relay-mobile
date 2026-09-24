package ipca.api.relay.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ipca.api.relay.navigation.Notifications
import ipca.api.relay.navigation.Product
import ipca.api.relay.presentation.common.NotificationPermissionHandler
import ipca.api.relay.presentation.home.components.CategoryBottomSheet
import ipca.api.relay.presentation.home.components.CategoryChip
import ipca.api.relay.presentation.home.components.ResourceCard

// Theme constants hoisted out of the composable so they're not reallocated on
// every recomposition. Color objects are tiny but with the grid recomposing on
// any list update, the cumulative churn matters.
private val FigmaPrimary = Color(0xFF2A2A5E)
private val AccentPurple = Color(0xFF6666FF)
private val BadgePurple = Color(0xFF6666FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel
) {
    var askForNotifications by remember { mutableStateOf(false) }

    // Fire the request once, right after this screen appears.
    LaunchedEffect(Unit) { askForNotifications = true }

    NotificationPermissionHandler(
        trigger = askForNotifications,
        onResult = { /* optionally log or update a preference */ }
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Stable click handler for grid items — without remember, a new lambda is
    // allocated on each recomposition and Compose can't skip ResourceCard
    // because its onClick parameter changes identity every time.
    //
    // We pass ownerId through the route so ProductScreen can paint the
    // correct bottom bar (Owner vs Buy/Borrow) on the first frame — Home
    // already has the full Resource, no reason to make the user wait for a
    // re-fetch.
    val onResourceClick = remember<(Int, Int) -> Unit>(navController) {
        { resourceId, ownerId ->
            navController.navigate(Product(id = resourceId, ownerId = ownerId))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header: live-filter search bar + notifications icon with badge.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = FigmaPrimary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = FigmaPrimary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FigmaPrimary,
                    unfocusedBorderColor = FigmaPrimary,
                    cursorColor = FigmaPrimary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Notifications button + badge.
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, FigmaPrimary, RoundedCornerShape(12.dp))
                        .clickable { navController.navigate(Notifications) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Notifications",
                        tint = FigmaPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (uiState.unreadNotificationCount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 5.dp, y = (-5).dp)
                            .size(18.dp),
                        shape = CircleShape,
                        color = BadgePurple,
                        tonalElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (uiState.unreadNotificationCount > 99) "99+"
                                else uiState.unreadNotificationCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Category chips row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = ALL_CATEGORY) {
                    CategoryChip(
                        category = "All",
                        selected = uiState.selectedCategory == ALL_CATEGORY,
                        onClick = { viewModel.onCategorySelected(ALL_CATEGORY) }
                    )
                }
                items(uiState.topCategoryNames, key = { it }) { name ->
                    CategoryChip(
                        category = name,
                        selected = uiState.selectedCategory == name,
                        onClick = { viewModel.onCategorySelected(name) }
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "View all",
                color = AccentPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { viewModel.onOpenCategorySheet() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resources grid.
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FigmaPrimary
                )
                uiState.error != null -> Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
                uiState.filteredResources.isEmpty() -> Text(
                    text = if (uiState.searchQuery.isNotBlank())
                        "No results for \"${uiState.searchQuery}\""
                    else
                        "No listings in this category",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                    textAlign = TextAlign.Center
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // key = it.id lets Compose reuse item composables when
                    // the underlying list reorders or shrinks (e.g. search
                    // filtering). Without a key, every visible card is
                    // rebuilt from scratch when the list changes — which is
                    // exactly what happens on each search keystroke.
                    items(
                        items = uiState.filteredResources,
                        key = { it.id }
                    ) { resource ->
                        ResourceCard(
                            resource = resource,
                            onClick = { onResourceClick(resource.id, resource.ownerId) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showCategorySheet) {
        CategoryBottomSheet(
            categories = uiState.allCategories,
            selectedCategory = uiState.selectedCategory,
            onSelect = { viewModel.onCategorySelected(it) },
            onDismiss = { viewModel.onCloseCategorySheet() }
        )
    }
}