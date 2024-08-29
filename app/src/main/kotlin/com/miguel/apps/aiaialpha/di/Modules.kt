package com.miguel.apps.aiaialpha.di

import com.miguel.apps.aiaialpha.data.db.ChatDatabase
import com.miguel.apps.aiaialpha.ui.viewmodel.ChatViewModel
import com.miguel.apps.aiaialpha.ui.viewmodel.GroupViewModel
import com.miguel.apps.aiaialpha.repo.ChatRepository
import com.miguel.apps.aiaialpha.repo.ChatRepositoryImpl
import com.miguel.apps.aiaialpha.repo.GeminiAIRepo
import com.miguel.apps.aiaialpha.repo.GeminiAIRepoImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::GeminiAIRepoImpl) { bind<GeminiAIRepo>() }
}

val viewModelModule = module {
    viewModelOf(::ChatViewModel)
    viewModelOf(::GroupViewModel)
}
val databaseModule = module {
    single { ChatDatabase.getInstance(androidApplication()) }
    singleOf(::ChatRepositoryImpl){ bind<ChatRepository>()}
}