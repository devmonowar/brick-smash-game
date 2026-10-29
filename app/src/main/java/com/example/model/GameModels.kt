package com.example.model

import androidx.compose.ui.graphics.Color

enum class BrickType {
    NORMAL,
    STRONG,
    HARD,
    STEEL,
    EXPLOSIVE,
    BONUS
}

enum class PowerUpType(val label: String, val color: Color, val durationMs: Long) {
    WIDE_PADDLE("Wide Paddle", Color(0xFF00E5FF), 12000L),
    SLOW_BALL("Slow Ball", Color(0xFF76FF03), 10000L),
    MULTI_BALL("Multi Ball", Color(0xFFFF9100), 0L), // Instant effect: spawns extra balls
    EXTRA_LIFE("+1 Life", Color(0xFFFF1744), 0L)   // Instant effect: recovers life
}

data class Ball(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float = 14f,
    var isAttached: Boolean = true,
    var offsetXOnPaddle: Float = 0f,
    val trail: MutableList<Pair<Float, Float>> = mutableListOf()
)

data class Paddle(
    var x: Float,
    var y: Float,
    var baseWidth: Float = 210f,
    var currentWidth: Float = 210f,
    var height: Float = 26f
)

data class Brick(
    val id: Int,
    val row: Int,
    val col: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: BrickType,
    var hitsRemaining: Int,
    val maxHits: Int,
    var isDestroyed: Boolean = false,
    var hitCooldown: Float = 0f,
    val baseColor: Color,
    val scoreValue: Int
)

data class PowerUp(
    val id: Long,
    var x: Float,
    var y: Float,
    val vy: Float = 220f,
    val type: PowerUpType,
    var isCollected: Boolean = false,
    var isExpired: Boolean = false,
    val radius: Float = 18f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var alpha: Float = 1f,
    val radius: Float,
    val maxLife: Float = 0.6f,
    var currentLife: Float = 0f
)

data class ScorePopup(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var alpha: Float = 1f,
    val maxLife: Float = 0.8f,
    var currentLife: Float = 0f
)

enum class ScreenState {
    MAIN_MENU,
    LEVEL_SELECT,
    HOW_TO_PLAY,
    GAMEPLAY
}

enum class PlayStatus {
    READY_TO_LAUNCH,
    BALL_IN_PLAY,
    PAUSED,
    LEVEL_COMPLETED,
    GAME_OVER,
    ALL_LEVELS_CLEARED
}
