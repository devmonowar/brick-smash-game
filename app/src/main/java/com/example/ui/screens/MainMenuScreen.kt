package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LevelData

@Composable
fun MainMenuScreen(
    bestScore: Int,
    isSoundEnabled: Boolean,
    onToggleSound: () -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    onPlayClick: () -> Unit,
    onLevelSelectClick: () -> Unit,
    onHowToPlayClick: () -> Unit,
    onAdminSelectLevel: (Int) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    // Admin Level Selection state
    var adminLevelInput by remember { mutableStateOf("1") }
    var adminErrorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    val parsedLevel = adminLevelInput.trim().toIntOrNull()
    val isLevelValid = parsedLevel != null && parsedLevel in 1..LevelData.levels.size
    val selectedDef = if (isLevelValid) LevelData.levels[parsedLevel!! - 1] else null

    Box(
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
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Neon Brick Accent Header
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Color(0xFF00E5FF),
                    Color(0xFFFF2A7A),
                    Color(0xFFFFD600),
                    Color(0xFF00E676),
                    Color(0xFF7C4DFF)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Logo Title
            Text(
                text = "BRICK",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00E5FF),
                letterSpacing = 4.sp
            )
            Text(
                text = "SMASH",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFF2A7A),
                letterSpacing = 6.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "RETRO ARCADE BRICK BREAKER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90A4AE),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Best Score Card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10193E))
                    .border(1.dp, Color(0x5500E5FF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trophy",
                        tint = Color(0xFFFFD600),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HIGH SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB0BEC5)
                        )
                        Text(
                            text = "$bestScore",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD600)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PLAY BUTTON (Large Pulsing Action)
            Button(
                onClick = onPlayClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(56.dp)
                    .scale(pulseScale)
                    .testTag("play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY GAME",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // LEVEL SELECT BUTTON (Normal mode)
            OutlinedButton(
                onClick = onLevelSelectClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFF7C4DFF)),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(50.dp)
                    .testTag("level_select_button")
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFF7C4DFF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SELECT LEVEL (1-10)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // ADMIN / DEV LEVEL SELECTOR CARD
            // ==========================================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A3B)),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (isLevelValid) Color(0xFFFFD600).copy(alpha = 0.8f) else Color(0xFFFF1744)
                ),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .testTag("admin_level_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ADMIN LEVEL SELECT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD600),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFFF2A7A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "DEV MODE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Play any level (1-10) directly without unlocking:",
                        fontSize = 11.sp,
                        color = Color(0xFF90A4AE),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick-selection Level Chips (1..5 and 6..10)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (lvl in 1..5) {
                                val isSelected = parsedLevel == lvl
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFFD600) else Color(0xFF16234D)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFFFD600) else Color(0x4400E5FF),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            adminLevelInput = lvl.toString()
                                            adminErrorMessage = null
                                            focusManager.clearFocus()
                                        }
                                        .testTag("admin_level_chip_$lvl"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L$lvl",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (lvl in 6..10) {
                                val isSelected = parsedLevel == lvl
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Color(0xFFFFD600) else Color(0xFF16234D)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFFFD600) else Color(0x4400E5FF),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            adminLevelInput = lvl.toString()
                                            adminErrorMessage = null
                                            focusManager.clearFocus()
                                        }
                                        .testTag("admin_level_chip_$lvl"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "L$lvl",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input & Launch Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = adminLevelInput,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                adminLevelInput = filtered
                                if (filtered.isNotEmpty()) {
                                    val num = filtered.toIntOrNull()
                                    if (num == null || num < 1 || num > LevelData.levels.size) {
                                        adminErrorMessage = "Enter level 1 to ${LevelData.levels.size}"
                                    } else {
                                        adminErrorMessage = null
                                    }
                                } else {
                                    adminErrorMessage = null
                                }
                            },
                            label = { Text("Level #", fontSize = 11.sp) },
                            placeholder = { Text("1-10", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFD600),
                                unfocusedBorderColor = Color(0x6600E5FF),
                                focusedLabelColor = Color(0xFFFFD600),
                                unfocusedLabelColor = Color(0xFF90A4AE)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_level_input")
                        )

                        Button(
                            onClick = {
                                val targetLevel = parsedLevel
                                if (targetLevel != null && targetLevel in 1..LevelData.levels.size) {
                                    onAdminSelectLevel(targetLevel)
                                } else {
                                    adminErrorMessage = "Invalid level! Defaulting to Level 1."
                                    onAdminSelectLevel(1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("admin_start_level_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Start Level",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "START",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Level Info Preview or Error Banner
                    if (adminErrorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33FF1744))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = adminErrorMessage!!,
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("admin_error_text")
                            )
                        }
                    } else if (selectedDef != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF16234D))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Lvl ${selectedDef.levelNumber}: ${selectedDef.name}",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedDef.baseBallSpeed.toInt()} px/s",
                                color = Color(0xFFFFD600),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HOW TO PLAY BUTTON
            TextButton(
                onClick = onHowToPlayClick,
                modifier = Modifier.testTag("how_to_play_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HOW TO PLAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Audio and Haptic Toggles
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10193E))
                        .border(1.dp, Color(0x4400E5FF), CircleShape)
                        .testTag("menu_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Sound Toggle",
                        tint = if (isSoundEnabled) Color(0xFF00E5FF) else Color(0xFF78909C)
                    )
                }

                IconButton(
                    onClick = onToggleHaptics,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10193E))
                        .border(1.dp, Color(0x4400E5FF), CircleShape)
                        .testTag("menu_haptics_toggle")
                ) {
                    Icon(
                        imageVector = if (isHapticsEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                        contentDescription = "Haptics Toggle",
                        tint = if (isHapticsEnabled) Color(0xFF00E5FF) else Color(0xFF78909C)
                    )
                }
            }
        }
    }
}
