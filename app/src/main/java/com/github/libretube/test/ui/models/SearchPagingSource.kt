package com.github.libretube.test.ui.models

import android.util.Log

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.libretube.test.api.MediaServiceRepository
import com.github.libretube.test.api.obj.ContentItem

class SearchPagingSource(
    private val query: String,
    private val filter: String
) : PagingSource<String, ContentItem>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ContentItem> {
        return try {
            val nextPage = params.key
            
            val result = if (nextPage == null) {
                // Initial load
                MediaServiceRepository.instance.getSearchResults(query, filter)
            } else {
                // Load next page
                MediaServiceRepository.instance.getSearchResultsNextPage(query, filter, nextPage)
            }

            LoadResult.Page(
                data = result.items,
                prevKey = null, // Search doesn't support backward paging
                nextKey = result.nextpage
            )
        } catch (e: Exception) {
            Log.e("SearchPagingSource", "Failed to load search results for query: $query, filter: $filter", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, ContentItem>): String? {
        // Return null to always start from the beginning on refresh
        return null
    }
}
