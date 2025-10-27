package com.bcntransit.app.model

import com.bcntransit.app.model.transport.NearbyStation
import org.maplibre.android.annotations.Icon

data class MarkerInfo(
    val station: NearbyStation,
    val normalIcon: Icon,
    val enlargedIcon: Icon
)