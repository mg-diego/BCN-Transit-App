package com.example.bcntransit.BCNTransitApp.Screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class SettingsState(
    val receiveAlerts: Boolean = true,
    val darkTheme: Boolean = false
)

class SettingsViewModel(
    private val context: Context,
    private val androidId: String
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    companion object {
        private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
    }

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                val darkTheme = preferences[DARK_THEME_KEY] ?: false

                try {
                    val receiveAlerts = ApiClient.userApiService.getUserNotificationsConfiguration(androidId)
                    _state.value = SettingsState(
                        receiveAlerts = receiveAlerts,
                        darkTheme = darkTheme
                    )
                } catch (e: Exception) {
                    _state.value = SettingsState(
                        receiveAlerts = true,
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }

    fun toggleReceiveAlerts(enabled: Boolean) {
        viewModelScope.launch {
            try {
                ApiClient.userApiService.toggleUserNotifications(androidId, enabled)
                _state.value = _state.value.copy(receiveAlerts = enabled)
            } catch (e: Exception) {
                // Manejar error (podrías mostrar un toast o mensaje)
            }
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[DARK_THEME_KEY] = enabled
            }
            _state.value = _state.value.copy(darkTheme = enabled)
        }
    }
}

// Factory
class SettingsViewModelFactory(
    private val context: Context,
    private val androidId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(context, androidId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}