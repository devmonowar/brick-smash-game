package com.example.engine

import androidx.compose.ui.graphics.Color
import com.example.model.Brick
import com.example.model.BrickType

data class LevelDefinition(
    val levelNumber: Int,
    val name: String,
    val subtitle: String,
    val baseBallSpeed: Float,
    val layout: List<String>
)

object LevelData {

    // Legend for layout strings:
    // '.' : Empty space (allows ball to penetrate and ricochet inside)
    // 'N' : Normal Brick (1 hit, 10 pts)
    // 'H' : Hard Brick 2-hits (25 pts)
    // '3' : Hard Brick 3-hits (35 pts)
    // '4' : Hard Brick 4-hits (50 pts)
    // 'S' : Steel Brick (Indestructible, not a required target)
    // 'E' : Explosive Brick (30 pts, AOE explosion)
    // 'B' : Bonus Brick (50 pts, drops power-up)

    val levels: List<LevelDefinition> = listOf(
        // Level 1: Horizontal Rows (Beginner-Friendly, warm-up)
        LevelDefinition(
            levelNumber = 1,
            name = "Horizontal Rows",
            subtitle = "Spaced rows for smooth bouncing",
            baseBallSpeed = 480f,
            layout = listOf(
                "NN.NN.NN.NN",
                "NN.NB.BN.NN",
                "NN.NN.NN.NN",
                ".N.NN.NN.N."
            )
        ),
        // Level 2: Pyramid Peak (Pyramid layout)
        LevelDefinition(
            levelNumber = 2,
            name = "Pyramid Peak",
            subtitle = "Stepped pyramid with corridor gaps",
            baseBallSpeed = 500f,
            layout = listOf(
                "....NNNN....",
                "...NN..NN...",
                "..NN.BB.NN..",
                ".NN.3HH3.NN.",
                "NNNNNNNNNNNN"
            )
        ),
        // Level 3: Diamond Matrix (Diamond layout)
        LevelDefinition(
            levelNumber = 3,
            name = "Diamond Matrix",
            subtitle = "Geometric diamond with bonus heart",
            baseBallSpeed = 520f,
            layout = listOf(
                "....EEEE....",
                "..NN....NN..",
                ".NN.HBBH.NN.",
                "..NN....NN..",
                "....EEEE...."
            )
        ),
        // Level 4: Checkerboard (Checkerboard layout)
        LevelDefinition(
            levelNumber = 4,
            name = "Checkerboard",
            subtitle = "Alternating pattern allows ball entry",
            baseBallSpeed = 540f,
            layout = listOf(
                "N.N.N.N.N.N",
                ".N.E.B.E.N.",
                "N.B.H.H.B.N",
                ".N.E.B.E.N.",
                "N.N.N.N.N.N"
            )
        ),
        // Level 5: Zigzag Neon (Zigzag layout)
        LevelDefinition(
            levelNumber = 5,
            name = "Zigzag Neon",
            subtitle = "Snake the ball through zigzag tracks",
            baseBallSpeed = 560f,
            layout = listOf(
                "NNNNNN......",
                "....NNNNNN..",
                "......NNNNNN",
                "..H..B..B..H",
                "NNNNNN......",
                "....NNNNNN.."
            )
        ),
        // Level 6: Twin Pillars (Vertical column layout)
        LevelDefinition(
            levelNumber = 6,
            name = "Twin Pillars",
            subtitle = "Vertical pillars with a central chute",
            baseBallSpeed = 580f,
            layout = listOf(
                "NN..NN..NN..NN",
                "NN..EB..BE..NN",
                "NN..H....H..NN",
                "NN..EB..BE..NN",
                "NN..NN..NN..NN"
            )
        ),
        // Level 7: The Corridor (Tunnel/Corridor layout)
        LevelDefinition(
            levelNumber = 7,
            name = "The Corridor",
            subtitle = "Punch inside for multi-brick smash!",
            baseBallSpeed = 600f,
            layout = listOf(
                "NNNN..NNNN",
                "N........N",
                "N.NNBBNN.N",
                "N.3HEEH3.N",
                "N........N",
                "NNNN..NNNN"
            )
        ),
        // Level 8: Archway Dome (Circular/Arch-style layout)
        LevelDefinition(
            levelNumber = 8,
            name = "Archway Dome",
            subtitle = "Domed arch protecting explosive cores",
            baseBallSpeed = 620f,
            layout = listOf(
                "...NNNNNN...",
                ".NN......NN.",
                "NN.E3BB3E.NN",
                "NN..HHHH..NN",
                ".N........N."
            )
        ),
        // Level 9: Minefield Chaos (Balanced explosive cluster layout)
        LevelDefinition(
            levelNumber = 9,
            name = "Minefield Chaos",
            subtitle = "Trigger chain detonations inside!",
            baseBallSpeed = 640f,
            layout = listOf(
                "E..N..B..N..E",
                ".3..E..E..3.",
                "N..B..S..B..N",
                ".3..E..E..3.",
                "E..N..B..N..E"
            )
        ),
        // Level 10: Cosmic Finale (Grand finale)
        LevelDefinition(
            levelNumber = 10,
            name = "Cosmic Finale",
            subtitle = "The master challenge with all mechanics",
            baseBallSpeed = 660f,
            layout = listOf(
                "4.EE.BB.EE.4",
                ".4.3HH3.4.",
                "S..N..N..S",
                ".E.B..B.E.",
                "NNNNNNNNNN"
            )
        )
    )

