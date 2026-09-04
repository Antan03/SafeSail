# Team 44 – Case 2. Redd Titanic!
<!-- Åpne i preview for å se formatert -->

# Safesail
<!-- Skal vise logo, skal funke i github men ikke i preview her -->
![Safesail logo](app/src/main/ic_launcher-playstore.png)


**Gruppemedlemmer:** Anders, Daniel, Kishani, Magnus, Samantha, Sofia

---

## Om appen

Appen hjelper skipsnavigasjon i arktiske farvann ved å vise isfjell, simulere isfjelldrift og beregne kollisjonssannsynlighet mellom planlagte skipsruter og aktive isfjellbaner.

---

## Kjøre appen

### Krav
- Android Studio Hedgehog eller nyere
- Android-enhet eller emulator med API-nivå 29 (Android 10) eller høyere
- Internettforbindelse for å laste isfjelldata, værvarsler og iskart
- Appen krever **ingen** lokasjonstillatelse

### Oppsett av påloggingsinformasjon
Appen bruker Drifty-APIet for isfjellsimuleringer. Legg til følgende i `local.properties` i rotmappen:

```
drifty_username=BRUKERNAVN
drifty_password=PASSORD
```

Kontakt gruppen for tilgang til brukernavn og passord.

### Bygge og kjøre
1. Klon repoet og åpne prosjektet i Android Studio
2. Legg til `local.properties` som beskrevet over
3. Trykk **Sync Project with Gradle Files**
4. Koble til enhet eller start emulator
5. Trykk **Run**

---

## Biblioteker

### Vist i kurset
| Bibliotek                  | Bruk                                      |
|----------------------------|-------------------------------------------|
| Jetpack Compose            | UI-rammeverk                              |
| ViewModel + StateFlow      | MVVM-arkitektur og tilstandshåndtering    |
| Kotlin Coroutines          | Asynkron kode                             |
| Navigation Compose         | Navigasjon mellom skjermer                |
| Coil                       | Lasting av bilder fra URL                 |
| Ktor Client                | HTTP-klient for nettverkskall             |
| kotlinx.serialization      | JSON-serialisering av API-responser       |

**DataStore Preferences** (`androidx.datastore:datastore-preferences`)
Brukes til vedvarende lokal lagring av brukerinnstillinger som tutorial-status, animasjonshastighet og antall partikler.

**MapLibre GL Android** (`org.maplibre.gl:android-sdk`)
Kartrammeverk for rendering av interaktive kart. Brukes til å vise isfjell, skipsruter, simulerte driftsbaner og iskart som kartlag. MapLibre er et open-source alternativ til Mapbox.

**NetCDF (Unidata CDM)** (`edu.ucar:cdm-core`, `edu.ucar:netcdf4`)
Bibliotek for lesing av NetCDF-filer (.nc), et vitenskapelig filformat brukt av blant annet meteorologiske institusjoner. Drifty returnerer simuleringsresultater i dette formatet. Filene inneholder flerdimensjonale arrays med posisjon og tid for hvert simulerte isfjellpartikkel.

**Mapbox Turf** (`com.mapbox.mapboxsdk:mapbox-sdk-turf`)
Geometriberegningsbibliotek. Brukes til å tegne og beregne geografiske former som sirkler og polygoner på kartet.

---

## API-er

| API                | Bruk                                          |
|--------------------|-----------------------------------------------|
| MET Iceberg API    | Henter isfjellobservasjoner for valgt dato    |
| MET MetAlerts API  | Henter marine farevarsler                     |
| Victoria WMS (MET) | Iskart som WMS-kartlag                        |
| Drifty (MET)       | Kjører OpenDrift-simuleringer av isfjelldrift |

---
