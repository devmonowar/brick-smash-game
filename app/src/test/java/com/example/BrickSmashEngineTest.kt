package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.engine.BrickSmashEngine
import com.example.engine.LevelData
import com.example.model.BrickType
import com.example.model.PlayStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrickSmashEngineTest {

    private lateinit var context: Context
    private lateinit var soundManager: SoundManager
    private lateinit var preferences: GamePreferences
    private lateinit var engine: BrickSmashEngine

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        soundManager = SoundManager(context).apply {
            isSoundEnabled = false // Silent in tests
            isHapticsEnabled = false
        }
        preferences = GamePreferences(context)
        preferences.resetProgress()
        engine = BrickSmashEngine(soundManager, preferences)
    }

    @Test
    fun testLevel1Initialization() {
        assertEquals(1, engine.currentLevel.value)
        assertEquals(PlayStatus.READY_TO_LAUNCH, engine.playStatus.value)
        assertEquals(5, engine.lives.value)
        assertEquals(0, engine.score.value)
        assertEquals(1, engine.balls.size)
        assertTrue(engine.balls[0].isAttached)
        assertTrue(engine.bricks.isNotEmpty())
    }

    @Test
    fun testBallLaunch() {
        engine.launchBall()
        assertEquals(PlayStatus.BALL_IN_PLAY, engine.playStatus.value)
        assertFalse(engine.balls[0].isAttached)
        assertNotEquals(0f, engine.balls[0].vy)
    }

    @Test
    fun testPaddleMovementClamped() {
        // Move far left
        engine.setPaddleX(-500f)
        assertTrue(engine.paddle.x >= engine.paddle.currentWidth / 2f)

        // Move far right
        engine.setPaddleX(2000f)
        assertTrue(engine.paddle.x <= BrickSmashEngine.ARENA_WIDTH - engine.paddle.currentWidth / 2f)
    }

    @Test
    fun testKeyboardSteeringLeftRightAndStop() {
        val initialX = engine.paddle.x

        // Steering Left
        engine.keyboardSteeringDirection = -1f
        engine.update(0.05f)
        val leftX = engine.paddle.x
        assertTrue("Paddle should move left", leftX < initialX)

        // Steering Right
        engine.keyboardSteeringDirection = 1f
        engine.update(0.1f)
        val rightX = engine.paddle.x
        assertTrue("Paddle should move right", rightX > leftX)

        // Key released / stopped
        engine.keyboardSteeringDirection = 0f
        engine.update(0.05f)
        assertEquals("Paddle should stop when key released", rightX, engine.paddle.x, 0.001f)
    }

    @Test
    fun testKeyboardSteeringClampedWithinArenaBounds() {
        // Continuous left steering cannot exit left boundary
        engine.keyboardSteeringDirection = -1f
        engine.update(5.0f)
        val minAllowed = engine.paddle.currentWidth / 2f + 20f
        assertTrue("Paddle cannot cross left bound", engine.paddle.x >= minAllowed)

        // Continuous right steering cannot exit right boundary
        engine.keyboardSteeringDirection = 1f
        engine.update(5.0f)
        val maxAllowed = BrickSmashEngine.ARENA_WIDTH - engine.paddle.currentWidth / 2f - 20f
        assertTrue("Paddle cannot cross right bound", engine.paddle.x <= maxAllowed)
    }

    @Test
    fun testTenLevelsLoadedProperly() {
        assertEquals(10, LevelData.levels.size)
        for (i in 1..10) {
            engine.loadLevel(i)
            assertEquals(i, engine.currentLevel.value)
            assertTrue("Level $i should have bricks", engine.bricks.isNotEmpty())
        }
    }

    @Test
    fun testBrickDestructionAndScoring() {
        engine.launchBall()
        val initialScore = engine.score.value

        // Find first normal brick
        val normalBrick = engine.bricks.first { it.type == BrickType.NORMAL }
        // Place ball directly at brick center
        val ball = engine.balls[0]
        ball.x = normalBrick.x + normalBrick.width / 2f
        ball.y = normalBrick.y + normalBrick.height / 2f
        ball.vy = -100f

        engine.update(0.016f)

        assertTrue(normalBrick.isDestroyed)
        assertTrue(engine.score.value > initialScore)
        assertTrue(engine.combo.value >= 1)
    }

    @Test
    fun testHardBrickRequiresMultipleHits() {
        engine.loadLevel(2) // Level 2 has hard bricks
        engine.launchBall()
        val hardBrick = engine.bricks.first { it.type == BrickType.HARD }
        val initialHits = hardBrick.hitsRemaining
        assertTrue(initialHits >= 2)

        // Place ball at hard brick
        val ball = engine.balls[0]
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        ball.vy = -100f

        engine.update(0.016f)

        assertEquals(initialHits - 1, hardBrick.hitsRemaining)
        assertFalse(hardBrick.isDestroyed)
    }

    @Test
    fun testBallLossDecrementsLife() {
        engine.launchBall()
        val initialLives = engine.lives.value

        // Move ball below screen
        val ball = engine.balls[0]
        ball.y = BrickSmashEngine.ARENA_HEIGHT + 100f

        engine.update(0.016f)

        assertEquals(initialLives - 1, engine.lives.value)
        assertEquals(PlayStatus.READY_TO_LAUNCH, engine.playStatus.value)
        assertEquals(1, engine.balls.size)
        assertTrue(engine.balls[0].isAttached)
    }

    @Test
    fun testGameOverWhenAllLivesLost() {
        for (life in 1..5) {
            engine.launchBall()
            val ball = engine.balls.first()
            ball.y = BrickSmashEngine.ARENA_HEIGHT + 100f
            engine.update(0.016f)
        }

        assertEquals(0, engine.lives.value)
        assertEquals(PlayStatus.GAME_OVER, engine.playStatus.value)
    }

    @Test
    fun testEasyLifeSystemAwardsExtraLifeEvery10Bricks() {
        engine.launchBall()
        // Simulate having 3 lives (below max 5)
        engine.balls.first().y = BrickSmashEngine.ARENA_HEIGHT + 100f
        engine.update(0.016f) // 4 lives
        engine.launchBall()
        engine.balls.first().y = BrickSmashEngine.ARENA_HEIGHT + 100f
        engine.update(0.016f) // 3 lives
        assertEquals(3, engine.lives.value)

        engine.launchBall()
        // Smash 10 normal bricks
        val normalBricks = engine.bricks.filter { it.type == BrickType.NORMAL }.take(10)
        for (b in normalBricks) {
            val currentBall = engine.balls.first()
            currentBall.x = b.x + b.width / 2f
            currentBall.y = b.y + b.height / 2f
            currentBall.vy = -100f
            engine.update(0.016f)
        }

        // Lives should have increased from 3 to 4!
        assertEquals(4, engine.lives.value)
    }

    @Test
    fun testStrongBrickRequiresTwoHits() {
        engine.loadLevel(2)
        engine.launchBall()
        val strongBrick = engine.bricks.first { it.type == BrickType.STRONG }
        assertEquals(2, strongBrick.hitsRemaining)

        // 1st hit
        val ball = engine.balls[0]
        ball.x = strongBrick.x + strongBrick.width / 2f
        ball.y = strongBrick.y + strongBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(1, strongBrick.hitsRemaining)
        assertFalse(strongBrick.isDestroyed)

        // Reset cooldown to simulate ball leaving and re-impacting
        strongBrick.hitCooldown = 0f

        // 2nd hit
        ball.x = strongBrick.x + strongBrick.width / 2f
        ball.y = strongBrick.y + strongBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(0, strongBrick.hitsRemaining)
        assertTrue(strongBrick.isDestroyed)
    }

    @Test
    fun testHardBrickRequiresThreeHits() {
        engine.loadLevel(2)
        engine.launchBall()
        val hardBrick = engine.bricks.first { it.type == BrickType.HARD }
        assertEquals(3, hardBrick.hitsRemaining)

        // 1st hit
        val ball = engine.balls[0]
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(2, hardBrick.hitsRemaining)
        assertFalse(hardBrick.isDestroyed)

        // Reset cooldown
        hardBrick.hitCooldown = 0f

        // 2nd hit
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(1, hardBrick.hitsRemaining)
        assertFalse(hardBrick.isDestroyed)

        // Reset cooldown
        hardBrick.hitCooldown = 0f

        // 3rd hit
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(0, hardBrick.hitsRemaining)
        assertTrue(hardBrick.isDestroyed)
    }

    @Test
    fun testBricksPositionsAreFixedAndNeverShiftOnHit() {
        engine.loadLevel(1)
        engine.launchBall()

        val initialPositions = engine.bricks.map { it.id to Pair(it.x, it.y) }.toMap()

        // Destroy the first brick
        val targetBrick = engine.bricks.first { it.type == BrickType.NORMAL }
        val ball = engine.balls[0]
        ball.x = targetBrick.x + targetBrick.width / 2f
        ball.y = targetBrick.y + targetBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertTrue(targetBrick.isDestroyed)

        // Every brick in the level must remain at its EXACT original position!
        for (b in engine.bricks) {
            val initialPos = initialPositions[b.id]
            assertNotNull(initialPos)
            assertEquals("Brick ${b.id} X position must never shift", initialPos!!.first, b.x, 0.0001f)
            assertEquals("Brick ${b.id} Y position must never shift", initialPos.second, b.y, 0.0001f)
        }
    }

    @Test
    fun testSingleHitPerCollisionCooldownPreventsMultipleDamageInOneContact() {
        engine.loadLevel(2)
        engine.launchBall()
        val hardBrick = engine.bricks.first { it.type == BrickType.HARD }
        val ball = engine.balls[0]

        // 1st collision
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        ball.vy = -100f
        engine.update(0.016f)

        assertEquals(2, hardBrick.hitsRemaining)
        assertTrue("hitCooldown must be active after hit", hardBrick.hitCooldown > 0f)

        // Immediate subsequent sub-step without moving ball away:
        // Because hitCooldown > 0f, hitsRemaining MUST NOT decrease again!
        ball.x = hardBrick.x + hardBrick.width / 2f
        ball.y = hardBrick.y + hardBrick.height / 2f
        engine.update(0.005f)

        assertEquals("Same collision must not count multiple hits", 2, hardBrick.hitsRemaining)
    }

    @Test
    fun testBallBouncesWhenDestroyingNormalBrickFromBottom() {
        engine.loadLevel(1)
        engine.launchBall()

        // Choose the bottom-most normal brick so the space below it is open air
        val normalBrick = engine.bricks.filter { it.type == BrickType.NORMAL }.maxByOrNull { it.y }!!
        val ball = engine.balls[0]

        // Position ball just below the brick moving upward
        ball.x = normalBrick.x + normalBrick.width / 2f
        ball.y = normalBrick.y + normalBrick.height + ball.radius - 1f
        ball.vx = 0f
        ball.vy = -480f

        engine.update(0.005f)

        // Brick must be destroyed
        assertTrue("Brick should be destroyed", normalBrick.isDestroyed)
        // Ball must NOT penetrate! It must bounce downwards with positive vy
        assertTrue("Vertical velocity must reverse downward upon impact", ball.vy > 0f)
        assertTrue("Ball must be placed outside bottom of brick", ball.y >= normalBrick.y + normalBrick.height + ball.radius)
    }

    @Test
    fun testBallBouncesWhenHittingBrickFromTop() {
        engine.loadLevel(1)
        engine.launchBall()

        // Choose a top-most normal brick so the space above it is open air
        val normalBrick = engine.bricks.filter { it.type == BrickType.NORMAL }.minByOrNull { it.y }!!
        val ball = engine.balls[0]

        // Position ball just above the brick moving downward
        ball.x = normalBrick.x + normalBrick.width / 2f
        ball.y = normalBrick.y - ball.radius + 1f
        ball.vx = 0f
        ball.vy = 480f

        engine.update(0.005f)

        assertTrue(normalBrick.isDestroyed)
        // Ball must bounce upwards with negative vy
        assertTrue("Vertical velocity must reverse upward upon top impact", ball.vy < 0f)
        assertTrue("Ball must be placed outside top of brick", ball.y <= normalBrick.y - ball.radius)
    }

    @Test
    fun testBallBouncesWhenHittingBrickFromLeft() {
        engine.loadLevel(4) // Checkerboard level has isolated bricks with empty space around
        engine.launchBall()

        val normalBrick = engine.bricks.first { it.type == BrickType.NORMAL && it.x in 300f..700f }
        val ball = engine.balls[0]

        // Position ball just left of the brick moving right
        ball.x = normalBrick.x - ball.radius + 1f
        ball.y = normalBrick.y + normalBrick.height / 2f
        ball.vx = 480f
        ball.vy = 0f

        engine.update(0.005f)

        assertTrue(normalBrick.isDestroyed)
        // Ball must bounce to the left with negative vx
        assertTrue("Horizontal velocity must reverse left upon left face impact", ball.vx < 0f)
        assertTrue("Ball must be placed outside left of brick", ball.x <= normalBrick.x - ball.radius)
    }

    @Test
    fun testBallBouncesWhenHittingBrickFromRight() {
        engine.loadLevel(4) // Checkerboard level has isolated bricks with empty space around
        engine.launchBall()

        val normalBrick = engine.bricks.first { it.type == BrickType.NORMAL && it.x in 300f..700f }
        val ball = engine.balls[0]

        // Position ball just right of the brick moving left
        ball.x = normalBrick.x + normalBrick.width + ball.radius - 1f
        ball.y = normalBrick.y + normalBrick.height / 2f
        ball.vx = -480f
        ball.vy = 0f

        engine.update(0.005f)

        assertTrue(normalBrick.isDestroyed)
        // Ball must bounce to the right with positive vx
        assertTrue("Horizontal velocity must reverse right upon right face impact", ball.vx > 0f)
        assertTrue("Ball must be placed outside right of brick", ball.x >= normalBrick.x + normalBrick.width + ball.radius)
    }

    @Test
    fun testAdminLoadAnyLevelDirectlyInitializesCleanState() {
        // First simulate a dirty state (score accumulated, life lost)
        engine.launchBall()
        engine.balls.clear()
        engine.update(0.016f) // triggers life lost

        // Now Admin selects Level 7 directly
        engine.loadLevel(7, resetScore = true)

        assertEquals(7, engine.currentLevel.value)
        assertEquals(0, engine.score.value)
        assertEquals(5, engine.lives.value)
        assertEquals(0, engine.combo.value)
        assertEquals(PlayStatus.READY_TO_LAUNCH, engine.playStatus.value)
        assertEquals(1, engine.balls.size)
        assertTrue(engine.balls[0].isAttached)
        assertEquals(LevelData.levels[6].baseBallSpeed, engine.getEffectiveSpeed(), 0.1f)
        assertTrue(engine.bricks.isNotEmpty())
        assertTrue(engine.activePowerUps.value.isEmpty())
    }

    @Test
    fun testAdminLoadInvalidLevelClampsGracefully() {
        // Below 1
        engine.loadLevel(0, resetScore = true)
        assertEquals(1, engine.currentLevel.value)

        // Above max level (10)
        engine.loadLevel(99, resetScore = true)
        assertEquals(LevelData.levels.size, engine.currentLevel.value)
    }
}

