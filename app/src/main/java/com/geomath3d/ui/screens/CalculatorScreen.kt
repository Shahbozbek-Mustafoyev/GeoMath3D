package com.geomath3d.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geomath3d.CalcUiState
import com.geomath3d.data.ShapeType
import com.geomath3d.ui.components.Shape3DCanvas2
import com.geomath3d.ui.theme.Accent
import com.geomath3d.ui.theme.AccentGreen
import com.geomath3d.ui.theme.animateDoubleAsState
import com.geomath3d.ui.theme.staggerDelay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    state: CalcUiState,
    onSelectShape: (ShapeType) -> Unit,
    onRadius: (Float) -> Unit,
    onHeight: (Float) -> Unit,
) {
    val scroll = rememberScrollState()

    // ── Staggered entry animations ─────────────────────────────────────
    val cardCount = 4
    val cardAlphas   = remember { List(cardCount) { Animatable(0f) } }
    val cardOffsets  = remember { List(cardCount) { Animatable(32f) } }
    LaunchedEffect(Unit) {
        cardAlphas.forEachIndexed { i, anim ->
            launch {
                delay(staggerDelay(i, 80).toLong())
                launch { anim.animateTo(1f, tween(350, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) }
                cardOffsets[i].animateTo(0f, tween(380, easing = CubicBezierEasing(0.25f,1f,0.5f,1f)))
            }
        }
    }

    // ── Animated result values ─────────────────────────────────────────
    val animVolume = animateDoubleAsState(state.result.volume)
    val animSurface = animateDoubleAsState(state.result.surfaceArea)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Shape selector row ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlphas[0].value)
                .offset(y = cardOffsets[0].value.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShapeType.entries.forEach { shape ->
                ShapeChip(
                    shape    = shape,
                    selected = state.selectedShape == shape,
                    onClick  = { onSelectShape(shape) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── 3D shape card ──────────────────────────────────────────────
        Surface(
            color  = MaterialTheme.colorScheme.surface,
            shape  = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .alpha(cardAlphas[1].value)
                .offset(y = cardOffsets[1].value.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Animated shape name crossfade
                    AnimatedContent(
                        targetState = state.selectedShape.labelUz,
                        transitionSpec = {
                            fadeIn(tween(200)) + slideInVertically { -it / 2 } togetherWith
                            fadeOut(tween(150)) + slideOutVertically { it / 2 }
                        },
                        label = "shapeName",
                    ) { name ->
                        Text(name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                    }
                    // Formula crossfade
                    AnimatedContent(
                        targetState = state.result.formulaVolume,
                        transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                        label = "formula",
                    ) { formula ->
                        Text(formula,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = AccentGreen,
                                fontSize = 13.sp,
                            ))
                    }
                }
                Spacer(Modifier.height(8.dp))

                // 3D canvas with crossfade on shape change
                AnimatedContent(
                    targetState = state.selectedShape,
                    transitionSpec = {
                        fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.88f) togetherWith
                        fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 1.08f)
                    },
                    label = "3dShape",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                ) { shapeType ->
                    Shape3DCanvas2(
                        shapeType      = shapeType,
                        primaryColor   = Accent,
                        secondaryColor = Color(0xFF3D35B0),
                        modifier       = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    "Aylantirib ko'ring →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Sliders card ───────────────────────────────────────────────
        Surface(
            color  = MaterialTheme.colorScheme.surface,
            shape  = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .alpha(cardAlphas[2].value)
                .offset(y = cardOffsets[2].value.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val radiusLabel = if (state.selectedShape == ShapeType.CUBE) "Tomon (a)" else "Radius (r)"
                SliderRow(
                    label = radiusLabel,
                    value = state.radius,
                    range = 1f..20f,
                    color = Accent,
                    onValueChange = onRadius,
                )
                AnimatedVisibility(
                    visible = state.selectedShape != ShapeType.SPHERE && state.selectedShape != ShapeType.CUBE,
                    enter   = fadeIn(tween(250)) + expandVertically(tween(300, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))),
                    exit    = fadeOut(tween(200)) + shrinkVertically(tween(250)),
                ) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = .5.dp)
                        Spacer(Modifier.height(12.dp))
                        SliderRow(
                            label = "Balandlik (h)",
                            value = state.height,
                            range = 1f..30f,
                            color = AccentGreen,
                            onValueChange = onHeight,
                        )
                    }
                }
            }
        }

        // ── Result cards with animated counter numbers ─────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlphas[3].value)
                .offset(y = cardOffsets[3].value.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ResultCard(
                label   = "Hajm (V)",
                value   = "%.1f sm³".format(animVolume),
                color   = Accent,
                formula = state.result.formulaVolume,
                modifier = Modifier.weight(1f),
            )
            ResultCard(
                label   = "Yuza (S)",
                value   = "%.1f sm²".format(animSurface),
                color   = AccentGreen,
                formula = state.result.formulaSurface,
                modifier = Modifier.weight(1f),
            )
        }

        // ── Parameter detail ───────────────────────────────────────────
        AnimatedContent(
            targetState = state.selectedShape,
            transitionSpec = {
                fadeIn(tween(250)) + slideInHorizontally(tween(300)) { it / 4 } togetherWith
                fadeOut(tween(180)) + slideOutHorizontally(tween(220)) { -it / 4 }
            },
            label = "params",
        ) { shape ->
            val params = state.result.paramLabels
            Surface(
                color  = MaterialTheme.colorScheme.surface,
                shape  = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Parametrlar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(.5f))
                    Spacer(Modifier.height(8.dp))
                    params.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                            Text("%.1f sm".format(value),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeChip(shape: ShapeType, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    // Bouncy scale on selection
    val chipScale by animateFloatAsState(
        targetValue   = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "chipScale",
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) .15f else 0f,
        animationSpec = tween(200),
        label         = "chipBg",
    )
    val borderWidth by animateDpAsState(
        targetValue   = if (selected) 1.5.dp else 1.dp,
        animationSpec = tween(180),
        label         = "chipBorder",
    )
    val borderColor by animateColorAsState(
        targetValue   = if (selected) Accent else MaterialTheme.colorScheme.outline,
        animationSpec = tween(200),
        label         = "chipBorderColor",
    )

    Column(
        modifier = modifier
            .scale(chipScale)
            .clip(RoundedCornerShape(12.dp))
            .background(Accent.copy(bgAlpha))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(shape.emoji, fontSize = 20.sp)
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
            label = "chipLabel_${shape.name}",
        ) { isSel ->
            Text(
                shape.labelUz,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSel) Accent else MaterialTheme.colorScheme.onSurface.copy(.5f),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, color: Color, onValueChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.6f))
            Text("%.0f sm".format(value),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(.2f),
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ResultCard(label: String, value: String, color: Color, formula: String, modifier: Modifier) {
    Surface(
        color = color.copy(.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, color.copy(.25f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.55f))
            Spacer(Modifier.height(4.dp))
            Text(value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 18.sp,
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(formula,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = color.copy(.6f),
                    fontSize = 10.sp,
                )
            )
        }
    }
}
