package ipca.api.relay.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ipca.api.relay.domain.model.Resource
import ipca.api.relay.domain.repository.NotificationRepository
import ipca.api.relay.domain.repository.ResourceRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val repository: ResourceRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Search query is kept in its own flow so we can debounce it cheaply.
    // Without debouncing, every keystroke was running a full O(n) scan
    // synchronously on the main thread; on a 200-item catalog that's a
    // visible hitch on lower-end devices.
    private val searchQuery = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow(ALL_CATEGORY)
    private val allResources = MutableStateFlow<List<Resource>>(emptyList())

    init {
        loadResources()
        loadCategories()
        loadUnreadCount()

        // Recompute the filtered list when (debounced search, category, full
        // list) changes. The 200ms debounce window is small enough that
        // typing still feels responsive, but coalesces a burst of keystrokes
        // into a single recomputation.
        combine(
            searchQuery.debounce(SEARCH_DEBOUNCE_MS).distinctUntilChanged(),
            selectedCategory,
            allResources
        ) { query, category, resources ->
            filterResources(resources, query, category)
        }
            .onEach { filtered -> _uiState.update { it.copy(filteredResources = filtered) } }
            .launchIn(viewModelScope)
    }

    private fun loadResources() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val resources = repository.getResources()
                allResources.value = resources
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        resources = resources,
                        filteredResources = resources,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onSuccess { list ->
                    _uiState.update { it.copy(allCategories = list) }
                }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            notificationRepository.getMyNotifications()
                .onSuccess { notifications ->
                    _uiState.update {
                        it.copy(unreadNotificationCount = notifications.count { n -> !n.isRead })
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        // Mirror the query into uiState immediately so the OutlinedTextField
        // stays in lockstep with the user's typing. The expensive filtering
        // step downstream still runs on the debounced flow above.
        _uiState.update { it.copy(searchQuery = query) }
        searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category, showCategorySheet = false) }
        selectedCategory.value = category
    }

    fun onOpenCategorySheet() {
        _uiState.update { it.copy(showCategorySheet = true) }
    }

    fun onCloseCategorySheet() {
        _uiState.update { it.copy(showCategorySheet = false) }
    }

    private fun filterResources(
        resources: List<Resource>,
        query: String,
        category: String
    ): List<Resource> {
        // Trim once, lower-case once — was being recomputed per item before.
        val trimmedQuery = query.trim()
        val noQuery = trimmedQuery.isEmpty()
        val noCategoryFilter = category == ALL_CATEGORY

        // Fast path: nothing to filter on. Returning the same reference lets
        // Compose's structural equality skip recomposing the grid entirely.
        if (noQuery && noCategoryFilter) return resources

        return resources.filter { resource ->
            val matchesSearch = noQuery || resource.title.contains(trimmedQuery, ignoreCase = true)
            val matchesCategory = noCategoryFilter ||
                    resource.category.equals(category, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 200L
    }
}