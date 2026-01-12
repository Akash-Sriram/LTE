package com.github.libretube.test.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.github.libretube.test.api.obj.ContentItem
import com.github.libretube.test.db.DatabaseHolder
import com.github.libretube.test.db.obj.SearchHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SearchScreenViewModel : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter
    
    private val _searchSuggestion = MutableStateFlow<Pair<String, Boolean>?>(null)
    val searchSuggestion: StateFlow<Pair<String, Boolean>?> = _searchSuggestion
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<ContentItem>> = combine(
        _searchQuery,
        _selectedFilter
    ) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(PagingData.empty())
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = 20,
                    enablePlaceholders = false,
                    initialLoadSize = 20
                ),
                pagingSourceFactory = {
                    SearchPagingSource(query, filter)
                }
            ).flow.cachedIn(viewModelScope)
        }
    }
    
    fun setQuery(query: String) {
        _searchQuery.value = query
        // Reset suggestion when query changes
        _searchSuggestion.value = null
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }
    
    fun saveToHistory(query: String) {
        if (query.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val historyItem = SearchHistoryItem(query = query)
                DatabaseHolder.Database.searchHistoryDao().insert(historyItem)
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    fun setSuggestion(suggestion: String, corrected: Boolean) {
        _searchSuggestion.value = suggestion to corrected
    }
}
