package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.data.GamePreferences
import com.example.engine.LevelData

@Composable
fun LevelSelectScreen(
    preferences: GamePreferences,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    val unlockedLevel = preferences.unlockedLevel
    var devUnlockAll by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060919),
                        Color(0xFF0C1438),
                        Color(0xFF060919)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10193E))
                    .testTag("level_select_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Main Menu",
                    tint = Color(0xFF00E5FF)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SELECT LEVEL",
                    color = Color(0xFF00E5FF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "10 Arcade Arenas",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp
                )
            }

            // Admin / Dev Unlock All Mode Toggle
            FilterChip(
                selected = devUnlockAll,
                onClick = { devUnlockAll = !devUnlockAll },
                label = {
                    Text(
                        text = if (devUnlockAll) "DEV: ALL" else "PROGRESS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (devUnlockAll) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFD600),
                    selectedLabelColor = Color.Black,
                    selectedLeadingIconColor = Color.Black,
                    containerColor = Color(0xFF16234D),
                    labelColor = Color(0xFFB0BEC5),
                    iconColor = Color(0xFFB0BEC5)
                ),
                modifier = Modifier.testTag("dev_unlock_toggle")
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(LevelData.levels) { def ->
                val naturallyUnlocked = def.levelNumber <= unlockedLevel
                val isPlayable = devUnlockAll || naturallyUnlocked
                val stars = preferences.getLevelStars(def.levelNumber)
                val highScore = preferences.getLevelHighScore(def.levelNumber)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isPlayable) Color(0xFF101C45) else Color(0xFF0A0F26)
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                naturallyUnlocked -> Color(0xFF00E5FF).copy(alpha = 0.5f)
                                isPlayable -> Color(0xFFFFD600).copy(alpha = 0.5f)
                                else -> Color(0x33FFFFFF)
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = isPlayable) {
                            onSelectLevel(def.levelNumber)
                        }
                        .padding(12.dp)
                        .testTag("level_item_${def.levelNumber}")
                ) {
                    if (isPlayable) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LVL ${def.levelNumber}",
                                        color = if (naturallyUnlocked) Color(0xFF00E5FF) else Color(0xFFFFD600),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (!naturallyUnlocked && devUnlockAll) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "[DEV]",
                                            color = Color(0xFFFFD600),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Stars
                                Row {
                                    for (s in 1..3) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (s <= stars) Color(0xFFFFD600) else Color(0x33FFD600),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Column {
                                Text(
                                    text = def.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = def.subtitle,
                                    color = Color(0xFF90A4AE),
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                            if (highScore > 0) {
                                Text(
                                    text = "Best: $highScore",
                                    color = Color(0xFFFFD600),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Speed: ${def.baseBallSpeed.toInt()} px/s",
                                    color = Color(0xFF78909C),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    } else {
                        // Locked State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF546E7A),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "LVL ${def.levelNumber}",
                                color = Color(0xFF546E7A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
