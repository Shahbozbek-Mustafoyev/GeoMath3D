package com.geomath3d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geomath3d.ui.screens.ARPreviewScreen
import com.geomath3d.ui.screens.CalculatorScreen
import com.geomath3d.ui.screens.SplashScreen
import com.geomath3d.ui.screens.TeacherScreen
import com.geomath3d.ui.theme.GeoMath3DTheme

enum class Screen(
    val route: String,
    val labelUz: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Calculator ("calc",    "Kalkulyator", Icons.Filled.Calculate,  Icons.Outlined.Calculate),
    Teacher ("teacher", "Panel",       Icons.Filled.School,     Icons.Outlined.School),
    AR ("ar",      "AR Rejim",    Icons.Filled.CameraAlt,  Icons.Outlined.CameraAlt);
    companion object { val all = listOf(Calculator, Teacher, AR) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GeoMath3DTheme { GeoMathApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoMathApp() {
    val vm: MainViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    // ── Splash ─────────────────────────────────────────────────────────
    var showSplash by remember { mutableStateOf(true) }
    AnimatedVisibility(
        visible = showSplash,
        exit = fadeOut(tween(400)) + scaleOut(tween(400), targetScale = 1.05f),
    ) {
        SplashScreen(onFinished = { showSplash = false })
    }

    AnimatedVisibility(
        visible = !showSplash,
        enter = fadeIn(tween(350, delayMillis = 50)),
    ) {
        MainScaffold(vm = vm, state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(vm: MainViewModel, state: CalcUiState) {
    var currentScreen by remember { mutableStateOf(Screen.Calculator) }
    var previousScreen by remember { mutableStateOf(Screen.Calculator) }

    // Direction: left→right or right→left based on tab index
    val currentIdx  = Screen.all.indexOf(currentScreen)
    val previousIdx = Screen.all.indexOf(previousScreen)
    val goingRight  = currentIdx > previousIdx

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Animated title crossfade on tab change
                    AnimatedContent(
                        targetState = currentScreen.labelUz,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        },
                        label = "topBarTitle",
                    ) { label ->
                        Text(
                            when (label) {
                                "Kalkulyator" -> "GeoMath 3D"
                                else -> label
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                Screen.all.forEach { screen ->
                    val selected = currentScreen == screen
                    // Animated scale on tab press
                    val iconScale by animateFloatAsState(
                        targetValue   = if (selected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness    = Spring.StiffnessMedium,
                        ),
                        label = "navScale_${screen.route}",
                    )
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            previousScreen = currentScreen
                            currentScreen  = screen
                        },
                        icon  = {
                            Icon(
                                if (selected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.labelUz,
                                modifier = Modifier.scale(iconScale),
                            )
                        },
                        label  = { Text(screen.labelUz, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor    = MaterialTheme.colorScheme.primary.copy(.15f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ── Animated screen transitions ────────────────────────────
            val slideOffset = 48
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val enterSlide  = if (goingRight) slideOffset else -slideOffset
                    val exitSlide   = if (goingRight) -slideOffset else slideOffset
                    (fadeIn(tween(250)) + slideIn(tween(300, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) { IntOffset(enterSlide, 0) }) togetherWith
                    (fadeOut(tween(180)) + slideOut(tween(220)) { IntOffset(exitSlide, 0) })
                },
                label = "screenContent",
                modifier = Modifier.fillMaxSize(),
            ) { screen ->
                when (screen) {
                    Screen.Calculator -> CalculatorScreen(
                        state         = state,
                        onSelectShape = vm::selectShape,
                        onRadius      = vm::setRadius,
                        onHeight      = vm::setHeight,
                    )
                    Screen.Teacher -> TeacherScreen()
                    Screen.AR      -> ARPreviewScreen()
                }
            }
        }
    }
}
