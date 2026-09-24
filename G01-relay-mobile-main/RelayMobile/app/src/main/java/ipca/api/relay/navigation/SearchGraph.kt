/*
package ipca.api.relay.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import ipca.api.relay.presentation.search.SearchScreen
import ipca.api.relay.presentation.search.SearchViewModel
import kotlinx.serialization.Serializable


fun NavGraphBuilder.searchGraph(navController: NavHostController) {

    navigation<SearchGraph>(startDestination = Search) {

        composable<Search> {

            val viewModel = remember {
                SearchViewModel()
            }

            SearchScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

 */