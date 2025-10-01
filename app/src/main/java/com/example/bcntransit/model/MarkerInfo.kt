package com.example.bcntransit.model

import com.example.bcntransit.model.transport.NearbyStation
import org.maplibre.android.annotations.Icon

data class MarkerInfo(
    val station: NearbyStation,
    val normalIcon: Icon,
    val enlargedIcon: Icon
)