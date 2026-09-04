package no.uio.ifi.in2000.sofiaalo.team44.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

data class TutorialStep(val title: String, val description: String)

@Composable
fun TutorialOverlay(onDismiss: () -> Unit) {
    val steps = listOf(

        TutorialStep("Velkommen", "Trykk på neste for å gå videre."
        ),

        TutorialStep("Lag-kontroller","Øverst til venstre på kartet finner du lag-kontrollene. " +
                "Her kan du skru av og på hva som vises på kartet, som isfjell, skipsrute, simuleringer og farevarsler."
        ),

        TutorialStep("Datovalg","Nederst til venstre kan du velge dato og klokkeslett. " +
                "Dette bestemmer hvilken dato Isfjell, Iskart og Værvarsler er hentet fra, og setter starttidspunktet for skipsruten din."
        ),

        TutorialStep("Ruteplanlegging",
            "For å starte, trykk hvor som helst der det er vann, det vil da være startposisjonen. " +
                "Du må selv manuelt manøvrere ruta rundt landområder med etapper/punkter. " +
                "For å se/slette punkter på kartet, dra opp panelet over navigasjonslinjen. " +
                "Her kan du også velge fart og se hvor lang tid reisen tar. "
        ),

        TutorialStep("Isfjell", "Trykk på et isfjell for informasjon om det valgte isfjellet og mulighet for å starte simulering av isfjellets kurs. " +
                "Simuleringene lagres under \"Simuleringer\".\n" +
                "Etter påbegynt ruteplanlegging vil de relevante isfjellene få en grønn, oransje eller rød sirkel rundt seg for å vise farenivå. "
        ),

        TutorialStep("Simulerte isfjell", "Gå til \"Simuleringer\" via knappen nederst. " +
                "Her aktiverer du simuleringer du har lagret — disse brukes både for å vise mulige driftsbaner på kartet og for å beregne risikoanalysen. " +
                "Du kan aktivere flere simuleringer samtidig, men det er en grense på totalt antall timer. " +
                "Denne grensen kan justeres i Innstillinger ved å endre antall partikler — færre partikler gir plass til flere timer, men kan redusere nøyaktigheten."
        ),

        TutorialStep("Simuleringer",  "Simuleringer predikerer mulige driftsbaner for et isfjell over tid, basert på observasjonsdata. " +
                "Start en simulering ved å trykke på et isfjell på kartet. Etter at den er lagret, aktiverer du den i \"Simuleringer\". " +
                "Du kan deretter se animasjonen i \"Simulering\"-modus via panelet nederst på hjemskjermen, eller i 'Følg rute'-modus hvis tidspunktet på ruten overlapper med simuleringens tidsrom."

        ),

        TutorialStep("Risikoanalyse", "Trykk på fare-ikonet øverst til høyre på hjemskjermen. " +
                "Risikoanalysen beregner sannsynligheten for at skipets rute og aktive isfjell sine predikerte baner overlapper i tid og rom. " +
                "Du trenger en planlagt rute og minst én aktiv simulering. Sikkerhetsmargin kan justeres i Innstillinger." ),


        TutorialStep("Farevarsel", "Farevarseler dukker opp som fargede polygoner på kartet. " +
                "Fargen viser alvorlighetsgrad.\n" +
                "\n" +
                "Grønn = Lite\n" +
                "Gul = Moderat\n" +
                "Oransje = Alvorlig\n" +
                "Rød = Ekstrem\n"
        ),

        TutorialStep("Iskart", "Iskart-laget viser havisdekke basert på observasjonsdata. " +
                "Det kan slås av og på under lag-kontrollene.\n" +
                "\n" +
                "Fargene viser isdekningsgrad på en skala fra 0 til 10:\n" +
                "\n" +
                "Grå = Fastis (10)\n" +
                "Rød = Tett drivis (9–10)\n" +
                "Oransje = Nær drivis (7–9)\n" +
                "Gul = Åpen drivis (4–7)\n" +
                "Grønn = Spredt drivis (1–4)\n" +
                "Blå = Åpent hav (0–1)\n"
        ),

        TutorialStep("Ferdig", "Du kan alltid se denne veiledningen igjen inne i 'Innstillinger'.")

    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val isLastStep = currentStepIndex == steps.lastIndex

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = steps[currentStepIndex].title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = steps[currentStepIndex].description,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        if (isLastStep) onDismiss() else currentStepIndex++
                    }) {
                        Text(if (isLastStep) "Ferdig" else "Neste")
                    }
                }
            }
        }
    }
}