    fun createBricksForLevel(
        levelIndex: Int,
        arenaWidth: Float = 1000f,
        topOffset: Float = 220f
    ): List<Brick> {
        val level = levels.getOrElse(levelIndex) { levels.first() }
        val rows = level.layout.size
        val cols = level.layout.maxOfOrNull { it.length } ?: 8

        val marginX = 50f
        val availableWidth = arenaWidth - (2 * marginX)
        val spacingX = 8f
        val spacingY = 10f

        val brickWidth = (availableWidth - (spacingX * (cols - 1))) / cols
        val brickHeight = 36f

        val bricks = mutableListOf<Brick>()
        var brickId = 0

        for (r in 0 until rows) {
            val rowStr = level.layout[r]
            for (c in 0 until rowStr.length) {
                val char = rowStr[c]
                if (char == '.') continue

                val bx = marginX + c * (brickWidth + spacingX)
                val by = topOffset + r * (brickHeight + spacingY)

                val (type, maxHits, color, score) = when (char) {
                    'N' -> {
                        val rowColor = when (r % 5) {
                            0 -> Color(0xFF00E5FF) // Neon Cyan
                            1 -> Color(0xFFFF2A7A) // Neon Magenta
                            2 -> Color(0xFFFFD600) // Neon Yellow
                            3 -> Color(0xFF00E676) // Neon Green
                            else -> Color(0xFF7C4DFF) // Neon Purple
                        }
                        Tuple4(BrickType.NORMAL, 1, rowColor, 10)
                    }
                    'H' -> Tuple4(BrickType.STRONG, 2, Color(0xFFFF9100), 25)
                    '3', '4' -> Tuple4(BrickType.HARD, 3, Color(0xFFFF1744), 40)
                    'S' -> Tuple4(BrickType.STEEL, 9999, Color(0xFF90A4AE), 0)
                    'E' -> Tuple4(BrickType.EXPLOSIVE, 1, Color(0xFFFF1744), 30)
                    'B' -> Tuple4(BrickType.BONUS, 1, Color(0xFFE040FB), 50)
                    else -> Tuple4(BrickType.NORMAL, 1, Color(0xFF00E5FF), 10)
                }

                bricks.add(
                    Brick(
                        id = ++brickId,
                        row = r,
                        col = c,
                        x = bx,
                        y = by,
                        width = brickWidth,
                        height = brickHeight,
                        type = type,
                        hitsRemaining = maxHits,
                        maxHits = maxHits,
                        isDestroyed = false,
                        baseColor = color,
                        scoreValue = score
                    )
                )
            }
        }
        return bricks
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
