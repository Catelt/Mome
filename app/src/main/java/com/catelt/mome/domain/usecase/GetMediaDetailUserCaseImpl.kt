package com.catelt.mome.domain.usecase

import com.catelt.mome.data.model.ophim.OphimResponse
import com.catelt.mome.data.remote.api.ApiResponse
import com.catelt.mome.data.remote.api.awaitApiResponse
import com.catelt.mome.data.repository.ophim.OphimRepository
import javax.inject.Inject

class GetMediaDetailUserCaseImpl @Inject constructor(
    private val ophimRepository: OphimRepository
) {
    suspend operator fun invoke(
        slugName: String,
    ): ApiResponse<OphimResponse> {
        return ophimRepository.getMediaDetail(
            slugName = slugName,
        ).awaitApiResponse()
    }
}