
/*
package ipca.api.relay.presentation.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ipca.api.relay.navigation.*
import ipca.api.relay.presentation.components.SearchBar

@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val figmaPurple = Color(0xFF6666FF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 🔍 ÁREA DE PESQUISA (Sem as chips em baixo)
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchChange
            )
        }

        // 📂 LISTA DE CATEGORIAS EM COLUNA
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.categories) { category ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("$HomeWithCategoryRoute?category=${category.name}") {
                                    popUpTo(HomeGraph) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = figmaPurple,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = category.name,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = figmaPurple
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

 */