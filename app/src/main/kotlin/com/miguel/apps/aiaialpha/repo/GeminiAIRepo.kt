package com.miguel.apps.aiaialpha.repo

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.SafetySetting

interface GeminiAIRepo {
    fun provideConfig(): GenerationConfig

    fun getGenerativeModel(
        modelName:String,
        config: GenerationConfig,
        safetySetting: List<SafetySetting>? = null,
    ): GenerativeModel
}