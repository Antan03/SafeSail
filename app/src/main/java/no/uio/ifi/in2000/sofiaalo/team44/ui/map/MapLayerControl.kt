package no.uio.ifi.in2000.sofiaalo.team44.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.sofiaalo.team44.model.map.MapLayers

// For å skru av og på layers
@Composable
fun LayerControls(
    layers: MapLayers,
    onLayersChanged: (MapLayers) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Layers,
                contentDescription = "Lag"
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column( modifier = Modifier
                    .padding(4.dp)
                    .width(IntrinsicSize.Max)
                ) {
                    LayerToggleItem(
                        label = "Havis",
                        checked = layers.showVictoriaLayer,
                        onCheckedChange = { onLayersChanged(layers.copy(showVictoriaLayer = it)) }
                    )
                    LayerToggleItem(
                        label = "Isfjell",
                        checked = layers.showIcebergs,
                        onCheckedChange = { onLayersChanged(layers.copy(showIcebergs = it)) }
                    )
                    LayerToggleItem(
                        label = "Farevarsler",
                        checked = layers.showMetAlerts,
                        onCheckedChange = { onLayersChanged(layers.copy(showMetAlerts = it)) }
                    )
                    LayerToggleItem(
                        label = "Skipsrute",
                        checked = layers.showShipRoute,
                        onCheckedChange = { onLayersChanged(layers.copy(showShipRoute = it)) }
                    )
                    LayerToggleItem(
                        label = "Simulering",
                        checked = layers.showSimulation,
                        onCheckedChange = { onLayersChanged(layers.copy(showSimulation = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 0.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .scale(0.8f)
                .semantics { contentDescription = "$label lag ${if (checked) "på" else "av"}" }
        )
    }
}