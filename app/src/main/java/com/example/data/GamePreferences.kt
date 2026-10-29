package com.example.data

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("brick_smash_prefs", Context.MODE_PRIVATE)

    var bestScore: Int
        get() = prefs.getInt("key_best_score", 0)
        set(value) = prefs.edit().putInt("key_best_score", value).apply()

    var unlockedLevel: Int
        get() = prefs.getInt("key_unlocked_level", 1)
        set(value) = prefs.edit().putInt("key_unlocked_level", value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("key_sound_enabled", true)
        set(value) = prefs.edit().putBoolean("key_sound_enabled", value).apply()

    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean("key_haptics_enabled", true)
        set(value) = prefs.edit().putBoolean("key_haptics_enabled", value).apply()

    fun getLevelStars(level: Int): Int {
        return prefs.getInt("key_level_stars_$level", 0)
    }

    fun setLevelStars(level: Int, stars: Int) {
        val current = getLevelStars(level)
        if (stars > current) {
            prefs.edit().putInt("key_level_stars_$level", stars).apply()
        }
    }

    fun getLevelHighScore(level: Int): Int {
        return prefs.getInt("key_level_score_$level", 0)
    }

    fun setLevelHighScore(level: Int, score: Int) {
        val current = getLevelHighScore(level)
        if (score > current) {
            prefs.edit().putInt("key_level_score_$level", score).apply()
        }
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}
