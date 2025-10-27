package com.bcntransit.app.widget

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bcntransit.app.api.ApiClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {
    fun registerUser(androidId: String) {
        viewModelScope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                val body = mapOf(
                    "fcmToken" to fcmToken
                )

                val response = ApiClient.userApiService.registerUser(androidId, body)

                if (response) {
                    Log.d("UserRegister", "User registered successfully: androidId=$androidId")
                } else {
                    Log.w("UserRegister", "User already exists or token updated: androidId=$androidId")
                }

            } catch (e: retrofit2.HttpException) {
                Log.e("UserRegister", "HTTP error: ${e.code()} - ${e.message()}")
            } catch (e: java.net.SocketTimeoutException) {
                Log.e("UserRegister", "Timeout error: ${e.message}")
            } catch (e: Exception) {
                Log.e("UserRegister", "Unexpected error: ${e.localizedMessage}", e)
            }
        }
    }
}