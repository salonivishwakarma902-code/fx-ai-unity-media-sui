package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class DailyRingtoneItem(
    val id: String,
    val title: String,
    val durationLabel: String,
    val startColor: Color,
    val endColor: Color,
    val frequenciesHz: List<Float>,
    val noteDurationMs: Int = 210
)

val WALLPERS_DAILY_RINGTONES = listOf(
    DailyRingtoneItem(
        id = "crystal_chime",
        title = "Crystal Chime",
        durationLabel = "0:03",
        startColor = Color(0xFF00D9FF),
        endColor = Color(0xFF3B82F6),
        frequenciesHz = listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f, 1046.50f)
    ),
    DailyRingtoneItem(
        id = "aurora_pulse",
        title = "Aurora Pulse",
        durationLabel = "0:03",
        startColor = Color(0xFF8B5CF6),
        endColor = Color(0xFFEC4899),
        frequenciesHz = listOf(440.00f, 554.37f, 659.25f, 880.00f, 1108.73f, 880.00f)
    ),
    DailyRingtoneItem(
        id = "liquid_bell",
        title = "Liquid Bell",
        durationLabel = "0:03",
        startColor = Color(0xFF10B981),
        endColor = Color(0xFF0EA5E9),
        frequenciesHz = listOf(587.33f, 739.99f, 880.00f, 1174.66f, 1479.98f)
    ),
    DailyRingtoneItem(
        id = "cosmic_echo",
        title = "Cosmic Echo",
        durationLabel = "0:04",
        startColor = Color(0xFF6366F1),
        endColor = Color(0xFF06B6D4),
        frequenciesHz = listOf(392.00f, 493.88f, 587.33f, 783.99f, 987.77f, 783.99f)
    ),
    DailyRingtoneItem(
        id = "velvet_horizon",
        title = "Velvet Horizon",
        durationLabel = "0:03",
        startColor = Color(0xFFF59E0B),
        endColor = Color(0xFFEF4444),
        frequenciesHz = listOf(493.88f, 622.25f, 739.99f, 987.77f, 1244.51f)
    )
)

object WallpersRingtonePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playbackJob: Job? = null
    private var currentTrack: AudioTrack? = null

    private val _playingRingtoneId = MutableStateFlow<String?>(null)
    val playingRingtoneId: StateFlow<String?> = _playingRingtoneId.asStateFlow()

    fun toggleRingtone(
        item: DailyRingtoneItem,
        onStarted: (DailyRingtoneItem) -> Unit = {}
    ) {
        if (_playingRingtoneId.value == item.id) {
            stop()
            return
        }
        stop()
        _playingRingtoneId.value = item.id
        onStarted(item)

        playbackJob = scope.launch {
            val sampleRate = 44100
            val samplesPerNote = (sampleRate * (item.noteDurationMs / 1000.0)).toInt().coerceAtLeast(1)
            val totalSamples = samplesPerNote * item.frequenciesHz.size
            val pcm = ShortArray(totalSamples)

            item.frequenciesHz.forEachIndexed { noteIdx, freq ->
                val startOffset = noteIdx * samplesPerNote
                for (i in 0 until samplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / samplesPerNote
                    val attack = (progress / 0.08).coerceIn(0.0, 1.0)
                    val decay = exp(-3.6 * progress)
                    val fundamental = sin(2.0 * PI * freq * t)
                    val harmonic = 0.35 * sin(2.0 * PI * freq * 2.0 * t)
                    val sample = ((fundamental + harmonic) * attack * decay * 0.42 * Short.MAX_VALUE)
                        .toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    pcm[startOffset + i] = sample.toShort()
                }
            }

            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                currentTrack = track
                track.play()

                var offset = 0
                val chunkSize = 2048
                while (isActive && offset < pcm.size) {
                    val count = minOf(chunkSize, pcm.size - offset)
                    val written = track.write(pcm, offset, count)
                    if (written <= 0) break
                    offset += written
                }
                track.stop()
                track.release()
            } catch (_: Throwable) {
                // Gracefully recover on headless or audio-restricted environments
            } finally {
                currentTrack = null
                if (_playingRingtoneId.value == item.id) {
                    _playingRingtoneId.value = null
                }
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            currentTrack?.pause()
            currentTrack?.flush()
            currentTrack?.stop()
            currentTrack?.release()
        } catch (_: Throwable) {
        } finally {
            currentTrack = null
            _playingRingtoneId.value = null
        }
    }
}
