package com.catelt.mome.data.repository.search

import androidx.paging.PagingData
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.search.SearchResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun multiSearch(
        query: String,
        includeAdult: Boolean = false,
        year: Int? = null,
        releaseYear: Int? = null,
        deviceLanguage: DeviceLanguage = DeviceLanguage.default
    ): Flow<PagingData<SearchResult>>
}