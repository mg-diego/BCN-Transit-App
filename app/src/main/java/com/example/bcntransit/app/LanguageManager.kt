package com.bcntransit.app.util

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LanguageManager {

    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_LANG = "app_language"

    /**
     * Guarda el idioma seleccionado y lo aplica.
     * Si es Android 13+, usa la API del sistema.
     * Si es anterior, guarda en SharedPreferences y recrea la activity.
     */
    fun setLocale(context: Context, languageCode: String) {
        // 1. Guardar en SharedPreferences (para versiones antiguas)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, languageCode).apply()

        // 2. Aplicar el cambio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+: Usamos la API nativa (esto no requiere AppCompatActivity)
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            // Android 12 o inferior: Usamos AppCompatDelegate (Funciona en ComponentActivity
            // para gestionar los recursos, aunque requiere un reinicio manual)
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    /**
     * Obtiene el código de idioma actual (es, en, ca)
     */
    fun getCurrentLanguage(context: Context): String {
        // Intentar leer de la API moderna primero
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!locales.isEmpty) return locales.get(0).language
        }

        // Fallback: AppCompatDelegate
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (!appLocales.isEmpty) return appLocales.get(0)?.language ?: "es"

        // Fallback final: SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, Locale.getDefault().language) ?: "es"
    }
}