package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F1738),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    color = Color(0xFF00E5FF),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESUME", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("restart_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RESTART LEVEL", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onToggleSound) {
                        Icon(
                            if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSoundEnabled) "Sound: ON" else "Sound: OFF", color = Color.White, fontSize = 12.sp)
                    }

                    TextButton(onClick = onToggleHaptics) {
                        Icon(
                            if (isHapticsEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHapticsEnabled) "Haptics: ON" else "Haptics: OFF", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onMainMenu,
                    modifier = Modifier.testTag("main_menu_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFFFF5252))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MAIN MENU", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    finalScore: Int,
    bestScore: Int,
    isNewBest: Boolean,
    onTryAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF140D24),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF1744)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color(0xFFFF1744),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isNewBest) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFD600).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFD600), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "★ NEW HIGH SCORE! ★",
                            color = Color(0xFFFFD600),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text = "FINAL SCORE",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$finalScore",
                    color = Color(0xFF00E5FF),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BEST SCORE: $bestScore",
                    color = Color(0xFF90A4AE),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onTryAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("try_again_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TRY AGAIN", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onMainMenu,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("gameover_main_menu_button")
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MAIN MENU", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun LevelCompleteDialog(
    level: Int,
    levelName: String,
    score: Int,
    stars: Int,
    hasNextLevel: Boolean,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMainMenu: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B192C),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00E676)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL COMPLETE!",
                    color = Color(0xFF00E676),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Level $level: $levelName",
                    color = Color(0xFFB0BEC5),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars Display
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..3) {
                        val active = i <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (active) Color(0xFFFFD600) else Color(0x33FFD600),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TOTAL SCORE",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp
                )
                Text(
                    text = "$score",
                    color = Color(0xFFFFD600),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_level_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NEXT LEVEL", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFD600).copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "👑 YOU BEAT ALL LEVELS! 👑",
                            color = Color(0xFFFFD600),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                OutlinedButton(
                    onClick = onReplay,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REPLAY LEVEL", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onMainMenu) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MAIN MENU", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F1738),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HOW TO PLAY",
                    color = Color(0xFF00E5FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GuideItem("🕹️ Controls", "Touch & drag to move paddle, or use Left/Right Arrow keys (←/→ or A/D). Tap, Space, or Enter to launch.")
                    GuideItem("🧱 Normal Bricks", "1 hit to break (10 pts). Breaks with glowing particles.")
                    GuideItem("🛡️ Hard Bricks", "Takes 2 to 4 hits! Shows remaining hits and cracks (25-50 pts).")
                    GuideItem("💥 Explosive Bricks", "Detonates adjacent bricks in a fiery chain explosion (30 pts)!")
                    GuideItem("⚡ Steel Bricks", "Indestructible obstacles that bounce your ball at new angles.")
                    GuideItem("⭐ Bonus Power-Ups", "Drops Wide Paddle, Slow Ball, Multi-Ball (+2 balls), and Extra Life (+1 heart)!")
                    GuideItem("🔥 Combos", "Consecutive brick hits multiply your score up to x3!")
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("how_to_play_got_it_button")
                ) {
                    Text("GOT IT!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GuideItem(title: String, description: String) {
    Column {
        Text(text = title, color = Color(0xFFFFD600), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = description, color = Color(0xFFECEFF1), fontSize = 11.5.sp, lineHeight = 15.sp)
    }
}
