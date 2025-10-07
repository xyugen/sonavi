package com.pagzone.sonavi.data.repository

import com.pagzone.sonavi.data.store.ProfileSettingsDataStore
import com.pagzone.sonavi.model.ProfileSettings
import com.pagzone.sonavi.model.Settings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileSettingsRepository @Inject constructor(
    private val profileSettingsDataStore: ProfileSettingsDataStore
) {
    fun getProfileSettings(): Flow<ProfileSettings> = profileSettingsDataStore.profileFlow

    fun getSettings(): Flow<Settings> = profileSettingsDataStore.settingsFlow

    suspend fun updateProfile(
        name: String,
        address: String?
    ): Result<Unit> {
        return try {
            profileSettingsDataStore.updateProfile(name.trim(), address?.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateHasCurrentLocation(hasCurrentLocation: Boolean): Result<Unit> {
        return try {
            profileSettingsDataStore.updateHasCurrentLocation(hasCurrentLocation)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateShouldShowCriticalInfoDialog(shouldShowCriticalInfoDialog: Boolean): Result<Unit> {
        return try {
            profileSettingsDataStore.updateShouldShowCriticalInfoDialog(shouldShowCriticalInfoDialog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearProfile(): Result<Unit> {
        return try {
            profileSettingsDataStore.clearProfile()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}