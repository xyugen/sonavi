package com.pagzone.sonavi.data.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pagzone.sonavi.dataStore
import com.pagzone.sonavi.model.ProfileSettings
import com.pagzone.sonavi.util.Constants.DataStoreKeys.PROFILE_ADDRESS
import com.pagzone.sonavi.util.Constants.DataStoreKeys.PROFILE_LOCATION
import com.pagzone.sonavi.util.Constants.DataStoreKeys.PROFILE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileSettingsDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val PROFILE_NAME_KEY = stringPreferencesKey(PROFILE_NAME)
        private val PROFILE_ADDRESS_KEY = stringPreferencesKey(PROFILE_ADDRESS)
        private val PROFILE_LOCATION_KEY = booleanPreferencesKey(PROFILE_LOCATION)
    }

    private val dataStore = context.dataStore

    val profileFlow: Flow<ProfileSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            ProfileSettings(
                name = preferences[PROFILE_NAME_KEY] ?: "User",
                address = preferences[PROFILE_ADDRESS_KEY] ?: "",
                hasCurrentLocation = preferences[PROFILE_LOCATION_KEY] ?: false
            )
        }

    suspend fun updateProfile(name: String, address: String?) {
        dataStore.edit { preferences ->
            preferences[PROFILE_NAME_KEY] = name
            preferences[PROFILE_ADDRESS_KEY] = address ?: ""
        }
    }

    suspend fun updateName(name: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_NAME_KEY] = name
        }
    }

    suspend fun updateAddress(address: String) {
        dataStore.edit { preferences ->
            preferences[PROFILE_ADDRESS_KEY] = address
        }
    }

    suspend fun updateHasCurrentLocation(hasCurrentLocation: Boolean) {
        dataStore.edit { preferences ->
            preferences[PROFILE_LOCATION_KEY] = hasCurrentLocation
        }
    }

    suspend fun clearProfile() {
        dataStore.edit { preferences ->
            preferences.remove(PROFILE_NAME_KEY)
            preferences.remove(PROFILE_ADDRESS_KEY)
            preferences.remove(PROFILE_LOCATION_KEY)
        }
    }

    suspend fun saveThemePreference(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("dark_mode")] = isDark
        }
    }

    fun getThemePreference(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("dark_mode")] ?: false
        }
    }
}