package com.pagzone.sonavi.data.repository

import com.pagzone.sonavi.data.store.ProfileSettingsDataStore
import com.pagzone.sonavi.model.ProfileSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileSettingsRepository @Inject constructor(
    private val profileSettingsDataStore: ProfileSettingsDataStore
) {
    fun getProfileSettings(): Flow<ProfileSettings> = profileSettingsDataStore.profileFlow

    suspend fun updateProfile(name: String, address: String?): Result<Unit> {
        return try {
            profileSettingsDataStore.updateProfile(name.trim(), address?.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateName(name: String): Result<Unit> {
        return try {
            profileSettingsDataStore.updateName(name.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAddress(address: String): Result<Unit> {
        return try {
            profileSettingsDataStore.updateAddress(address.trim())
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