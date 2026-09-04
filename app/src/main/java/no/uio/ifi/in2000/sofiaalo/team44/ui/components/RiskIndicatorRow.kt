package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.sofiaalo.team44.model.drifty.SavedSimulationUi
import no.uio.ifi.in2000.sofiaalo.team44.ui.probability.ProbabilityViewModel
import java.time.Instant

@Composable
fun RiskIndicatorRow(
    probabilityViewModel: ProbabilityViewModel,
    activeIcebergs: List<SavedSimulationUi>,
    routeStartMs: Long
) {
    val collisionRisks by probabilityViewModel.riskResults.collectAsState()
    val isCalculating by probabilityViewModel.isCalculating.collectAsState()

    when {
        isCalculating -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Beregner risiko...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        collisionRisks.isEmpty() -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Beregner risiko...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            val maxRisk = activeIcebergs.mapNotNull { collisionRisks[it.uuid] }.maxOrNull()
                ?: return

            val (color, label) = when {
                maxRisk >= 50.0 -> Color.Red to "Høy risiko"
                maxRisk >= 20.0 -> Color(0xFFFF6600) to "Moderat risiko"
                maxRisk >= 5.0 -> Color(0xFFCCCC00) to "Lav risiko"
                else -> Color(0xFF2E7D32) to "Trygg"
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        "%.1f%%".format(maxRisk),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                    items(activeIcebergs) { iceberg ->
                        val risk = collisionRisks[iceberg.uuid] ?: return@items
                        val coords = iceberg.response.spec.geo.point

                        val activeRange = runCatching {
                            val icebergStartMs = Instant.parse(coords.time).toEpochMilli()
                            val startH = ((icebergStartMs - routeStartMs) / 3_600_000L).toInt()
                            val endH = startH + iceberg.response.spec.simulationDurationInHours
                            "T+${startH}h – T+${endH}h"
                        }.getOrNull()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "%.2f°, %.2f°".format(coords.latitude, coords.longitude),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "%.1f%%".format(risk),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (activeRange != null) {
                                Text(
                                    activeRange,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}