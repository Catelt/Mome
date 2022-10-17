package com.catelt.mome.domain.usecase

import androidx.paging.PagingData
import com.catelt.mome.data.model.DeviceLanguage
import com.catelt.mome.data.model.search.SearchResult
import com.catelt.mome.data.repository.search.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMediaMultiSearchUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository
) {
    operator fun invoke(
        query: String,
        deviceLanguage: DeviceLanguage
    ): Flow<PagingData<SearchResult>> {
        return searchRepository.multiSearch(query = query, deviceLanguage = deviceLanguage)
    }
}