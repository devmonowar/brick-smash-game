package com.example.engine

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import kotlin.random.Random

class BrickSmashEngine(
    private val soundManager: SoundManager,
    private val preferences: GamePreferences
) {
    companion object {
        const val ARENA_WIDTH = 1000f
        const val ARENA_HEIGHT = 1600f
        const val TOP_WALL_Y = 140f
        const val PADDLE_DEFAULT_Y = 1450f
        // Wider paddle for beginner-friendly, fun and forgiving gameplay
        const val PADDLE_BASE_WIDTH = 210f
        const val PADDLE_WIDE_WIDTH = 300f
        const val PADDLE_HEIGHT = 26f
        const val INITIAL_LIVES = 5
    }

    // Compose state observable for frame rendering synchronization
    val frameTick = mutableLongStateOf(0L)

    // Pulse animation timer for visual effects
    var pulseTime: Float = 0f
        private set

    // Steering input from buttons (-1f = Left, +1f = Right, 0f = None)
    var steeringDirection: Float = 0f

    // Steering input from keyboard (-1f = Left, +1f = Right, 0f = None)
    var keyboardSteeringDirection: Float = 0f

    // Counter for Easy Life System (1 extra life every 10 bricks broken)
    var bricksDestroyedCount: Int = 0
        private set

    // Game state observables
    private val _playStatus = MutableStateFlow(PlayStatus.READY_TO_LAUNCH)
    val playStatus: StateFlow<PlayStatus> = _playStatus.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(preferences.bestScore)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _lives = MutableStateFlow(INITIAL_LIVES)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _activePowerUps = MutableStateFlow<Map<PowerUpType, Float>>(emptyMap())
    val activePowerUps: StateFlow<Map<PowerUpType, Float>> = _activePowerUps.asStateFlow()

    // Game world elements
    val paddle = Paddle(
        x = ARENA_WIDTH / 2f,
        y = PADDLE_DEFAULT_Y,
        baseWidth = PADDLE_BASE_WIDTH,
        currentWidth = PADDLE_BASE_WIDTH,
        height = PADDLE_HEIGHT
    )

    val balls = mutableListOf<Ball>()
    val bricks = mutableListOf<Brick>()
    val powerUps = mutableListOf<PowerUp>()
    val particles = mutableListOf<Particle>()
    val scorePopups = mutableListOf<ScorePopup>()

    private var baseSpeed: Float = 480f
    private var powerUpIdGen = 0L
    private var popupIdGen = 0L
    private var minBrickTop: Float = 0f
    private var maxBrickBottom: Float = 800f

    init {
        loadLevel(1, resetScore = true)
    }

    fun loadLevel(levelNum: Int, resetScore: Boolean = false) {
        val clampedLevel = levelNum.coerceIn(1, LevelData.levels.size)
        _currentLevel.value = clampedLevel
        if (resetScore) {
            _score.value = 0
            _lives.value = INITIAL_LIVES
            bricksDestroyedCount = 0
        }
        _combo.value = 0
        _activePowerUps.value = emptyMap()
        steeringDirection = 0f
        keyboardSteeringDirection = 0f

        val def = LevelData.levels[clampedLevel - 1]
        baseSpeed = def.baseBallSpeed

        // Setup bricks
        bricks.clear()
        bricks.addAll(LevelData.createBricksForLevel(clampedLevel - 1, ARENA_WIDTH))
        minBrickTop = bricks.minOfOrNull { it.y } ?: 0f
        maxBrickBottom = bricks.maxOfOrNull { it.y + it.height } ?: 800f

        // Reset paddle
        paddle.x = ARENA_WIDTH / 2f
        paddle.currentWidth = PADDLE_BASE_WIDTH

        // Clear active items
        powerUps.clear()
        particles.clear()
        scorePopups.clear()

        // Setup initial ball attached to paddle
        balls.clear()
        val initialBall = Ball(
            x = paddle.x,
            y = paddle.y - paddle.height / 2f - 14f,
            vx = 0f,
            vy = 0f,
            radius = 14f,
            isAttached = true
        )
        balls.add(initialBall)

        _playStatus.value = PlayStatus.READY_TO_LAUNCH
        frameTick.longValue++
    }

    fun launchBall() {
        if (_playStatus.value != PlayStatus.READY_TO_LAUNCH) return

        val speed = getEffectiveSpeed()
        // Launch with random angle between -75 and -55 degrees (up-left or up-right)
        val angleDeg = if (Random.nextBoolean()) Random.nextFloat() * 20f - 75f else Random.nextFloat() * 20f - 105f
        val angleRad = Math.toRadians(angleDeg.toDouble())

        for (ball in balls) {
            if (ball.isAttached) {
                ball.isAttached = false
                ball.vx = (speed * cos(angleRad)).toFloat()
                ball.vy = (speed * sin(angleRad)).toFloat()
            }
        }

        soundManager.playLaunch()
        _playStatus.value = PlayStatus.BALL_IN_PLAY
        frameTick.longValue++
    }

    fun setPaddleX(targetX: Float) {
        val halfW = paddle.currentWidth / 2f
        val clampedX = targetX.coerceIn(halfW + 20f, ARENA_WIDTH - halfW - 20f)
        paddle.x = clampedX

        // Move attached balls along with paddle
        for (ball in balls) {
            if (ball.isAttached) {
                ball.x = paddle.x + ball.offsetXOnPaddle
                ball.y = paddle.y - paddle.height / 2f - ball.radius
            }
        }
    }

    fun movePaddleBy(deltaX: Float) {
        setPaddleX(paddle.x + deltaX)
    }

    fun pauseGame() {
        if (_playStatus.value == PlayStatus.BALL_IN_PLAY || _playStatus.value == PlayStatus.READY_TO_LAUNCH) {
            _playStatus.value = PlayStatus.PAUSED
            frameTick.longValue++
        }
    }

    fun resumeGame() {
        if (_playStatus.value == PlayStatus.PAUSED) {
            val hasActiveBall = balls.any { !it.isAttached }
            _playStatus.value = if (hasActiveBall) PlayStatus.BALL_IN_PLAY else PlayStatus.READY_TO_LAUNCH
            frameTick.longValue++
        }
    }

    fun restartCurrentLevel() {
        loadLevel(_currentLevel.value, resetScore = false)
    }

    fun nextLevel() {
        val next = _currentLevel.value + 1
        if (next <= LevelData.levels.size) {
            loadLevel(next, resetScore = false)
        } else {
            _playStatus.value = PlayStatus.ALL_LEVELS_CLEARED
        }
        frameTick.longValue++
    }

    fun getEffectiveSpeed(): Float {
        val slowFactor = if (_activePowerUps.value.containsKey(PowerUpType.SLOW_BALL)) 0.7f else 1.0f
        return baseSpeed * slowFactor
    }

    fun update(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0.001f, 0.05f)
        pulseTime = (pulseTime + dt * 4f) % (2f * Math.PI.toFloat())

        // Quick & responsive receiver bar steering (1800f/s)
        val combinedSteering = (steeringDirection + keyboardSteeringDirection).coerceIn(-1f, 1f)
        if (combinedSteering != 0f) {
            movePaddleBy(combinedSteering * 1800f * dt)
        }

        // Cool down brick hit timers so each collision registers only once
        val totalBricksCount = bricks.size
        for (i in 0 until totalBricksCount) {
            val b = bricks[i]
            if (b.hitCooldown > 0f) {
                b.hitCooldown -= dt
            }
        }

        // Always update visual particles and score popups
        updateVisualEffects(dt)

        if (_playStatus.value != PlayStatus.BALL_IN_PLAY) {
            // Keep attached ball aligned with paddle position
            for (ball in balls) {
                if (ball.isAttached) {
                    ball.x = paddle.x + ball.offsetXOnPaddle
                    ball.y = paddle.y - paddle.height / 2f - ball.radius
                }
            }
            frameTick.longValue++
            return
        }

        // 1. Update Power-Up timers efficiently
        updatePowerUpTimers(dt)

        // 2. Smoothly animate paddle width
        val targetWidth = if (_activePowerUps.value.containsKey(PowerUpType.WIDE_PADDLE)) PADDLE_WIDE_WIDTH else PADDLE_BASE_WIDTH
        paddle.currentWidth += (targetWidth - paddle.currentWidth) * min(1f, dt * 14f)

        // 3. Sub-stepping for ultra-smooth & continuous collision detection (zero tunneling)
        val currentSpeed = getEffectiveSpeed()
        val maxStepDist = 3.0f // Step distance kept under 3.0px to guarantee zero tunneling
        val totalDist = currentSpeed * dt
        val steps = ceil(totalDist / maxStepDist).toInt().coerceIn(2, 8)
        val subDt = dt / steps

        for (s in 0 until steps) {
            updateBallsSubStep(subDt, currentSpeed)
        }

        // Handle ball loss if all balls exited arena
        handleBallLoss()

        // 4. Update Falling Power-Ups
        updatePowerUps(dt)

        // 5. Check Level Complete
        checkLevelCompletion()

        // 6. Signal DrawScope to redraw this frame
        frameTick.longValue++
    }

    private fun updateBallsSubStep(subDt: Float, currentSpeed: Float) {
        val ballsIterator = balls.iterator()
        while (ballsIterator.hasNext()) {
            val ball = ballsIterator.next()
            if (ball.isAttached) {
                ball.x = paddle.x + ball.offsetXOnPaddle
                ball.y = paddle.y - paddle.height / 2f - ball.radius
                continue
            }

            // Normalise velocity vector to match effective speed
            val mag = hypot(ball.vx, ball.vy)
            if (mag > 0.001f) {
                ball.vx = (ball.vx / mag) * currentSpeed
                ball.vy = (ball.vy / mag) * currentSpeed
            }

            // Anti-stuck prevention: avoid purely horizontal or purely vertical ball loops
            if (abs(ball.vy) < currentSpeed * 0.2f) {
                ball.vy = if (ball.vy >= 0) currentSpeed * 0.25f else -currentSpeed * 0.25f
            }
            if (abs(ball.vx) < currentSpeed * 0.15f) {
                ball.vx = if (ball.vx >= 0) currentSpeed * 0.2f else -currentSpeed * 0.2f
            }

            // Step movement
            ball.x += ball.vx * subDt
            ball.y += ball.vy * subDt

            // Wall collisions with clean boundary clamp
            if (ball.x - ball.radius <= 20f) {
                ball.x = 20f + ball.radius
                ball.vx = abs(ball.vx)
                soundManager.playBounceWall()
            } else if (ball.x + ball.radius >= ARENA_WIDTH - 20f) {
                ball.x = ARENA_WIDTH - 20f - ball.radius
                ball.vx = -abs(ball.vx)
                soundManager.playBounceWall()
            }

            if (ball.y - ball.radius <= TOP_WALL_Y) {
                ball.y = TOP_WALL_Y + ball.radius
                ball.vy = abs(ball.vy)
                soundManager.playBounceWall()
            }

            // Paddle collision
            val paddleTop = paddle.y - paddle.height / 2f
            val paddleBottom = paddle.y + paddle.height / 2f
            val paddleLeft = paddle.x - paddle.currentWidth / 2f
            val paddleRight = paddle.x + paddle.currentWidth / 2f

            if (ball.vy > 0 &&
                ball.y + ball.radius >= paddleTop &&
                ball.y - ball.radius <= paddleBottom &&
                ball.x + ball.radius >= paddleLeft &&
                ball.x - ball.radius <= paddleRight
            ) {
                ball.y = paddleTop - ball.radius
                val hitOffset = ((ball.x - paddle.x) / (paddle.currentWidth / 2f)).coerceIn(-1f, 1f)
                val maxBounceAngle = Math.toRadians(65.0)
                val bounceAngle = hitOffset * maxBounceAngle

                ball.vx = (currentSpeed * sin(bounceAngle)).toFloat()
                ball.vy = -(currentSpeed * cos(bounceAngle)).toFloat()

                soundManager.playBouncePaddle()

                // Lightweight paddle sparks (3 particles)
                for (p in 0 until 3) {
                    val angle = Random.nextFloat() * PI.toFloat() - PI.toFloat()
                    val pSpeed = Random.nextFloat() * 100f + 50f
                    addParticle(
                        Particle(
                            x = ball.x,
                            y = paddleTop,
                            vx = cos(angle) * pSpeed,
                            vy = -abs(sin(angle) * pSpeed),
                            color = Color(0xFF00E5FF),
                            radius = Random.nextFloat() * 2.5f + 1.5f,
                            maxLife = 0.25f
                        )
                    )
                }
            }

            // Brick collisions (with multi-hit durability and safe hit separation)
            checkBrickCollisions(ball)

            // Bottom Boundary check (Ball loss)
            if (ball.y - ball.radius > ARENA_HEIGHT) {
                ballsIterator.remove()
            }
        }
    }

    private fun handleBallLoss() {
        if (balls.isEmpty()) {
            _combo.value = 0
            val remainingLives = _lives.value - 1
            _lives.value = remainingLives
            soundManager.playLifeLost()

            if (remainingLives <= 0) {
                _playStatus.value = PlayStatus.GAME_OVER
                soundManager.playGameOver()
                updateHighScore()
            } else {
                // Respawn single ball attached to paddle for instant, quick re-launch
                val respawnedBall = Ball(
                    x = paddle.x,
                    y = paddle.y - paddle.height / 2f - 14f,
                    vx = 0f,
                    vy = 0f,
                    radius = 14f,
                    isAttached = true
                )
                balls.add(respawnedBall)
                _playStatus.value = PlayStatus.READY_TO_LAUNCH
            }
        }
    }

    private fun checkBrickCollisions(ball: Ball) {
        // Fast spatial cull: if ball is not in the brick field vertically, skip all checks
        if (ball.y - ball.radius > maxBrickBottom || ball.y + ball.radius < minBrickTop) {
            return
        }

        val totalBricks = bricks.size
        for (i in 0 until totalBricks) {
            val brick = bricks[i]
            if (brick.isDestroyed || brick.hitCooldown > 0f) continue

            // AABB fast rejection check taking ball radius into account
            if (ball.x + ball.radius < brick.x ||
                ball.x - ball.radius > brick.x + brick.width ||
                ball.y + ball.radius < brick.y ||
                ball.y - ball.radius > brick.y + brick.height
            ) {
                continue
            }

            // Circle - Box Collision detection with ball radius
            val closestX = ball.x.coerceIn(brick.x, brick.x + brick.width)
            val closestY = ball.y.coerceIn(brick.y, brick.y + brick.height)

            val dx = ball.x - closestX
            val dy = ball.y - closestY
            val distSq = dx * dx + dy * dy

            if (distSq < ball.radius * ball.radius) {
                // Register hit with cooldown to prevent duplicate hit counting in same contact
                brick.hitCooldown = 0.12f

                // Overlap depths from each of the 4 faces
                val overlapLeft = (ball.x + ball.radius) - brick.x
                val overlapRight = (brick.x + brick.width) - (ball.x - ball.radius)
                val overlapTop = (ball.y + ball.radius) - brick.y
                val overlapBottom = (brick.y + brick.height) - (ball.y - ball.radius)

                val canHitTop = ball.vy > 0f && overlapTop > 0f
                val canHitBottom = ball.vy < 0f && overlapBottom > 0f
                val canHitLeft = ball.vx > 0f && overlapLeft > 0f
                val canHitRight = ball.vx < 0f && overlapRight > 0f

                val verticalDepth = when {
                    canHitBottom -> overlapBottom
                    canHitTop -> overlapTop
                    else -> Float.MAX_VALUE
                }

                val horizontalDepth = when {
                    canHitLeft -> overlapLeft
                    canHitRight -> overlapRight
                    else -> Float.MAX_VALUE
                }

                // Bounce realistically: Reverse velocity along the contact axis and separate ball cleanly outside
                if (verticalDepth <= horizontalDepth && verticalDepth != Float.MAX_VALUE) {
                    // Vertical collision (Hit top or bottom)
                    if (canHitBottom) {
                        ball.y = brick.y + brick.height + ball.radius + 1.5f
                        ball.vy = abs(ball.vy)
                    } else {
                        ball.y = brick.y - ball.radius - 1.5f
                        ball.vy = -abs(ball.vy)
                    }
                } else if (horizontalDepth != Float.MAX_VALUE) {
                    // Horizontal collision (Hit left or right side)
                    if (canHitLeft) {
                        ball.x = brick.x - ball.radius - 1.5f
                        ball.vx = -abs(ball.vx)
                    } else {
                        ball.x = brick.x + brick.width + ball.radius + 1.5f
                        ball.vx = abs(ball.vx)
                    }
                } else {
                    // Fallback to minimum overlap
                    val minOverlap = min(min(abs(overlapLeft), abs(overlapRight)), min(abs(overlapTop), abs(overlapBottom)))
                    when (minOverlap) {
                        abs(overlapTop) -> {
                            ball.y = brick.y - ball.radius - 1.5f
                            ball.vy = -abs(ball.vy)
                        }
                        abs(overlapBottom) -> {
                            ball.y = brick.y + brick.height + ball.radius + 1.5f
                            ball.vy = abs(ball.vy)
                        }
                        abs(overlapLeft) -> {
                            ball.x = brick.x - ball.radius - 1.5f
                            ball.vx = -abs(ball.vx)
                        }
                        else -> {
                            ball.x = brick.x + brick.width + ball.radius + 1.5f
                            ball.vx = abs(ball.vx)
                        }
                    }
                }

                // Apply damage, scoring, audio, particles, and power-ups
                handleBrickHit(brick, ball)
                break // Bounced cleanly, process one brick impact per sub-step
            }
        }
    }

    private fun handleBrickHit(brick: Brick, ball: Ball) {
        if (brick.type == BrickType.STEEL) {
            soundManager.playSteelHit()
            // 2 sparks
            for (i in 0 until 2) {
                val pAngle = Random.nextFloat() * 2f * PI.toFloat()
                val pSpd = Random.nextFloat() * 80f + 40f
                addParticle(
                    Particle(
                        x = brick.x + brick.width / 2f,
                        y = brick.y + brick.height / 2f,
                        vx = cos(pAngle) * pSpd,
                        vy = sin(pAngle) * pSpd,
                        color = Color(0xFFB0BEC5),
                        radius = 2f,
                        maxLife = 0.2f
                    )
                )
            }
            return
        }

        // Decrement remaining durability by exactly 1 per hit
        brick.hitsRemaining--

        if (brick.hitsRemaining <= 0) {
            brick.isDestroyed = true
            val currentCombo = _combo.value + 1
            _combo.value = currentCombo

            val comboMultiplier = when {
                currentCombo >= 10 -> 3.0f
                currentCombo >= 5 -> 2.0f
                currentCombo >= 3 -> 1.5f
                else -> 1.0f
            }

            val earnedScore = (brick.scoreValue * comboMultiplier).toInt()
            _score.value += earnedScore

            // Show floating popup
            val popupText = if (comboMultiplier > 1f) "+$earnedScore (x${comboMultiplier.toInt()})" else "+$earnedScore"
            addScorePopup(popupText, brick.x + brick.width / 2f, brick.y, brick.baseColor)

            // EASY LIFE SYSTEM: Every 10 bricks broken gives +1 Extra Life!
            bricksDestroyedCount++
            if (bricksDestroyedCount % 10 == 0) {
                if (_lives.value < INITIAL_LIVES) {
                    _lives.value++
                    soundManager.playPowerUpCollected()
                    addScorePopup("EXTRA LIFE +1 💖", ball.x, ball.y - 30f, Color(0xFFFF1744))
                } else {
                    _score.value += 100
                    soundManager.playPowerUpCollected()
                    addScorePopup("MAX LIVES! +100 ⭐", ball.x, ball.y - 30f, Color(0xFFFFD600))
                }
            }

            if (brick.type == BrickType.EXPLOSIVE) {
                soundManager.playExplosion()
                triggerExplosion(brick.x + brick.width / 2f, brick.y + brick.height / 2f)
            } else {
                soundManager.playDestroyBrick()
                spawnBrickShatterParticles(brick)
            }

            // Power-Up Drops: Guaranteed from Bonus bricks, plus 15% chance during high combos
            if (brick.type == BrickType.BONUS || (currentCombo >= 3 && Random.nextFloat() < 0.15f)) {
                spawnRandomPowerUp(brick.x + brick.width / 2f, brick.y + brick.height / 2f)
            }

            updateHighScore()
        } else {
            soundManager.playHitBrick()
            // 2 small hit chips for visual feedback without lag
            for (i in 0 until 2) {
                val pAngle = Random.nextFloat() * 2f * PI.toFloat()
                val pSpd = Random.nextFloat() * 60f + 30f
                addParticle(
                    Particle(
                        x = brick.x + brick.width / 2f,
                        y = brick.y + brick.height / 2f,
                        vx = cos(pAngle) * pSpd,
                        vy = sin(pAngle) * pSpd,
                        color = brick.baseColor,
                        radius = 2f,
                        maxLife = 0.2f
                    )
                )
            }
        }
    }

    private fun triggerExplosion(centerX: Float, centerY: Float) {
        // Controlled explosion particles (8 particles max)
        for (i in 0 until 8) {
            val pAngle = Random.nextFloat() * 2f * PI.toFloat()
            val pSpd = Random.nextFloat() * 180f + 60f
            val pColor = if (Random.nextBoolean()) Color(0xFFFF1744) else Color(0xFFFF9100)
            addParticle(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = cos(pAngle) * pSpd,
                    vy = sin(pAngle) * pSpd,
                    color = pColor,
                    radius = Random.nextFloat() * 3.5f + 2f,
                    maxLife = 0.35f
                )
            )
        }

        addScorePopup("BOOM!", centerX, centerY - 20f, Color(0xFFFF1744))

        val blastRadius = 210f
        for (neighbor in bricks) {
            if (neighbor.isDestroyed || neighbor.type == BrickType.STEEL) continue
            val nbCenterX = neighbor.x + neighbor.width / 2f
            val nbCenterY = neighbor.y + neighbor.height / 2f
            val dist = hypot(centerX - nbCenterX, centerY - nbCenterY)
            if (dist <= blastRadius) {
                neighbor.hitsRemaining -= 2
                if (neighbor.hitsRemaining <= 0) {
                    neighbor.isDestroyed = true
                    _score.value += neighbor.scoreValue
                    spawnBrickShatterParticles(neighbor)
                    bricksDestroyedCount++
                    if (bricksDestroyedCount % 10 == 0) {
                        if (_lives.value < INITIAL_LIVES) {
                            _lives.value++
                            soundManager.playPowerUpCollected()
                            addScorePopup("EXTRA LIFE +1 💖", nbCenterX, nbCenterY - 30f, Color(0xFFFF1744))
                        } else {
                            _score.value += 100
                            addScorePopup("MAX LIVES! +100 ⭐", nbCenterX, nbCenterY - 30f, Color(0xFFFFD600))
                        }
                    }
                    if (neighbor.type == BrickType.BONUS) {
                        spawnRandomPowerUp(nbCenterX, nbCenterY)
                    }
                }
            }
        }
    }

    private fun spawnBrickShatterParticles(brick: Brick) {
        val count = 5 // Lightweight particles
        val centerX = brick.x + brick.width / 2f
        val centerY = brick.y + brick.height / 2f
        for (i in 0 until count) {
            val pAngle = Random.nextFloat() * 2f * PI.toFloat()
            val pSpd = Random.nextFloat() * 120f + 40f
            addParticle(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = cos(pAngle) * pSpd,
                    vy = sin(pAngle) * pSpd,
                    color = brick.baseColor,
                    radius = Random.nextFloat() * 3f + 1.5f,
                    maxLife = 0.3f
                )
            )
        }
    }

    private fun addParticle(particle: Particle) {
        if (particles.size < 25) {
            particles.add(particle)
        }
    }

    private fun spawnRandomPowerUp(x: Float, y: Float) {
        val choices = listOf(
            PowerUpType.WIDE_PADDLE,
            PowerUpType.SLOW_BALL,
            PowerUpType.MULTI_BALL,
            PowerUpType.EXTRA_LIFE
        )
        val selected = choices.random()
        powerUps.add(
            PowerUp(
                id = ++powerUpIdGen,
                x = x,
                y = y,
                type = selected
            )
        )
    }

    private fun updatePowerUps(dt: Float) {
        val iter = powerUps.iterator()
        val paddleTop = paddle.y - paddle.height / 2f
        val paddleBottom = paddle.y + paddle.height / 2f
        val paddleLeft = paddle.x - paddle.currentWidth / 2f
        val paddleRight = paddle.x + paddle.currentWidth / 2f

        while (iter.hasNext()) {
            val p = iter.next()
            p.y += p.vy * dt

            // Check collision with paddle
            if (p.y + p.radius >= paddleTop &&
                p.y - p.radius <= paddleBottom &&
                p.x + p.radius >= paddleLeft &&
                p.x - p.radius <= paddleRight
            ) {
                applyPowerUp(p.type)
                addScorePopup(p.type.label, p.x, p.y - 15f, p.type.color)
                soundManager.playPowerUpCollected()
                iter.remove()
                continue
            }

            // Exceeded bottom
            if (p.y - p.radius > ARENA_HEIGHT) {
                iter.remove()
            }
        }
    }

    private fun applyPowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.WIDE_PADDLE, PowerUpType.SLOW_BALL -> {
                val current = _activePowerUps.value.toMutableMap()
                current[type] = type.durationMs / 1000f
                _activePowerUps.value = current
            }
            PowerUpType.MULTI_BALL -> {
                // Duplicate active ball with spread angles
                val referenceBall = balls.firstOrNull { !it.isAttached } ?: balls.firstOrNull()
                if (referenceBall != null) {
                    val currentSpeed = getEffectiveSpeed()
                    for (i in 1..2) {
                        val angleOffset = if (i == 1) 0.45f else -0.45f
                        val currentAngle = atan2(referenceBall.vy, referenceBall.vx)
                        val newAngle = currentAngle + angleOffset
                        balls.add(
                            Ball(
                                x = referenceBall.x,
                                y = referenceBall.y,
                                vx = currentSpeed * cos(newAngle),
                                vy = currentSpeed * sin(newAngle),
                                radius = referenceBall.radius,
                                isAttached = false
                            )
                        )
                    }
                }
            }
            PowerUpType.EXTRA_LIFE -> {
                _lives.value = min(INITIAL_LIVES, _lives.value + 1)
            }
        }
    }

    private fun updatePowerUpTimers(dt: Float) {
        val current = _activePowerUps.value
        if (current.isEmpty()) return

        var changed = false
        val newMap = mutableMapOf<PowerUpType, Float>()
        for ((type, time) in current) {
            val remaining = time - dt
            if (remaining > 0f) {
                newMap[type] = remaining
                if (ceil(time).toInt() != ceil(remaining).toInt()) {
                    changed = true
                }
            } else {
                changed = true
            }
        }
        if (changed || newMap.size != current.size) {
            _activePowerUps.value = newMap
        }
    }

    private fun updateVisualEffects(dt: Float) {
        // Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.currentLife += dt
            if (p.currentLife >= p.maxLife) {
                pIter.remove()
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.alpha = 1f - (p.currentLife / p.maxLife)
            }
        }

        // Score popups
        val spIter = scorePopups.iterator()
        while (spIter.hasNext()) {
            val sp = spIter.next()
            sp.currentLife += dt
            if (sp.currentLife >= sp.maxLife) {
                spIter.remove()
            } else {
                sp.y -= 70f * dt
                sp.alpha = 1f - (sp.currentLife / sp.maxLife)
            }
        }
    }

    private fun addScorePopup(text: String, x: Float, y: Float, color: Color) {
        if (scorePopups.size >= 5) {
            scorePopups.removeAt(0)
        }
        scorePopups.add(
            ScorePopup(
                id = ++popupIdGen,
                text = text,
                x = x,
                y = y,
                color = color
            )
        )
    }

    private fun checkLevelCompletion() {
        val remainingTargets = bricks.count { !it.isDestroyed && it.type != BrickType.STEEL }
        if (remainingTargets == 0) {
            _playStatus.value = PlayStatus.LEVEL_COMPLETED
            soundManager.playLevelComplete()

            // Bonus points for remaining lives and level clear
            val livesBonus = _lives.value * 100
            val levelBonus = _currentLevel.value * 200
            val totalEarned = livesBonus + levelBonus
            _score.value += totalEarned
            updateHighScore()

            // Unlock next level in preferences
            val nextLvl = _currentLevel.value + 1
            if (nextLvl > preferences.unlockedLevel && nextLvl <= LevelData.levels.size) {
                preferences.unlockedLevel = nextLvl
            }
            val stars = when {
                _lives.value >= 4 -> 3
                _lives.value >= 2 -> 2
                else -> 1
            }
            preferences.setLevelStars(_currentLevel.value, stars)
            preferences.setLevelHighScore(_currentLevel.value, _score.value)
        }
    }

    private fun updateHighScore() {
        if (_score.value > _bestScore.value) {
            _bestScore.value = _score.value
            preferences.bestScore = _score.value
        }
    }
}
