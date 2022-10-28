package com.catelt.mome.domain.usecase

import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.search.SearchResult
import com.catelt.mome.data.repository.search.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class GetMediaMultiSearchUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<SearchResult>> {
        return searchRepository.multiSearch(query = query, deviceLanguage = deviceLanguage)
            .mapLatest { data ->
                data.filter {
                    it.overview?.isNotBlank() == true && it.posterPath != null
                }.map { it }
            }
    }
}