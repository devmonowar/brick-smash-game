package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    @Suppress("DEPRECATION")
    fun vibrate(durationMs: Long = 25, amplitude: Int = 120) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignored if device lacks vibration
        }
    }

    private fun playTone(
        startFreq: Float,
        endFreq: Float = startFreq,
        durationMs: Int,
        volume: Float = 0.5f,
        waveType: String = "sine"
    ) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)
                var currentFreq = startFreq
                val freqStep = if (numSamples > 1) (endFreq - startFreq) / numSamples else 0f
                var phase = 0.0

                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val envelope = when {
                        progress < 0.1f -> progress / 0.1f
                        progress > 0.8f -> (1f - progress) / 0.2f
                        else -> 1f
                    }

                    val sampleVal = when (waveType) {
                        "square" -> if (sin(phase) >= 0) 1.0 else -1.0
                        "triangle" -> (2.0 / PI) * Math.asin(sin(phase))
                        "noise" -> (Random.nextFloat() * 2f - 1f).toDouble()
                        else -> sin(phase)
                    }

                    buffer[i] = (sampleVal * envelope * volume * Short.MAX_VALUE).toInt().toShort()

                    phase += 2.0 * PI * currentFreq / sampleRate
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                    currentFreq += freqStep
                }

                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val audioFormat = AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()

                val track = AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                // Release track after playback completes
                launch {
                    kotlinx.coroutines.delay(durationMs + 50L)
                    try {
                        track.stop()
                        track.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Audio unavailable or error
            }
        }
    }

    fun playLaunch() {
        playTone(startFreq = 320f, endFreq = 780f, durationMs = 120, volume = 0.45f, waveType = "triangle")
        vibrate(20, 100)
    }

    fun playBouncePaddle() {
        playTone(startFreq = 260f, endFreq = 340f, durationMs = 70, volume = 0.4f, waveType = "sine")
        vibrate(15, 80)
    }

    fun playBounceWall() {
        playTone(startFreq = 480f, endFreq = 420f, durationMs = 50, volume = 0.35f, waveType = "sine")
    }

    fun playHitBrick() {
        playTone(startFreq = 620f, endFreq = 740f, durationMs = 60, volume = 0.45f, waveType = "square")
        vibrate(25, 120)
    }

    fun playDestroyBrick() {
        playTone(startFreq = 880f, endFreq = 1100f, durationMs = 90, volume = 0.5f, waveType = "triangle")
        vibrate(35, 160)
    }

    fun playExplosion() {
        playTone(startFreq = 180f, endFreq = 60f, durationMs = 240, volume = 0.6f, waveType = "noise")
        vibrate(60, 240)
    }

    fun playSteelHit() {
        playTone(startFreq = 220f, endFreq = 200f, durationMs = 60, volume = 0.4f, waveType = "square")
        vibrate(20, 100)
    }

    fun playPowerUpCollected() {
        scope.launch {
            val notes = listOf(523f, 659f, 784f, 1046f) // C5, E5, G5, C6
            for (freq in notes) {
                playTone(startFreq = freq, endFreq = freq, durationMs = 70, volume = 0.5f, waveType = "triangle")
                kotlinx.coroutines.delay(45L)
            }
        }
        vibrate(40, 180)
    }

    fun playLifeLost() {
        playTone(startFreq = 440f, endFreq = 120f, durationMs = 280, volume = 0.55f, waveType = "sawtooth")
        vibrate(80, 255)
    }

    fun playLevelComplete() {
        scope.launch {
            val notes = listOf(440f, 554f, 659f, 880f, 1108f)
            for (freq in notes) {
                playTone(startFreq = freq, endFreq = freq, durationMs = 120, volume = 0.55f, waveType = "triangle")
                kotlinx.coroutines.delay(75L)
            }
        }
        vibrate(100, 220)
    }

    fun playGameOver() {
        scope.launch {
            val notes = listOf(440f, 392f, 349f, 261f)
            for (freq in notes) {
                playTone(startFreq = freq, endFreq = freq, durationMs = 150, volume = 0.55f, waveType = "sine")
                kotlinx.coroutines.delay(110L)
            }
        }
        vibrate(120, 255)
    }
}
