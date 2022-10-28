package com.catelt.mome.domain.usecase

import com.catelt.mome.data.repository.config.ConfigRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpeechToTextAvailableUseCaseImpl @Inject constructor(
    private val configRepository: ConfigRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return configRepository.getSpeechToTextAvailable()
    }
}