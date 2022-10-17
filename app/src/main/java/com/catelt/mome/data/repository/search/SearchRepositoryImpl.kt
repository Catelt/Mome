package com.catelt.mome.data.repository.search

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.search.SearchResult
import com.catelt.mome.data.paging.SearchResponsePagingDataSource
import com.catelt.mome.data.remote.api.others.TmdbOthersApiHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val externalScope: CoroutineScope,
    private val apiOthersHelper: TmdbOthersApiHelper,
) : SearchRepository {
    override fun multiSearch(
        query: String,
        includeAdult: Boolean,
        year: Int?,
        releaseYear: Int?,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<SearchResult>> = Pager(
        PagingConfig(pageSize = 20)
    ) {
        SearchResponsePagingDataSource(
            apiOtherHelper = apiOthersHelper,
            includeAdult = includeAdult,
            query = query,
            year = year,
            releaseYear = releaseYear,
            language = deviceLanguage.languageCode
        )
    }.flow.flowOn(defaultDispatcher)
}