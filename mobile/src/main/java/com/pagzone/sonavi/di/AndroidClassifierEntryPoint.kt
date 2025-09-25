package com.pagzone.sonavi.di

import com.pagzone.sonavi.data.repository.EmergencyContactRepository
import com.pagzone.sonavi.data.repository.SoundRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AudioClassifierEntryPoint {
    fun getSoundRepository(): SoundRepository
    fun getEmergencyContactRepository(): EmergencyContactRepository
}