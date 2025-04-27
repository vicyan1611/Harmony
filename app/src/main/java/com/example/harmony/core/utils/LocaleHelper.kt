package com.example.harmony.core.utils // Adjust package name as needed

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.harmony.domain.model.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun updateLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.localeTag) // Get locale from your enum
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        configuration.setLocale(locale)

        // For API 24+ setLocales is available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        }

        // Update the context configuration
        @Suppress("DEPRECATION") // updateConfiguration is required for older APIs
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        // Create and return a new context wrapper for the updated configuration
        return context.createConfigurationContext(configuration)
    }

    // Function to wrap the base context in Activities
    fun onAttach(baseContext: Context, language: AppLanguage): Context {
        return updateLocale(baseContext, language)
    }
}