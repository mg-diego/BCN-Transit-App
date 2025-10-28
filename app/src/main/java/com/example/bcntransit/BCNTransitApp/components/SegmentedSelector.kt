package com.bcntransit.app.BCNTransitApp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedSelector(
    options: List<String>,
    direction: String,
    onOptiondirection: (String) -> Unit,
    modifier: Modifier = Modifier,
    lineColor: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        options.forEachIndexed { index, label ->
            val isdirection = direction == label

            Surface(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                color = if (isdirection) lineColor else lineColor.copy(alpha = 0x0F / 255f),
                contentColor = if (isdirection) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f) // ocupa el mismo espacio cada opción
                    .padding(horizontal = 2.dp) // pequeño espacio entre botones
                    .clickable { onOptiondirection(label) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isdirection) androidx.compose.ui.text.font.FontWeight.Bold
                            else androidx.compose.ui.text.font.FontWeight.Normal
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(3.dp)
                    )
                }
            }
        }
    }
}*/

@Composable
fun SegmentedSelector(
    options: List<String>,
    direction: String,
    onSelectedOption: (String) -> Unit,
    lineColor: Color
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF0F0F0))
            .border(1.dp, Color.LightGray, RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = direction == label

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) lineColor else Color.Transparent)
                    .clickable { onSelectedOption(label) }
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
