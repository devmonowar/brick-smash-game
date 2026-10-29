package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PowerUpType

@Composable
fun GameHUD(
    level: Int,
    levelName: String,
    score: Int,
    bestScore: Int,
    lives: Int,
    combo: Int,
    activePowerUps: Map<PowerUpType, Float>,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF0060919),
                        Color(0xD0080D24),
                        Color(0x00080D24)
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Row: Level Title, Scores, Pause & Audio Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level & Score
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LVL $level",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = levelName,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SCORE: ",
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$score",
                        color = Color(0xFFFFD600),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "BEST: $bestScore",
                        color = Color(0xFF78909C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Right Action Buttons: Sound & Pause
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tint = if (isSoundEnabled) Color(0xFF00E5FF) else Color(0xFF78909C)
                    )
                }

                IconButton(
                    onClick = onPauseClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5FF))
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Middle Row: Lives & Combo badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5 Lives Hearts
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    val hasLife = i <= lives
                    Icon(
                        imageVector = if (hasLife) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Life $i",
                        tint = if (hasLife) Color(0xFFFF1744) else Color(0x44FF1744),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 2.dp)
                    )
                }
            }

            // Combo Badge
            AnimatedVisibility(
                visible = combo >= 2,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF9100).copy(alpha = 0.25f))
                        .border(1.dp, Color(0xFFFF9100), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "COMBO x${when { combo >= 10 -> 3; combo >= 5 -> 2; else -> "1.5" }}",
                        color = Color(0xFFFFAB40),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Bottom Row: Active Power-Up Bars
        if (activePowerUps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for ((type, timeLeft) in activePowerUps) {
                    val totalSec = type.durationMs / 1000f
                    val progress = (timeLeft / totalSec).coerceIn(0f, 1f)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = type.label,
                                color = type.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${timeLeft.toInt()}s",
                                color = type.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = type.color,
                            trackColor = type.color.copy(alpha = 0.2f),
                        )
                    }
                }
            }
        }
    }
}
