package com.geomath3d.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geomath3d.data.LessonPlan
import com.geomath3d.data.sampleLessons
import com.geomath3d.ui.theme.Accent
import com.geomath3d.ui.theme.AccentGreen
import com.geomath3d.ui.theme.AccentOrange
import com.geomath3d.ui.theme.staggerDelay
import kotlinx.coroutines.launch

@Composable
fun TeacherScreen() {
    val scroll = rememberScrollState()

    // Stagger entry
    val itemCount = 4
    val alphas   = remember { List(itemCount) { Animatable(0f) } }
    val offsets  = remember { List(itemCount) { Animatable(28f) } }
    LaunchedEffect(Unit) {
        alphas.forEachIndexed { i, anim ->
            launch {
                kotlinx.coroutines.delay(staggerDelay(i, 90).toLong())
                launch { anim.animateTo(1f, tween(340, easing = CubicBezierEasing(0.25f,1f,0.5f,1f))) }
                offsets[i].animateTo(0f, tween(360, easing = CubicBezierEasing(0.25f,1f,0.5f,1f)))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats row
        Text("Sinf statistikasi", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(.5f),
            modifier = Modifier.alpha(alphas[0].value).offset(y = offsets[0].value.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .alpha(alphas[0].value)
                .offset(y = offsets[0].value.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard("28",  "O'quvchi",     Accent,       Modifier.weight(1f))
            StatCard("74%", "O'zlashtirish", AccentGreen, Modifier.weight(1f))
            StatCard("6",   "Dars o'tildi", AccentOrange, Modifier.weight(1f))
        }

        // Lesson plans
        Text("Dars rejalari", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(.5f),
            modifier = Modifier.alpha(alphas[1].value).offset(y = offsets[1].value.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .alpha(alphas[1].value)
                .offset(y = offsets[1].value.dp),
        ) {
            sampleLessons.forEachIndexed { i, lesson ->
                LessonCard(lesson = lesson, animDelay = i * 60)
            }
        }

        // QR share
        Text("Sinfga ulashish", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(.5f),
            modifier = Modifier.alpha(alphas[2].value).offset(y = offsets[2].value.dp))
        Box(modifier = Modifier
            .alpha(alphas[2].value)
            .offset(y = offsets[2].value.dp)) {
            QrShareCard()
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, color: Color, modifier: Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(Modifier.height(2.dp))
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(.5f))
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonPlan, animDelay: Int = 0) {
    // Animate progress bar on entry
    val animatedProgress by animateFloatAsState(
        targetValue   = lesson.progressPercent / 100f,
        animationSpec = tween(700, delayMillis = animDelay + 300, easing = CubicBezierEasing(0.25f,1f,0.5f,1f)),
        label         = "progress_${lesson.id}",
    )
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Accent.copy(.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(lesson.shapeType.emoji, fontSize = 20.sp)
            }
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(lesson.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text("${lesson.grade} · ${lesson.durationMin} daqiqa",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            }
            // Progress
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(60.dp)) {
                val pct = lesson.progressPercent
                val color = when {
                    pct >= 70 -> AccentGreen
                    pct >= 30 -> AccentOrange
                    else -> Accent
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = color,
                    trackColor = MaterialTheme.colorScheme.outline,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text("$pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.5f))
            }
        }
    }
}

@Composable
private fun QrShareCard() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // QR placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null,
                    modifier = Modifier.size(44.dp), tint = Color(0xFF1A1A2E))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Dars QR-kodi",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("O'quvchilar skanerlaydi va darsga bevosita kiradi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(.55f),
                    lineHeight = 18.sp)
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {},
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Ulashish", fontSize = 13.sp)
                }
            }
        }
    }
}
