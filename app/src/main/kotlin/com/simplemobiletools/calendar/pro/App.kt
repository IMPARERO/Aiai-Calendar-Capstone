package com.simplemobiletools.calendar.pro

import androidx.multidex.MultiDexApplication
import com.miguel.apps.aiaialpha.di.appModule
import com.miguel.apps.aiaialpha.di.databaseModule
import com.miguel.apps.aiaialpha.di.viewModelModule
import com.simplemobiletools.commons.extensions.checkUseEnglish
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@App)
            // Load modules
            modules(appModule, viewModelModule, databaseModule)
        }
    }

}
