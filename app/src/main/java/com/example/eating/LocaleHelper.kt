package com.example.eating

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocale(baseContext: Context): Context {
        val locale = Locale(getSavedLanguage(baseContext))
        Locale.setDefault(locale)

        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)

        return baseContext.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getString("lang", "en") ?: "en"
    }

    fun saveLanguage(context: Context, lang: String) {
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("lang", lang)
            .apply()
    }
}
