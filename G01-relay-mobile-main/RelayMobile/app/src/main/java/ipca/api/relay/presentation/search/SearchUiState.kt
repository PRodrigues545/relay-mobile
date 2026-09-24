package ipca.api.relay.presentation.search

import ipca.api.relay.domain.model.Category
import ipca.api.relay.domain.category.CategoryProvider

data class SearchUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "Todos",
    val chips: List<String> = CategoryProvider.homeCategories,
    val categories: List<Category> = CategoryProvider.allCategories
)