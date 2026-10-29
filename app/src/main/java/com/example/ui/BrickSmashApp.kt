package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.audio.SoundManager
import com.example.data.GamePreferences
import com.example.engine.BrickSmashEngine
import com.example.model.ScreenState
import com.example.ui.dialogs.HowToPlayDialog
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MainMenuScreen

@Composable
fun BrickSmashApp() {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val preferences = remember { GamePreferences(context) }
    val engine = remember { BrickSmashEngine(soundManager, preferences) }

    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_MENU) }
    var showHowToPlay by remember { mutableStateOf(false) }

    var isSoundEnabled by remember { mutableStateOf(preferences.isSoundEnabled) }
    var isHapticsEnabled by remember { mutableStateOf(preferences.isHapticsEnabled) }

    LaunchedEffect(isSoundEnabled, isHapticsEnabled) {
        soundManager.isSoundEnabled = isSoundEnabled
        soundManager.isHapticsEnabled = isHapticsEnabled
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060919))
    ) {
        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                ScreenState.MAIN_MENU -> {
                    MainMenuScreen(
                        bestScore = preferences.bestScore,
                        isSoundEnabled = isSoundEnabled,
                        onToggleSound = {
                            isSoundEnabled = !isSoundEnabled
                            preferences.isSoundEnabled = isSoundEnabled
                        },
                        isHapticsEnabled = isHapticsEnabled,
                        onToggleHaptics = {
                            isHapticsEnabled = !isHapticsEnabled
                            preferences.isHapticsEnabled = isHapticsEnabled
                        },
                        onPlayClick = {
                            engine.loadLevel(preferences.unlockedLevel, resetScore = true)
                            currentScreen = ScreenState.GAMEPLAY
                        },
                        onLevelSelectClick = {
                            currentScreen = ScreenState.LEVEL_SELECT
                        },
                        onHowToPlayClick = {
                            showHowToPlay = true
                        },
                        onAdminSelectLevel = { level ->
                            engine.loadLevel(level, resetScore = true)
                            currentScreen = ScreenState.GAMEPLAY
                        }
                    )
                }

                ScreenState.LEVEL_SELECT -> {
                    BackHandler {
                        currentScreen = ScreenState.MAIN_MENU
                    }
                    LevelSelectScreen(
                        preferences = preferences,
                        onSelectLevel = { selectedLevel ->
                            engine.loadLevel(selectedLevel, resetScore = true)
                            currentScreen = ScreenState.GAMEPLAY
                        },
                        onBack = {
                            currentScreen = ScreenState.MAIN_MENU
                        }
                    )
                }

                ScreenState.GAMEPLAY -> {
                    GameScreen(
                        engine = engine,
                        soundManager = soundManager,
                        preferences = preferences,
                        onExitToMainMenu = {
                            currentScreen = ScreenState.MAIN_MENU
                        }
                    )
                }

                ScreenState.HOW_TO_PLAY -> {
                    // Handled as dialog below
                }
            }
        }

        if (showHowToPlay) {
            HowToPlayDialog(onDismiss = { showHowToPlay = false })
        }
    }
}
