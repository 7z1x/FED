package bangkit.project.fed.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesDataStore private constructor(private val dataStore: DataStore<Preferences>) {

    private val THEME_KEY = booleanPreferencesKey("theme_setting")
    private val LOCALE_KEY = stringPreferencesKey("local")
    private val PROFILE_IMAGE_PATH_KEY = stringPreferencesKey("profile_image_path")

    fun getThemeSetting() : Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: false
        }
    }

    suspend fun saveThemeSetting(isDarkModeActive: Boolean) {
        dataStore.edit {preferences ->
            preferences[THEME_KEY] = isDarkModeActive
        }
    }

    fun getLocaleSetting(): Flow<String> {
        return dataStore.data.map {
            it[LOCALE_KEY] ?: "en"
        }
    }

    suspend fun saveLocaleSetting(localeName: String) {
        dataStore.edit {
            it[LOCALE_KEY] = localeName
        }
    }

    suspend fun saveProfileImagePath(path: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_PATH_KEY] = path
        }
    }

    fun getProfileImagePath(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PROFILE_IMAGE_PATH_KEY]
        }
    }

    companion object{
        @Volatile
        private var INSTANCE: PreferencesDataStore? = null

        fun getInstance(dataStore: DataStore<Preferences>): PreferencesDataStore{
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesDataStore(dataStore)
                INSTANCE = instance
                instance
            }
        }

    }

}