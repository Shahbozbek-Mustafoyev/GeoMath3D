package com.geomath3d.ui.screens

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geomath3d.data.ShapeCalculator
import com.geomath3d.data.ShapeType
import com.geomath3d.ui.components.Shape3DCanvas2
import com.geomath3d.ui.theme.Accent
import com.geomath3d.ui.theme.AccentGreen

@Composable
fun ARPreviewScreen() {
    var selectedShape by remember { mutableStateOf(ShapeType.SPHERE) }
    val result = ShapeCalculator.calculate(selectedShape, 5f, 10f)

    Column(modifier = Modifier.fillMaxSize()) {
        // "Camera" viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0A1020))
        ) {
            // Grid overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(Color(0xFF0D2137).copy(.6f), Color(0xFF050A10))
                        )
                    )
            )
            // 3D Shape (centered, large)
            Shape3DCanvas2(
                shapeType = selectedShape,
                primaryColor = AccentGreen,
                secondaryColor = Color(0xFF007A60),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.75f)
                    .align(Alignment.Center),
            )
            // HUD — top
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = Color.Black.copy(.55f),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Text(
                        "  AR Ko'rinish  ",
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 5.dp)
                    )
                }
                Surface(
                    color = Color.Red.copy(.8f),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        BlinkingDot()
                        Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // HUD — bottom info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(.85f))
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(selectedShape.labelUz,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)
                Text(result.formulaVolume,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AccentGreen,
                    modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HudChip("V = %.1f sm³".format(result.volume))
                    HudChip("S = %.1f sm²".format(result.surfaceArea))
                }
            }
        }

        // Controls panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shape selector
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeType.entries.forEach { shape ->
                    val sel = shape == selectedShape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) Accent.copy(.15f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                if (sel) 1.5.dp else 1.dp,
                                if (sel) Accent else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedShape = shape }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(shape.emoji, fontSize = 18.sp)
                            Text(shape.labelUz,
                                fontSize = 9.sp,
                                color = if (sel) Accent else MaterialTheme.colorScheme.onSurface.copy(.5f))
                        }
                    }
                }
            }
            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(text = "Kamerani ochish")
                }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
                    Text("Saqlash")
                }
            }
        }
    }
}

@Composable
private fun HudChip(text: String) {
    Surface(
        color = Color.White.copy(.12f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun BlinkingDot() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "blink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(600),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ), label = "blinkAlpha"
    )
    Icon(Icons.Default.FiberManualRecord, contentDescription = null,
        tint = Color.White.copy(alpha), modifier = Modifier.size(8.dp))
}
