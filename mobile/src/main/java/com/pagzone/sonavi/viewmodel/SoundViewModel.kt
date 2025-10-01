package com.pagzone.sonavi.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.domain.SoundEmbeddingModel
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.model.UiState
import com.pagzone.sonavi.util.Helper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.sqrt

@HiltViewModel
class SoundViewModel @Inject constructor(
    private val repository: SoundRepository,
    @param:ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val sounds = repository
        .getAllSounds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val snoozeStatuses: StateFlow<Map<Long, Boolean>> = _uiState
        .map { it.snoozeStatuses }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Only track sounds that are actually snoozed
    private val snoozeJobs = mutableMapOf<Long, Job>()

    init {
        // Wait for actual sounds to be loaded (skip empty initial value)
        viewModelScope.launch {
            sounds
                .filter { it.isNotEmpty() } // Skip empty list
                .take(1) // Take first non-empty emission
                .collect { soundList ->
                    soundList.forEach { sound ->
                        Log.d("SoundViewModel", "${sound.name}: ${sound.snoozedUntil}")
                        if (isSnoozed(sound)) {
                            startSnoozeTimer(sound.id, sound.snoozedUntil!!)
                        }
                    }
                }
        }
    }

    fun getAllSounds(): Flow<List<SoundProfile>> {
        return repository.getAllSounds()
    }

    suspend fun getActiveSounds(): List<SoundProfile> {
        return repository.getActiveSounds()
    }

    suspend fun getBuiltInSounds(): List<SoundProfile> {
        return repository.getBuiltInSounds()
    }

    suspend fun getCustomSounds(): List<SoundProfile> {
        return repository.getCustomSounds()
    }

    fun updateSoundProfile(
        soundId: Long,
        threshold: Float? = null,
        vibrationPattern: List<Long>? = null,
        isCritical: Boolean? = null,
        displayName: String? = null
    ) = viewModelScope.launch {
        try {
            repository.updateSoundProfile(
                soundId,
                threshold,
                vibrationPattern,
                isCritical,
                displayName
            )
            _uiState.value = _uiState.value.copy(message = "Sound profile updated successfully")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun updateSoundProfile(soundProfile: SoundProfile) = viewModelScope.launch {
        try {
            repository.updateSoundProfile(soundProfile.id, fullProfile = soundProfile)
            _uiState.value = _uiState.value.copy(message = "Sound profile updated successfully")
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    private fun isSnoozed(sound: SoundProfile): Boolean {
        return sound.snoozedUntil?.after(Date()) == true
    }

    // Start individual timer for a specific sound
    private fun startSnoozeTimer(soundId: Long, snoozeUntil: Date) {
        // Cancel existing job if any
        snoozeJobs[soundId]?.cancel()

        val delayMs = snoozeUntil.time - System.currentTimeMillis()
        if (delayMs <= 0) {
            // Already expired, remove from snooze immediately
            removeFromSnooze(soundId)
            return
        }

        snoozeJobs[soundId] = viewModelScope.launch {
            try {
                delay(delayMs)
                // Snooze expired, remove from UI state
                removeFromSnooze(soundId)
            } catch (_: CancellationException) {
                // Timer was cancelled (expected behavior)
            }
        }

        // Update UI state immediately
        _uiState.update { currentState ->
            currentState.copy(
                snoozeStatuses = currentState.snoozeStatuses + (soundId to true)
            )
        }
    }

    private fun removeFromSnooze(soundId: Long) {
        snoozeJobs.remove(soundId)?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                snoozeStatuses = currentState.snoozeStatuses - soundId
            )
        }

        // Clear the database field
        viewModelScope.launch {
            repository.clearSnooze(soundId)
        }
    }

    // Public function to snooze a sound
    fun snoozeSound(soundId: Long, durationMinutes: Int) {
        viewModelScope.launch {
            val snoozeUntil = Date(System.currentTimeMillis() + durationMinutes * 60 * 1000)

            // Update database
            repository.updateSnoozeUntil(soundId, snoozeUntil)

            // Start timer
            startSnoozeTimer(soundId, snoozeUntil)
        }
    }

    // Function to manually unsnooze
    fun unsnoozeSound(soundId: Long) {
        removeFromSnooze(soundId)
    }

    override fun onCleared() {
        super.onCleared()
        // Cancel all snooze timers
        snoozeJobs.values.forEach { it.cancel() }
        snoozeJobs.clear()
    }

    fun toggleSoundProfile(id: Long) {
        viewModelScope.launch {
            repository.toggleSoundProfile(id)
        }
    }

    fun setSoundProfileEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundProfileEnabled(id, enabled)
        }
    }

    fun createCustomSound(
        name: String,
        audioSamples: List<FloatArray>, // 3-5 recordings from user
        threshold: Float = 0.80f // Default threshold (tune based on testing)
    ) {
        viewModelScope.launch {
            val embeddingModel = SoundEmbeddingModel(appContext)

            try {
                Log.d("ViewModel", "Creating custom sound: $name with ${audioSamples.size} samples")

                // Extract embeddings from all samples
                val embeddings = audioSamples.mapIndexed { index, audio ->
                    Log.d("ViewModel", "Processing sample ${index + 1}/${audioSamples.size}")
                    embeddingModel.extractEmbeddingFromLongAudio(audio)
                }

                // Average embeddings to create prototype
                val prototypeEmbedding = Helper.averageEmbeddings(embeddings)

                // Optional: L2 normalize for better similarity matching
                val normalizedEmbedding = Helper.normalizeEmbedding(prototypeEmbedding)

                // Calculate quality metrics (optional but useful)
                val quality = Helper.calculateEmbeddingQuality(embeddings)
                Log.d("ViewModel", "Embedding quality - variance: ${quality.first}, consistency: ${quality.second}")

                // Store as JSON string
                val embeddingJson = Gson().toJson(normalizedEmbedding)

                // Save to database
                val customSound = SoundProfile(
                    name = name,
                    displayName = name,
                    isBuiltIn = false,
                    mfccEmbedding = embeddingJson,
                    threshold = threshold
                )

                Log.d("SoundViewModel", "Embedding JSON: $embeddingJson")

                repository.addCustomSound(customSound)
                Log.d("SoundViewModel", "Custom sound saved successfully")
            } catch (e: Exception) {
                Log.e("SoundViewModel", "Error creating custom sound", e)
                // Handle error (show to user)
            } finally {
                embeddingModel.cleanup()
            }
        }
    }
}