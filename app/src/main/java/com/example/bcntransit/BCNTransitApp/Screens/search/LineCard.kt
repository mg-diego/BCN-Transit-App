package com.example.bcntransit.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bcntransit.R
import com.example.bcntransit.model.LineDto

@Composable
fun LineCard(line: LineDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() }, // <-- aquí usamos el lambda
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val iconRes = when (line.transport_type) {
                "metro" -> when (line.name) {
                    "L1"  -> R.drawable.metro_l1
                    "L2"  -> R.drawable.metro_l2
                    "L3"  -> R.drawable.metro_l3
                    "L4"  -> R.drawable.metro_l4
                    "L5"  -> R.drawable.metro_l5
                    "L9N" -> R.drawable.metro_l9n
                    "L9S" -> R.drawable.metro_l9s
                    "L10N"-> R.drawable.metro_l10n
                    "L10S"-> R.drawable.metro_l10s
                    "L11" -> R.drawable.metro_l11
                    else -> R.drawable.metro
                }
                "rodalies" -> when(line.code) {
                    "R1" -> R.drawable.rodalies_r1
                    "R2" -> R.drawable.rodalies_r2
                    "R2N"-> R.drawable.rodalies_r2_nord
                    "R2S"-> R.drawable.rodalies_r2_sud
                    "R3" -> R.drawable.rodalies_r3
                    "R4" -> R.drawable.rodalies_r4
                    "R7" -> R.drawable.rodalies_r7
                    "R8" -> R.drawable.rodalies_r8
                    "R11"-> R.drawable.rodalies_r11
                    "R13"-> R.drawable.rodalies_r13
                    "R14"-> R.drawable.rodalies_r14
                    "R15"-> R.drawable.rodalies_r15
                    "R16"-> R.drawable.rodalies_r16
                    "R17"-> R.drawable.rodalies_r17
                    "RG1"-> R.drawable.rodalies_rg1
                    "RT1"-> R.drawable.rodalies_rt1
                    "RT2"-> R.drawable.rodalies_rt2
                    "RL3"-> R.drawable.rodalies_rl3
                    "RL4"-> R.drawable.rodalies_rl4
                    else -> R.drawable.rodalies
                }
                "tram" -> when(line.name) {
                    "T1" -> R.drawable.tram_t1
                    "T2" -> R.drawable.tram_t2
                    "T3" -> R.drawable.tram_t3
                    "T4" -> R.drawable.tram_t4
                    "T5" -> R.drawable.tram_t5
                    "T6" -> R.drawable.tram_t6
                    else -> R.drawable.tram
                }
                "fgc" -> when(line.code) {
                    "L6" -> R.drawable.fgc_l6
                    "L7" -> R.drawable.fgc_l7
                    "L8" -> R.drawable.fgc_l8
                    "L12"-> R.drawable.fgc_l12
                    "S1" -> R.drawable.fgc_s1
                    "S2" -> R.drawable.fgc_s2
                    "S3" -> R.drawable.fgc_s3
                    "S4" -> R.drawable.fgc_s4
                    "S5" -> R.drawable.fgc_s5
                    "S7" -> R.drawable.fgc_s7
                    "S8" -> R.drawable.fgc_s8
                    "S9" -> R.drawable.fgc_s9
                    "R5" -> R.drawable.fgc_r5
                    "R6" -> R.drawable.fgc_r6
                    "R50"-> R.drawable.fgc_r50
                    "R60"-> R.drawable.fgc_r60
                    "RL1"-> R.drawable.fgc_rl1
                    "RL2"-> R.drawable.fgc_rl2
                    "FV" -> R.drawable.fgc_fv
                    "MM" -> R.drawable.fgc_mm
                    else -> R.drawable.fgc
                }
                "bus" -> R.drawable.bus
                "bicing" -> R.drawable.bicing
                else -> R.drawable.rodalies_r2_nord
            }

            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(42.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            val alertText = if (line.has_alerts) "Incidencias" else "Servicio normal"
            val alertColor = if (line.has_alerts) Color.Red else Color.Green

            Column {
                Text(line.description, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(alertColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(alertText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
