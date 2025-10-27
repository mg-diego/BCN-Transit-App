package com.example.bcntransit.BCNTransitApp.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.bcntransit.app.R

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = colorResource(R.color.medium_red),
            checkedTrackColor = colorResource(R.color.medium_red).copy(alpha = 0.5f)
        )
    )
}