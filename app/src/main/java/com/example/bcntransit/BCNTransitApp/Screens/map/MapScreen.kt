package com.example.bcntransit.screens.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MapScreen(context: Context) {
    val mapView = rememberMapView(context)
    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
}
