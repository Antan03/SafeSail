package no.uio.ifi.in2000.sofiaalo.team44.data.tutorial

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TutorialRepository(private val context: Context) {
    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[hasSeenTutorial] = completed
        }
    }
    private val hasSeenTutorial = booleanPreferencesKey("has_seen_tutorial")

    // vis tuto
    val showTutorial: Flow<Boolean> = context.dataStore.data
        .map {it[hasSeenTutorial] ?: false
        }
}



