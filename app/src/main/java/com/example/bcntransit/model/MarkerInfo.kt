package com.example.bcntransit.model

import org.maplibre.android.annotations.Icon

data class MarkerInfo(
    val station: NearbyStation,
    val normalIcon: Icon,
    val enlargedIcon: Icon
)