package com.example.bcntransit.screens.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedProgressBar(
    firstCategoryStr: String,
    firstCategoryValue: Int,
    secondCategoryStr: String,
    secondCategoryValue: Int,
    thirdCategoryStr: String,
    thirdCategoryValue: Int,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp
) {
    // Calcular el total
    val total = firstCategoryValue + secondCategoryValue + thirdCategoryValue
    val firstWeight = if (total != 0) firstCategoryValue.toFloat() / total else 0f
    val secondWeight = if (total != 0) secondCategoryValue.toFloat() / total else 0f
    val thirdWeight = if (total != 0) thirdCategoryValue.toFloat() / total else 0f

    Column(modifier = modifier) {
        // Barra segmentada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color.LightGray, shape = MaterialTheme.shapes.small)
        ) {
            // First category
            Box(
                modifier = Modifier
                    .weight(firstWeight)
                    .fillMaxHeight()
                    .background(Color(0xFF2196F3))
            )

            // Second category
            Box(
                modifier = Modifier
                    .weight(secondWeight)
                    .fillMaxHeight()
                    .background(Color(0xFFFF9800))
            )

            // Third category
            Box(
                modifier = Modifier
                    .weight(thirdWeight)
                    .fillMaxHeight()
                    .background(Color(0xFF9C27B0))
            )
        }

        // Leyenda
        Spacer(modifier = Modifier.height(8.dp))
        Column (
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF2196F3), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("$firstCategoryStr ($firstCategoryValue)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFFFF9800), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("$secondCategoryStr ($secondCategoryValue)")
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF9C27B0), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("$thirdCategoryStr ($thirdCategoryValue)")
            }
        }
    }
}