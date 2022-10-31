package com.catelt.mome.ui.search

import androidx.paging.PagingData
import com.catelt.mome.data.model.DetailPresentable
import com.catelt.mome.data.model.search.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


data class SearchScreenUIState(
    val query: String?,
    val searchState: SearchState,
    val resultState: ResultState,
    val queryLoading: Boolean,
    val voiceSearch: Boolean,
) {
    companion object {
        val default: SearchScreenUIState = SearchScreenUIState(
            query = null,
            searchState = SearchState.EmptyQuery,
            resultState = ResultState.Default(),
            queryLoading = false,
            voiceSearch = false
        )
    }
}

sealed class SearchState {
    object EmptyQuery : SearchState()
    object ValidQuery : SearchState()
}

sealed class ResultState {
    data class Default(val popular: Flow<PagingData<DetailPresentable>> = emptyFlow()) : ResultState()
    data class Search(val result: Flow<PagingData<SearchResult>>) : ResultState()
}

data class QueryState(
    val query: String?,
    val loading: Boolean
) {
    companion object {
        val default: QueryState = QueryState(
            query = null,
            loading = false
        )
    }
}
