package com.example.bcntransit.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bcntransit.api.ApiClient
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    fun register(androidId: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.userApiService.registerUser(androidId)
            } catch (e: Exception) {

            }
        }
    }
}