package com.example.game

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

interface SoundPlayer {
    fun playButtonClick()
    fun playCorrect()
    fun playWrong()
    fun playCombo(comboCount: Int)
    fun playJoker()
    fun playGameOver()
    fun playSuccessFanfare()
    fun release()
}

class SoundEffectManager(private val context: Context) : SoundPlayer {

    private var toneGen: ToneGenerator? = null
    var isSoundEnabled: Boolean = true
    var isVibrationEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            null
        }
    }

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            Log.w("SoundEffectManager", "ToneGenerator could not be created", e)
        }
    }

    override fun playButtonClick() {
        vibrate(20)
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (_: Exception) {}
    }

    override fun playCorrect() {
        vibrate(60)
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 160)
        } catch (_: Exception) {}
    }

    override fun playWrong() {
        vibratePattern(longArrayOf(0, 50, 40, 70))
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        } catch (_: Exception) {}
    }

    override fun playCombo(comboCount: Int) {
        vibrate(80)
        if (!isSoundEnabled) return
        try {
            val tone = when {
                comboCount >= 10 -> ToneGenerator.TONE_SUP_PIP
                comboCount >= 5 -> ToneGenerator.TONE_PROP_ACK
                else -> ToneGenerator.TONE_PROP_BEEP2
            }
            toneGen?.startTone(tone, 150)
        } catch (_: Exception) {}
    }

    override fun playJoker() {
        vibrate(35)
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
        } catch (_: Exception) {}
    }

    override fun playGameOver() {
        vibratePattern(longArrayOf(0, 100, 80, 150))
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 350)
        } catch (_: Exception) {}
    }

    override fun playSuccessFanfare() {
        vibratePattern(longArrayOf(0, 40, 30, 40, 30, 80))
        if (!isSoundEnabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 250)
        } catch (_: Exception) {}
    }

    private fun vibrate(durationMs: Long) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    override fun release() {
        try {
            toneGen?.release()
            toneGen = null
        } catch (_: Exception) {}
    }
}
