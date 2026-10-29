package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.BrickSmashEngine
import com.example.model.*
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: BrickSmashEngine,
    playStatus: PlayStatus,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Shared transform holder: [0] = scale, [1] = offsetX, [2] = offsetY
    val transform = remember { FloatArray(3) { 1f } }

    // Pre-allocated reusable Paint objects to avoid per-frame GC allocations
    val durabilityPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    val letterPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    val popupPaint = remember {
        Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    val launchPaint = remember {
        Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
    }

    // Pre-allocated brushes to avoid per-frame gradient allocations
    val bgBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF060919),
                Color(0xFF0A0F2C),
                Color(0xFF080D24)
            )
        )
    }

    val paddleBrush = remember {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF0091EA))
        )
    }

    val steelGradientColors = remember { listOf(Color(0xFF90A4AE), Color(0xFF37474F)) }
    val bonusGradientColors = remember { listOf(Color(0xFFFF007F), Color(0xFF7C4DFF)) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var prevX = down.position.x
                    var totalMovement = 0f

                    // Snappy, instant receiver bar response on initial tap/touch
                    val initialScale = transform[0]
                    val initialOffsetX = transform[1]
                    if (initialScale > 0.001f) {
                        val logicalTargetX = (down.position.x - initialOffsetX) / initialScale
                        engine.setPaddleX(logicalTargetX)
                    }

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break

                        if (!change.pressed) {
                            // Finger lifted without significant drag -> trigger tap / launch
                            if (totalMovement < 15f) {
                                onTap()
                            }
                            break
                        }

                        val currentX = change.position.x
                        val deltaPx = currentX - prevX
                        prevX = currentX
                        totalMovement += kotlin.math.abs(deltaPx)

                        // 1:1 Instant direct paddle tracking
                        val currentScale = transform[0]
                        val currentOffsetX = transform[1]
                        if (currentScale > 0.001f) {
                            val logicalTargetX = (currentX - currentOffsetX) / currentScale
                            engine.setPaddleX(logicalTargetX)
                        }
                        change.consume()
                    }
                }
            }
    ) {
        val _tick = engine.frameTick.longValue

        // Uniform scale to fit 1000x1600 logical area inside available canvas
        val logicalW = BrickSmashEngine.ARENA_WIDTH
        val logicalH = BrickSmashEngine.ARENA_HEIGHT

        val scaleX = size.width / logicalW
        val scaleY = size.height / logicalH
        val currentScale = minOf(scaleX, scaleY)

        val renderW = logicalW * currentScale
        val renderH = logicalH * currentScale

        val currentOffsetX = (size.width - renderW) / 2f
        val currentOffsetY = (size.height - renderH) / 2f

        // Update transform for touch gestures
        transform[0] = currentScale
        transform[1] = currentOffsetX
        transform[2] = currentOffsetY

        fun toScreen(lx: Float, ly: Float): Offset {
            return Offset(currentOffsetX + lx * currentScale, currentOffsetY + ly * currentScale)
        }

        // 1. Draw Deep Cyber Space Background
        drawRect(
            brush = bgBrush,
            size = size
        )

        // Arena Boundaries Glow Lines
        val topWall = toScreen(0f, BrickSmashEngine.TOP_WALL_Y)
        val leftWall = toScreen(20f, BrickSmashEngine.TOP_WALL_Y)
        val rightWall = toScreen(logicalW - 20f, BrickSmashEngine.TOP_WALL_Y)
        val bottomArena = toScreen(0f, logicalH)

        drawLine(
            color = Color(0x3300E5FF),
            start = Offset(leftWall.x, topWall.y),
            end = Offset(leftWall.x, bottomArena.y),
            strokeWidth = 3f * currentScale
        )
        drawLine(
            color = Color(0x3300E5FF),
            start = Offset(rightWall.x, topWall.y),
            end = Offset(rightWall.x, bottomArena.y),
            strokeWidth = 3f * currentScale
        )
        drawLine(
            color = Color(0x4400E5FF),
            start = Offset(leftWall.x, topWall.y),
            end = Offset(rightWall.x, topWall.y),
            strokeWidth = 3f * currentScale
        )

        val pulseTime = engine.pulseTime

        // 2. Draw Bricks (Fixed positions: never shift or rearrange)
        val totalBricks = engine.bricks.size
        for (i in 0 until totalBricks) {
            val brick = engine.bricks[i]
            if (brick.isDestroyed) continue

            val bTopLeft = toScreen(brick.x, brick.y)
            val bSize = Size(brick.width * currentScale, brick.height * currentScale)
            val corner = CornerRadius(6f * currentScale, 6f * currentScale)

            when (brick.type) {
                BrickType.STEEL -> {
                    // Indestructible Metallic Steel Brick
                    drawRoundRect(
                        color = Color(0xFF455A64),
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = steelGradientColors,
                            start = bTopLeft,
                            end = Offset(bTopLeft.x + bSize.width, bTopLeft.y + bSize.height)
                        ),
                        topLeft = Offset(bTopLeft.x + 2f * currentScale, bTopLeft.y + 2f * currentScale),
                        size = Size(bSize.width - 4f * currentScale, bSize.height - 4f * currentScale),
                        cornerRadius = corner
                    )
                    // Rivets
                    drawCircle(
                        color = Color(0xFFCFD8DC),
                        radius = 2.5f * currentScale,
                        center = Offset(bTopLeft.x + 8f * currentScale, bTopLeft.y + bSize.height / 2f)
                    )
                    drawCircle(
                        color = Color(0xFFCFD8DC),
                        radius = 2.5f * currentScale,
                        center = Offset(bTopLeft.x + bSize.width - 8f * currentScale, bTopLeft.y + bSize.height / 2f)
                    )
                }
                BrickType.EXPLOSIVE -> {
                    // Pulsating warning explosive brick
                    val pulse = (sin(pulseTime.toDouble() * 2) * 0.2 + 0.8).toFloat()
                    drawRoundRect(
                        color = Color(0xFFFF1744).copy(alpha = pulse),
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    drawCircle(
                        color = Color(0xFFFFEA00),
                        radius = 5.5f * currentScale,
                        center = Offset(bTopLeft.x + bSize.width / 2f, bTopLeft.y + bSize.height / 2f)
                    )
                }
                BrickType.BONUS -> {
                    // Bonus Brick
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = bonusGradientColors,
                            start = bTopLeft,
                            end = Offset(bTopLeft.x + bSize.width, bTopLeft.y + bSize.height)
                        ),
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5f * currentScale,
                        center = Offset(bTopLeft.x + bSize.width / 2f, bTopLeft.y + bSize.height / 2f)
                    )
                }
                BrickType.HARD -> {
                    // 3-HP Multi-Hit Brick with Damage Stages:
                    // 3 HP: Deep ruby crimson (Pristine)
                    // 2 HP: Amber-Orange + Light crack (1st hit taken)
                    // 1 HP: Bright distressed warning + heavy crack (2nd hit taken)
                    val (brickColor, isLightDamaged, isHeavyDamaged) = when (brick.hitsRemaining) {
                        3 -> Triple(Color(0xFFFF1744), false, false)
                        2 -> Triple(Color(0xFFFF6D00), true, false)
                        else -> Triple(Color(0xFFFFAB00), true, true)
                    }

                    drawRoundRect(
                        color = brickColor,
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    // Top highlight bevel
                    drawRoundRect(
                        color = Color(0x66FFFFFF),
                        topLeft = bTopLeft,
                        size = Size(bSize.width, 3f * currentScale),
                        cornerRadius = corner
                    )

                    // Visual crack feedback
                    if (isLightDamaged) {
                        // Light hairline fracture line
                        drawLine(
                            color = Color(0xCCFFFFFF),
                            start = Offset(bTopLeft.x + bSize.width * 0.25f, bTopLeft.y + 4f * currentScale),
                            end = Offset(bTopLeft.x + bSize.width * 0.42f, bTopLeft.y + bSize.height * 0.65f),
                            strokeWidth = 1.8f * currentScale
                        )
                    }
                    if (isHeavyDamaged) {
                        // Heavy multi-branch crack across the brick
                        drawLine(
                            color = Color.White,
                            start = Offset(bTopLeft.x + bSize.width * 0.42f, bTopLeft.y + bSize.height * 0.65f),
                            end = Offset(bTopLeft.x + bSize.width * 0.72f, bTopLeft.y + bSize.height - 4f * currentScale),
                            strokeWidth = 2f * currentScale
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(bTopLeft.x + bSize.width * 0.42f, bTopLeft.y + bSize.height * 0.65f),
                            end = Offset(bTopLeft.x + bSize.width * 0.2f, bTopLeft.y + bSize.height - 4f * currentScale),
                            strokeWidth = 1.8f * currentScale
                        )
                    }
                }
                BrickType.STRONG -> {
                    // 2-HP Strong Brick with Damage Stages:
                    // 2 HP: Vibrant Amber (Pristine)
                    // 1 HP: Darker Amber + Visible crack (1st hit taken)
                    val (brickColor, isDamaged) = when (brick.hitsRemaining) {
                        2 -> Pair(Color(0xFFFF9100), false)
                        else -> Pair(Color(0xFFFF6D00), true)
                    }

                    drawRoundRect(
                        color = brickColor,
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    // Top bevel highlight
                    drawRoundRect(
                        color = Color(0x55FFFFFF),
                        topLeft = bTopLeft,
                        size = Size(bSize.width, 3f * currentScale),
                        cornerRadius = corner
                    )

                    // Crack line when 1 HP remaining
                    if (isDamaged) {
                        drawLine(
                            color = Color(0xDDFFFFFF),
                            start = Offset(bTopLeft.x + bSize.width * 0.3f, bTopLeft.y + 4f * currentScale),
                            end = Offset(bTopLeft.x + bSize.width * 0.65f, bTopLeft.y + bSize.height - 4f * currentScale),
                            strokeWidth = 2f * currentScale
                        )
                    }
                }
                BrickType.NORMAL -> {
                    // 1-HP Standard glowing arcade brick (breaks on 1 hit)
                    drawRoundRect(
                        color = brick.baseColor,
                        topLeft = bTopLeft,
                        size = bSize,
                        cornerRadius = corner
                    )
                    // Top highlight for 3D depth
                    drawRoundRect(
                        color = Color(0x55FFFFFF),
                        topLeft = bTopLeft,
                        size = Size(bSize.width, 4f * currentScale),
                        cornerRadius = corner
                    )
                    // Subtle bottom shading
                    drawRoundRect(
                        color = Color(0x33000000),
                        topLeft = Offset(bTopLeft.x + 1f, bTopLeft.y + bSize.height - 3f * currentScale),
                        size = Size(bSize.width, 3f * currentScale),
                        cornerRadius = corner
                    )
                }
            }
        }

        // 3. Draw Falling Power-Ups (Capsule shapes)
        for (powerUp in engine.powerUps) {
            val pPos = toScreen(powerUp.x, powerUp.y)
            val pRadius = powerUp.radius * currentScale

            // Glowing Capsule
            drawCircle(
                color = powerUp.type.color.copy(alpha = 0.4f),
                radius = pRadius * 1.5f,
                center = pPos
            )
            drawCircle(
                color = powerUp.type.color,
                radius = pRadius,
                center = pPos
            )
            drawCircle(
                color = Color.White,
                radius = pRadius * 0.4f,
                center = Offset(pPos.x - pRadius * 0.3f, pPos.y - pRadius * 0.3f)
            )
        }

        // 4. Draw Paddle
        val paddle = engine.paddle
        val paddleTopLeft = toScreen(paddle.x - paddle.currentWidth / 2f, paddle.y - paddle.height / 2f)
        val paddleSize = Size(paddle.currentWidth * currentScale, paddle.height * currentScale)
        val paddleCorner = CornerRadius(12f * currentScale, 12f * currentScale)

        // Outer Neon Glow
        drawRoundRect(
            color = Color(0x4000E5FF),
            topLeft = Offset(paddleTopLeft.x - 4f * currentScale, paddleTopLeft.y - 4f * currentScale),
            size = Size(paddleSize.width + 8f * currentScale, paddleSize.height + 8f * currentScale),
            cornerRadius = CornerRadius(16f * currentScale, 16f * currentScale)
        )
        // Paddle Body
        drawRoundRect(
            brush = paddleBrush,
            topLeft = paddleTopLeft,
            size = paddleSize,
            cornerRadius = paddleCorner
        )
        // White Core Highlight
        drawRoundRect(
            color = Color(0xDDFFFFFF),
            topLeft = Offset(paddleTopLeft.x + 12f * currentScale, paddleTopLeft.y + 4f * currentScale),
            size = Size(paddleSize.width - 24f * currentScale, 4f * currentScale),
            cornerRadius = CornerRadius(2f * currentScale, 2f * currentScale)
        )

        // 5. Draw Balls (Continuous & guaranteed on EVERY frame)
        for (ball in engine.balls) {
            val ballCenter = toScreen(ball.x, ball.y)
            val ballRadius = ball.radius * currentScale

            // Ball outer glow
            drawCircle(
                color = Color(0x6600E5FF),
                radius = ballRadius * 1.5f,
                center = ballCenter
            )
            // Ball solid vibrant core
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = ballRadius,
                center = ballCenter
            )
            // Ball bright specular shine
            drawCircle(
                color = Color.White,
                radius = ballRadius * 0.45f,
                center = Offset(ballCenter.x - ballRadius * 0.3f, ballCenter.y - ballRadius * 0.3f)
            )
        }

        // 6. Draw Particles
        for (particle in engine.particles) {
            val pPos = toScreen(particle.x, particle.y)
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.radius * currentScale,
                center = pPos
            )
        }

        // 7. BATCH TEXT DRAW PASS (Single JNI bridge call per frame for zero GC allocations!)
        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas

            // A. Multi-Hit Durability Numbers
            durabilityPaint.textSize = 17f * currentScale
            for (i in 0 until totalBricks) {
                val b = engine.bricks[i]
                if (b.isDestroyed) continue
                if (b.type == BrickType.HARD || b.type == BrickType.STRONG) {
                    val cx = currentOffsetX + (b.x + b.width / 2f) * currentScale
                    val cy = currentOffsetY + (b.y + b.height / 2f) * currentScale + (6f * currentScale)
                    native.drawText("${b.hitsRemaining}", cx, cy, durabilityPaint)
                }
            }

            // B. Power-Up Letter Labels
            letterPaint.textSize = 14f * currentScale
            for (powerUp in engine.powerUps) {
                val px = currentOffsetX + powerUp.x * currentScale
                val py = currentOffsetY + powerUp.y * currentScale + (5f * currentScale)
                val letter = when (powerUp.type) {
                    PowerUpType.WIDE_PADDLE -> "W"
                    PowerUpType.SLOW_BALL -> "S"
                    PowerUpType.MULTI_BALL -> "M"
                    PowerUpType.EXTRA_LIFE -> "+"
                }
                native.drawText(letter, px, py, letterPaint)
            }

            // C. Floating Score Popups
            for (popup in engine.scorePopups) {
                val px = currentOffsetX + popup.x * currentScale
                val py = currentOffsetY + popup.y * currentScale
                popupPaint.color = android.graphics.Color.argb(
                    (popup.alpha * 255).toInt().coerceIn(0, 255),
                    (popup.color.red * 255).toInt(),
                    (popup.color.green * 255).toInt(),
                    (popup.color.blue * 255).toInt()
                )
                popupPaint.textSize = 22f * currentScale
                native.drawText(popup.text, px, py, popupPaint)
            }

            // D. "TAP TO LAUNCH" Pulsing Indicator when ball is attached
            if (playStatus == PlayStatus.READY_TO_LAUNCH) {
                val pulse = (sin(pulseTime.toDouble()) * 0.35 + 0.65).toFloat()
                val textY = currentOffsetY + (paddle.y - 65f) * currentScale
                launchPaint.color = android.graphics.Color.argb(
                    (pulse * 255).toInt().coerceIn(0, 255),
                    0,
                    229,
                    255
                )
                launchPaint.textSize = 28f * currentScale
                native.drawText("TAP TO LAUNCH", size.width / 2f, textY, launchPaint)
            }
        }
    }
}
