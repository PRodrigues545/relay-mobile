package ipca.api.relay.presentation.home

import ipca.api.relay.domain.model.RemoteCategory
import ipca.api.relay.domain.model.Resource

const val ALL_CATEGORY = "All"

/*
TODO: swap to data-driven once usage analytics exist. For now, replace these
 */
val HOME_TOP_CATEGORIES = listOf(
    "Books & Study",
    "Electronics",
    "Tools & Engineering",
    "Lab & Science"
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val resources: List<Resource> = emptyList(),
    val filteredResources: List<Resource> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = ALL_CATEGORY,
    val allCategories: List<RemoteCategory> = emptyList(),
    val error: String? = null,
    val unreadNotificationCount: Int = 0,
    val showCategorySheet: Boolean = false
) {
    val topCategoryNames: List<String>
        get() {
            val serverNames = allCategories.map { it.name }.toSet()
            return HOME_TOP_CATEGORIES.filter { it in serverNames }
        }
}