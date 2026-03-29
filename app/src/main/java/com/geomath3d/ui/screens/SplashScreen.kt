package com.geomath3d.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geomath3d.ui.theme.Accent
import com.geomath3d.ui.theme.AccentGreen
import com.geomath3d.data.ShapeType
import com.geomath3d.ui.components.Shape3DCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    // 1) Logo scale-in
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    // 2) Subtitle fade
    val subtitleAlpha = remember { Animatable(0f) }
    // 3) Shapes stagger
    val shape1Alpha = remember { Animatable(0f) }
    val shape2Alpha = remember { Animatable(0f) }
    val shape3Alpha = remember { Animatable(0f) }
    // 4) Tagline
    val tagAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo pop-in
        launch {
            logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
        }
        logoAlpha.animateTo(1f, tween(300))

        delay(150)
        subtitleAlpha.animateTo(1f, tween(350))

        delay(100)
        launch { shape1Alpha.animateTo(1f, tween(400, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) }
        delay(100)
        launch { shape2Alpha.animateTo(1f, tween(400, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) }
        delay(100)
        launch { shape3Alpha.animateTo(1f, tween(400, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) }

        delay(300)
        tagAlpha.animateTo(1f, tween(400))

        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F14)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Three floating 3D shapes side by side
            Row(
                horizontalArrangement = Arrangement.spacedBy((-16).dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(120.dp),
            ) {
                Box(modifier = Modifier.size(80.dp).alpha(shape1Alpha.value)) {
                    Shape3DCanvas(
                        shapeType  = ShapeType.SPHERE,
                        primaryColor   = Accent,
                        secondaryColor = Color(0xFF3D35B0),
                        modifier   = Modifier.fillMaxSize(),
                    )
                }
                Box(modifier = Modifier.size(100.dp).alpha(shape2Alpha.value)) {
                    Shape3DCanvas(
                        shapeType  = ShapeType.CUBE,
                        primaryColor   = AccentGreen,
                        secondaryColor = Color(0xFF007A60),
                        modifier   = Modifier.fillMaxSize(),
                    )
                }
                Box(modifier = Modifier.size(80.dp).alpha(shape3Alpha.value)) {
                    Shape3DCanvas(
                        shapeType  = ShapeType.CONE,
                        primaryColor   = Color(0xFFFF9F43),
                        secondaryColor = Color(0xFF9A5500),
                        modifier   = Modifier.fillMaxSize(),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Logo
            Text(
                "GeoMath 3D",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
            )

            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                "Matematika — ko'rish orqali",
                fontSize = 15.sp,
                color = AccentGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(subtitleAlpha.value),
            )

            Spacer(Modifier.height(48.dp))

            // Tagline dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.alpha(tagAlpha.value),
            ) {
                repeat(3) { i ->
                    val dotColor = when (i) {
                        0 -> Accent
                        1 -> AccentGreen
                        else -> Color(0xFFFF9F43)
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(dotColor, androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
