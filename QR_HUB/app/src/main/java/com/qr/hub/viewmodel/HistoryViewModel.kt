package com.qr.hub.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qr.hub.data.model.HistoryItem
import com.qr.hub.data.repository.HistoryRepository
import com.qr.hub.model.ScannedQR
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val items: List<HistoryItem> = emptyList(),
    val allItemsForTab: List<HistoryItem> = emptyList(), // Unfiltered items for category chips
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: Tab = Tab.ALL,
    val selectedCategory: String? = null,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val error: String? = null
)

enum class Tab {
    ALL, SCANNED, GENERATED, FAVORITES
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistoryRepository(application)

    private val _selectedTab = MutableStateFlow(Tab.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _selectionState = MutableStateFlow(SelectionState())

    data class SelectionState(
        val selectionMode: Boolean = false,
        val selectedIds: Set<Long> = emptySet()
    )

    // Reactive: whenever tab changes, switch to the correct Flow from DB
    private val tabItems: Flow<List<HistoryItem>> = _selectedTab.flatMapLatest { tab ->
        when (tab) {
            Tab.ALL -> repository.allHistory
            Tab.SCANNED -> repository.scannedHistory
            Tab.GENERATED -> repository.generatedHistory
            Tab.FAVORITES -> repository.favorites
        }
    }

    // Combine all state sources into one reactive UI state
    val uiState: StateFlow<HistoryUiState> = combine(
        tabItems,
        _searchQuery,
        _selectedCategory,
        _selectedTab,
        _selectionState
    ) { items, query, category, tab, selection ->
        val filtered = filterItems(items, query, category)
        HistoryUiState(
            items = filtered,
            allItemsForTab = items, // Keep unfiltered for category chips
            isLoading = false,
            searchQuery = query,
            selectedTab = tab,
            selectedCategory = category,
            selectionMode = selection.selectionMode,
            selectedIds = selection.selectedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    private fun filterItems(
        items: List<HistoryItem>,
        query: String,
        category: String?
    ): List<HistoryItem> {
        var filtered = items

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.rawValue.contains(query, ignoreCase = true) ||
                        it.title.contains(query, ignoreCase = true)
            }
        }

        if (category != null) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        return filtered
    }

    fun setTab(tab: Tab) {
        _selectedTab.value = tab
        _selectedCategory.value = null // Reset category when tab changes
        _selectionState.value = SelectionState() // Clear selection
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(item: HistoryItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun deleteItem(item: HistoryItem) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val ids = _selectionState.value.selectedIds.toList()
            if (ids.isNotEmpty()) {
                repository.deleteByIds(ids)
                _selectionState.value = SelectionState()
            }
        }
    }

    fun toggleSelection(id: Long) {
        _selectionState.update { state ->
            val newSelection = if (state.selectedIds.contains(id)) {
                state.selectedIds - id
            } else {
                state.selectedIds + id
            }
            state.copy(
                selectedIds = newSelection,
                selectionMode = newSelection.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        _selectionState.value = SelectionState()
    }

    fun selectAll() {
        val allIds = uiState.value.items.map { it.id }.toSet()
        _selectionState.value = SelectionState(
            selectedIds = allIds,
            selectionMode = true
        )
    }

    fun saveScan(rawValue: String, parsed: ScannedQR) {
        viewModelScope.launch {
            repository.saveScan(rawValue, parsed)
        }
    }

    fun saveGenerate(rawValue: String, type: String, title: String = "") {
        viewModelScope.launch {
            repository.saveGenerate(rawValue, type, title)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    suspend fun getItemById(id: Long): HistoryItem? {
        return repository.getById(id)
    }
}
