package no.uio.ifi.in2000.sofiaalo.team44.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.sofiaalo.team44.ui.home.HomeViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.playback.PlaybackViewModel
import no.uio.ifi.in2000.sofiaalo.team44.ui.probability.ProbabilityViewModel
import kotlin.math.roundToInt


@Composable
fun SettingsScreen(playbackViewModel: PlaybackViewModel, homeViewModel: HomeViewModel, probabilityViewModel: ProbabilityViewModel) {
    val ctx = LocalContext.current

    val animDelay = remember { mutableFloatStateOf(SettingsUtil.getAnimationDelay(ctx).toFloat()) }
    val particleCount =
        remember { mutableFloatStateOf(SettingsUtil.getParticleCount(ctx).toFloat()) }
    val lineOpacity = remember { mutableFloatStateOf(SettingsUtil.getLineOpacity(ctx)) }
    val thresholdKm = remember { mutableFloatStateOf(SettingsUtil.getThresholdKm(ctx)) }
    val animationDelaySteps = listOf(100, 150, 200, 300, 400, 500, 700, 1000, 1500, 2000)
    val delayIndex = remember {
        mutableIntStateOf(
            animationDelaySteps.indexOf(animDelay.floatValue.toInt()).coerceAtLeast(0)
        )
    }
    LaunchedEffect(Unit) {
        playbackViewModel.setParticleCount(SettingsUtil.getParticleCount(ctx))
        playbackViewModel.setAnimationDelay(SettingsUtil.getAnimationDelay(ctx))
        playbackViewModel.setLineOpacity(SettingsUtil.getLineOpacity(ctx))
        probabilityViewModel.setThresholdKm(SettingsUtil.getThresholdKm(ctx).toDouble())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Innstillinger", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { homeViewModel.startTutorial() }) {
            Text("Vis instruksjoner")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Antall partikler: ${particleCount.floatValue.toInt()}")
        Slider(
            value = particleCount.floatValue,
            onValueChange = { particleCount.floatValue = (it / 100).toInt() * 100f },
            valueRange = 100f..1000f,
            steps = 8
        )

        Spacer(modifier = Modifier.height(20.dp))


        Text("Bane-gjennomsiktighet: ${"%.2f".format(lineOpacity.floatValue)}")
        Slider(
            value = lineOpacity.floatValue,
            onValueChange = { lineOpacity.floatValue = it },
            valueRange = 0.01f..0.20f
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Animasjonsforsinkelse: ${animDelay.floatValue.toInt()} ms")
        Slider(
            value = delayIndex.intValue.toFloat(),
            onValueChange = { raw ->
                val newIndex = raw.roundToInt().coerceIn(0, animationDelaySteps.size - 1)
                delayIndex.intValue = newIndex
                animDelay.floatValue = animationDelaySteps[newIndex].toFloat()
            },
            valueRange = 0f..(animationDelaySteps.size - 1).toFloat(),
            steps = animationDelaySteps.size - 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Sikkerhetsmargin: ${thresholdKm.floatValue.toInt()} km")
        Slider(
            value = thresholdKm.floatValue,
            onValueChange = { thresholdKm.floatValue = (it / 5).toInt() * 5f },
            valueRange = 10f..200f,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            SettingsUtil.setAnimationDelay(ctx, animDelay.floatValue.toInt())
            SettingsUtil.setParticleCount(ctx, particleCount.floatValue.toInt())
            SettingsUtil.setLineOpacity(ctx, lineOpacity.floatValue)
            SettingsUtil.setThresholdKm(ctx, thresholdKm.floatValue)
            playbackViewModel.setParticleCount(particleCount.floatValue.toInt())
            playbackViewModel.setAnimationDelay(animDelay.floatValue.toInt())
            playbackViewModel.setLineOpacity(lineOpacity.floatValue)
            probabilityViewModel.setThresholdKm(thresholdKm.floatValue.toDouble())
        }) {
            Text("Lagre innstillinger")
        }
    }
}
