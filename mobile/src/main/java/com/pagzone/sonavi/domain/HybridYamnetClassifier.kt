package com.pagzone.sonavi.domain

import android.content.Context
import android.util.Log
import com.pagzone.sonavi.data.repository.SoundRepository
import com.pagzone.sonavi.di.AudioClassifierEntryPoint
import com.pagzone.sonavi.model.SoundProfile
import com.pagzone.sonavi.util.Constants
import com.pagzone.sonavi.util.Helper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.util.Date
import kotlin.math.abs
import kotlin.math.sqrt

class HybridYamnetClassifier(
    context: Context
) {
    private val soundRepository: SoundRepository by lazy {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AudioClassifierEntryPoint::class.java
        )
        hiltEntryPoint.getSoundRepository()
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var sounds: List<SoundProfile> = emptyList()

    private val interpreter: Interpreter
    private val labels: List<String>

    private val emaScores = mutableMapOf<SoundProfile, Float>()

    // Environment awareness (enhanced)
    private var environmentProfile = EnvironmentProfile()
    private val noiseHistory = ArrayDeque<Float>(20) // Track noise over time

    private val customSoundClassifier = CustomSoundClassifier(context)

    enum class EnvironmentType {
        VERY_QUIET,   // Library, bedroom at night
        QUIET,        // Normal room
        NORMAL,       // Office, home
        NOISY,        // Street, café
        VERY_NOISY    // Construction, crowd
    }

    init {
        val modelBuffer = Helper.loadModelFile(context, "yamnnet.tflite")
        interpreter = Interpreter(modelBuffer)

        coroutineScope.launch {
            soundRepository.getAllSounds().collect { value ->
                sounds = value
                emaScores.clear()
            }
        }

        labels = context.assets.open("yamnet_labels.txt").bufferedReader().readLines()
    }

    private fun mergePredictions(yamnetScores: FloatArray): Map<SoundProfile, Float> {
        val merged = mutableMapOf<SoundProfile, Float>()

        for (sound in sounds) {
            if (sound.yamnetIndices.isEmpty()) {
                merged[sound] = 0f
                continue
            }

            // Get individual scores for this sound's indices
            val indexScores = sound.yamnetIndices.map { yamnetScores[it] }

            // Use top 2-3 scores with simple weighting
            val sortedScores = indexScores.sortedDescending()
            val topScore = sortedScores[0]
            val secondScore = sortedScores.getOrNull(1) ?: 0f
            val thirdScore = sortedScores.getOrNull(2) ?: 0f

            // Simple weighted combination: 90% top + 7.5% second + 2.5% third
            merged[sound] = topScore * 0.9f + secondScore * 0.075f + thirdScore * 0.025f
        }

        return merged
    }

    private fun updateEma(merged: Map<SoundProfile, Float>) {
        for ((soundProfile, score) in merged) {
            val prev = emaScores[soundProfile] ?: score
            emaScores[soundProfile] =
                Constants.Classifier.SMOOTHING_ALPHA * score +
                        (1 - Constants.Classifier.SMOOTHING_ALPHA) * prev
        }
    }

    // Enhanced environment profile update
    private fun updateEnvironmentProfile(audio: FloatArray) {
        // Calculate RMS (noise level)
        val rms = sqrt(audio.map { it * it }.average()).toFloat()

        // Add to history
        noiseHistory.addLast(rms)
        if (noiseHistory.size > 20) noiseHistory.removeFirst()

        // Update average noise level with EMA
        environmentProfile.avgNoiseLevel =
            0.1f * rms + 0.9f * environmentProfile.avgNoiseLevel

        // Calculate noise variance (stability measure)
        if (noiseHistory.size >= 5) {
            val mean = noiseHistory.average().toFloat()
            val variance = noiseHistory.map { (it - mean) * (it - mean) }.average().toFloat()
            environmentProfile.recentNoiseVariance = variance
        }

        // Classify environment type
        environmentProfile.environmentType = when {
            environmentProfile.avgNoiseLevel < 0.02f -> EnvironmentType.VERY_QUIET
            environmentProfile.avgNoiseLevel < 0.05f -> EnvironmentType.QUIET
            environmentProfile.avgNoiseLevel < 0.12f -> EnvironmentType.NORMAL
            environmentProfile.avgNoiseLevel < 0.25f -> EnvironmentType.NOISY
            else -> EnvironmentType.VERY_NOISY
        }

        Log.d(
            "HybridYamnetClassifier",
            "Environment: ${environmentProfile.environmentType}, " +
                    "Noise: ${"%.4f".format(environmentProfile.avgNoiseLevel)}, " +
                    "Variance: ${"%.4f".format(environmentProfile.recentNoiseVariance)}"
        )
    }

    // Calculate adaptive threshold for built-in sounds
    private fun getAdaptiveThreshold(sound: SoundProfile, baseThreshold: Float = 0.3f): Float {
        val envType = environmentProfile.environmentType
        val variance = environmentProfile.recentNoiseVariance

        // Base adjustment by environment type
        val environmentAdjustment = when (envType) {
            EnvironmentType.VERY_QUIET -> 0.02f   // Be stricter (raise threshold)
            EnvironmentType.QUIET -> 0.0f
            EnvironmentType.NORMAL -> -0.1f
            EnvironmentType.NOISY -> -0.05f       // Be more lenient (lower threshold)
            EnvironmentType.VERY_NOISY -> -0.10f
        }

        // Additional adjustment for unstable noise (e.g., intermittent sounds)
        val stabilityAdjustment = when {
            variance > 0.01f -> -0.03f  // High variance: be more lenient
            variance > 0.005f -> -0.01f
            else -> 0f
        }

        // Critical sounds get lower thresholds (more sensitive)
        val criticalAdjustment = if (sound.isCritical) -0.02f else 0f

        val adaptiveThreshold = (baseThreshold + environmentAdjustment +
                stabilityAdjustment + criticalAdjustment)
            .coerceIn(0.15f, 0.5f) // Keep within reasonable bounds

        Log.d(
            "AdaptiveThreshold",
            "${sound.displayName}: base=$baseThreshold, " +
                    "env=${environmentAdjustment}, " +
                    "stability=${stabilityAdjustment}, " +
                    "critical=${criticalAdjustment}, " +
                    "final=${"%.3f".format(adaptiveThreshold)}"
        )

        return adaptiveThreshold
    }

    // Calculate adaptive threshold for custom sounds
    private fun getAdaptiveThresholdForCustom(sound: SoundProfile): Float {
        val baseThreshold = sound.threshold // User-defined or default
        val envType = environmentProfile.environmentType

        val environmentAdjustment = when (envType) {
            EnvironmentType.VERY_QUIET -> 0.03f
            EnvironmentType.QUIET -> 0.01f
            EnvironmentType.NORMAL -> 0f
            EnvironmentType.NOISY -> -0.03f
            EnvironmentType.VERY_NOISY -> -0.07f
        }

        // Custom sounds are usually more specific, so adjust less aggressively
        val adaptiveThreshold = (baseThreshold + environmentAdjustment)
            .coerceIn(0.65f, 0.95f)

        return adaptiveThreshold
    }

    // Adjust scores depending on environment
    private fun adjustForEnvironment(sound: SoundProfile, score: Float): Float {
        // Boost critical sounds in noisy environments
        val noisyBoost = when {
            environmentProfile.avgNoiseLevel > 0.3f && sound.isCritical -> 1.2f
            environmentProfile.avgNoiseLevel > 0.5f -> 0.8f
            else -> 1.0f
        }

        // Reduce scores in unstable noise (prevents false positives)
        val stabilityFactor = when {
            environmentProfile.recentNoiseVariance > 0.01f -> 0.9f
            else -> 1.0f
        }

        return score * noisyBoost * stabilityFactor
    }

    // Main classify method with adaptive thresholding
    fun classify(audioInput: FloatArray): ClassificationResult {
        // Update environment first
        updateEnvironmentProfile(audioInput)

        // Separate built-in and custom sounds
        val allowedSounds = sounds.filter {
            it.isEnabled && (it.snoozedUntil == null || it.snoozedUntil.before(Date()))
        }

        val builtInSounds = allowedSounds.filter { it.isBuiltIn }
        val customSounds = allowedSounds.filter { !it.isBuiltIn && it.mfccEmbedding != null }

        // 1. Classify built-in sounds with YAMNet
        val builtInResult = if (builtInSounds.isNotEmpty()) {
            classifyBuiltIn(audioInput, builtInSounds)
        } else {
            null
        }

        // 2. Classify custom sounds with embeddings
        val customResult = if (customSounds.isNotEmpty()) {
            classifyCustom(audioInput, customSounds)
        } else {
            null
        }

        Log.d(
            "HybridClassifier",
            "Built-in: ${builtInResult?.sound?.displayName} = " +
                    "${"%.3f".format(builtInResult?.score ?: 0f)} " +
                    "(threshold: ${"%.3f".format(builtInResult?.threshold ?: 0f)}, " +
                    "match: ${builtInResult?.isMatch})"
        )

        Log.d(
            "HybridClassifier",
            "Custom: ${customResult?.sound?.displayName} = " +
                    "${"%.3f".format(customResult?.score ?: 0f)} " +
                    "(threshold: ${"%.3f".format(customResult?.threshold ?: 0f)}, " +
                    "match: ${customResult?.isMatch})"
        )

        // 3. Choose the best match
        val finalResult = chooseBestMatch(builtInResult, customResult)

        return ClassificationResult(
            sound = finalResult?.sound,
            score = finalResult?.score ?: 0f,
            threshold = finalResult?.threshold ?: 0f,
            isMatch = finalResult?.isMatch ?: false,
            confidence = calculateConfidence(finalResult),
            environmentType = environmentProfile.environmentType
        )
    }

    private fun classifyBuiltIn(
        audioInput: FloatArray,
        builtInSounds: List<SoundProfile>
    ): SoundMatch? {
        val outputScores = Array(1) { FloatArray(labels.size) }
        interpreter.run(arrayOf(audioInput), outputScores)

        val scores = outputScores[0]
        val allMerged = mergePredictions(scores)
        val allowedMerged = allMerged.filterKeys { it in builtInSounds }

        updateEma(allowedMerged)

        val filteredScores = emaScores.filterKeys { it in builtInSounds }
        if (filteredScores.isEmpty()) return null

        // Apply environment adjustments
        val environmentAdjusted = filteredScores.mapValues { (sound, score) ->
            adjustForEnvironment(sound, score)
        }

        // Find best sound with its adaptive threshold
        val bestEntry = environmentAdjusted.maxByOrNull { it.value } ?: return null
        val bestSound = bestEntry.key
        val bestScore = bestEntry.value

        // Get adaptive threshold for this sound
        val adaptiveThreshold = getAdaptiveThreshold(
            sound = bestSound,
            baseThreshold = 0.3f // Can be made configurable per sound
        )

        val isMatch = bestScore >= adaptiveThreshold

        return SoundMatch(
            sound = bestSound,
            score = bestScore,
            threshold = adaptiveThreshold,
            isMatch = isMatch
        )
    }

    private fun classifyCustom(
        audioInput: FloatArray,
        customSounds: List<SoundProfile>
    ): SoundMatch? {
        val (matchedSound, similarity) = customSoundClassifier.matchCustomSounds(
            audioInput,
            customSounds
        )

        if (matchedSound == null) return null

        // Get adaptive threshold for custom sound
        val adaptiveThreshold = getAdaptiveThresholdForCustom(matchedSound)
        val isMatch = similarity >= adaptiveThreshold

        return SoundMatch(
            sound = matchedSound,
            score = similarity,
            threshold = adaptiveThreshold,
            isMatch = isMatch
        )
    }

    private fun chooseBestMatch(
        builtIn: SoundMatch?,
        custom: SoundMatch?
    ): SoundMatch? {
        // Only consider matches that exceed their thresholds
        val validBuiltIn = builtIn?.takeIf { it.isMatch }
        val validCustom = custom?.takeIf { it.isMatch }

        return when {
            validBuiltIn == null && validCustom == null -> null
            validBuiltIn == null -> validCustom
            validCustom == null -> validBuiltIn
            else -> {
                // Both matched - choose based on confidence margin
                val builtInMargin = validBuiltIn.score - validBuiltIn.threshold
                val customMargin = validCustom.score - validCustom.threshold

                // Prefer custom sounds if margins are similar (user intent)
                if (abs(builtInMargin - customMargin) < 0.1f) {
                    validCustom
                } else if (builtInMargin > customMargin) {
                    validBuiltIn
                } else {
                    validCustom
                }
            }
        }
    }

    private fun calculateConfidence(match: SoundMatch?): String {
        if (match == null || !match.isMatch) return "none"

        val margin = match.score - match.threshold
        val variance = environmentProfile.recentNoiseVariance

        return when {
            margin > 0.15f && variance < 0.005f -> "high"
            margin > 0.08f && variance < 0.01f -> "medium"
            margin > 0.03f -> "low"
            else -> "very_low"
        }
    }

    // Data classes
    data class EnvironmentProfile(
        var avgNoiseLevel: Float = 0f,
        var dominantFreqBands: List<Int> = emptyList(),
        var adaptationFactor: Float = 1.0f,
        var recentNoiseVariance: Float = 0f,
        var environmentType: EnvironmentType = EnvironmentType.NORMAL
    )

    data class SoundMatch(
        val sound: SoundProfile,
        val score: Float,
        val threshold: Float,
        val isMatch: Boolean
    )

    data class ClassificationResult(
        val sound: SoundProfile?,
        val score: Float,
        val threshold: Float,
        val isMatch: Boolean,
        val confidence: String,
        val environmentType: EnvironmentType
    )
}