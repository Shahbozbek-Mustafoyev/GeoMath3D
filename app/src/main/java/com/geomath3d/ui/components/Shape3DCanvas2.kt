package com.geomath3d.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import com.geomath3d.data.ShapeType
import kotlin.math.*

// ── 3D projection helpers ─────────────────────────────────────────────────
private fun project(x: Float, y: Float, z: Float, angleX: Float, angleY: Float): Offset {
    // Rotate around Y axis
    val cosY = cos(angleY); val sinY = sin(angleY)
    val x1 = x * cosY + z * sinY
    val z1 = -x * sinY + z * cosY
    // Rotate around X axis
    val cosX = cos(angleX); val sinX = sin(angleX)
    val y2 = y * cosX - z1 * sinX
    // Simple orthographic projection
    return Offset(x1, y2)
}

@Composable
fun Shape3DCanvas(
    shapeType: ShapeType,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    var angleY by remember { mutableFloatStateOf(0.4f) }
    var angleX by remember { mutableFloatStateOf(0.25f) }

    // Auto-rotation
    val infiniteTransition = rememberInfiniteTransition(label = "rot")
    val autoAngle by infiniteTransition.animateFloat(
        initialValue = angleY,
        targetValue = angleY + 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "autoRot"
    )
    var isDragging by remember { mutableStateOf(false) }
    var displayAngle by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(autoAngle, isDragging) {
        if (!isDragging) displayAngle = autoAngle
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd   = { isDragging = false; angleY = displayAngle },
                    onDrag = { change, drag ->
                        change.consume()
                        displayAngle += drag.x * 0.01f
                        angleX = (angleX - drag.y * 0.01f).coerceIn(-1f, 1f)
                    }
                )
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = minOf(size.width, size.height) * 0.35f

        fun Offset.toCanvas() = Offset(cx + x * scale, cy + y * scale)
        fun pt(x: Float, y: Float, z: Float) =
            project(x, y, z, angleX, displayAngle).toCanvas()

        val fill = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = .9f), secondaryColor.copy(alpha = .95f)),
            center = Offset(cx - scale * .2f, cy - scale * .2f),
            radius = scale * 1.5f,
        )
        val edgeColor = primaryColor.copy(alpha = .6f)
        val darkEdge  = secondaryColor.copy(alpha = .8f)

        when (shapeType) {

            ShapeType.SPHERE -> {
                // Draw sphere as filled circle + equator ellipse
                drawCircle(fill, radius = scale)
                // Highlight
                drawCircle(
                    color = Color.White.copy(alpha = .18f),
                    radius = scale * .4f,
                    center = Offset(cx - scale * .3f, cy - scale * .3f),
                )
                // Equator arc (rotated ellipse approximation)
                val cosA = cos(displayAngle)
                drawOval(
                    color = Color.White.copy(alpha = .3f),
                    topLeft = Offset(cx - scale, cy - scale * .25f * abs(cosA)),
                    size = Size(scale * 2f, scale * .5f * abs(cosA)),
                    style = Stroke(width = 1.5f)
                )
                // Meridian
                drawOval(
                    color = Color.White.copy(alpha = .2f),
                    topLeft = Offset(cx - scale * .25f * abs(cos(displayAngle + PI.toFloat() / 2)), cy - scale),
                    size = Size(scale * .5f * abs(cos(displayAngle + PI.toFloat() / 2)), scale * 2f),
                    style = Stroke(width = 1f)
                )
            }

            ShapeType.CYLINDER -> {
                val r = 1f; val h = 1.2f
                val steps = 120

                // Har bir nuqtaning 3D z-qiymatini hisoblash (aylanishdan keyin)
                // z > 0 => orqa, z < 0 => old
                data class RingPoint(val angle: Float, val projectedZ: Float, val top: Offset, val bot: Offset)

                val ring = (0..steps).map { i ->
                    val a = i * 2 * PI.toFloat() / steps
                    val wx = r * cos(a)
                    val wz = r * sin(a)
                    // Faqat Y o'qi atrofida aylantirilgandan keyin z ni hisoblash
                    val rotZ = -wx * sin(displayAngle) + wz * cos(displayAngle)
                    RingPoint(
                        angle = a,
                        projectedZ = rotZ,
                        top = pt(wx, -h, wz),
                        bot = pt(wx,  h, wz),
                    )
                }

                // z > 0 = orqa (kamera dan uzoq), z < 0 = old (kameraga yaqin)
                val backRing  = ring.filter { it.projectedZ >= 0 }
                val frontRing = ring.filter { it.projectedZ <  0 }

                // ── 1. Orqa yon yuz ──
                if (backRing.size >= 2) {
                    val path = Path().apply {
                        moveTo(backRing.first().top.x, backRing.first().top.y)
                        backRing.forEach { lineTo(it.top.x, it.top.y) }
                        backRing.reversed().forEach { lineTo(it.bot.x, it.bot.y) }
                        close()
                    }
                    drawPath(path, Brush.linearGradient(
                        listOf(secondaryColor.copy(.38f), secondaryColor.copy(.18f))
                    ))
                }

                // ── 2. Pastki disk ──
                val botPath = Path().apply {
                    moveTo(ring.first().bot.x, ring.first().bot.y)
                    ring.forEach { lineTo(it.bot.x, it.bot.y) }
                    close()
                }
                drawPath(botPath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.65f), secondaryColor.copy(.38f))
                ))
                drawPath(Path().apply {
                    moveTo(ring.first().bot.x, ring.first().bot.y)
                    ring.forEach { lineTo(it.bot.x, it.bot.y) }
                }, color = darkEdge, style = Stroke(1.5f))

                // ── 3. Old yon yuz ──
                if (frontRing.size >= 2) {
                    val path = Path().apply {
                        moveTo(frontRing.first().top.x, frontRing.first().top.y)
                        frontRing.forEach { lineTo(it.top.x, it.top.y) }
                        frontRing.reversed().forEach { lineTo(it.bot.x, it.bot.y) }
                        close()
                    }
                    drawPath(path, fill)
                }

                // ── 4. Yuqori disk (eng ustida) ──
                val topPath = Path().apply {
                    moveTo(ring.first().top.x, ring.first().top.y)
                    ring.forEach { lineTo(it.top.x, it.top.y) }
                    close()
                }
                drawPath(topPath, Brush.linearGradient(
                    listOf(primaryColor.copy(.95f), primaryColor.copy(.65f))
                ))
                // Top rim chiziq
                drawPath(Path().apply {
                    moveTo(ring.first().top.x, ring.first().top.y)
                    ring.forEach { lineTo(it.top.x, it.top.y) }
                }, color = edgeColor, style = Stroke(2f))

                // Silindirning yon qirra chiziqlari
                val leftPt  = ring.minByOrNull {
                    val p = project(r * cos(it.angle), 0f, r * sin(it.angle), angleX, displayAngle)
                    p.x
                }
                val rightPt = ring.maxByOrNull {
                    val p = project(r * cos(it.angle), 0f, r * sin(it.angle), angleX, displayAngle)
                    p.x
                }
                leftPt?.let  { drawLine(edgeColor, it.top, it.bot, strokeWidth = 1.5f) }
                rightPt?.let { drawLine(edgeColor, it.top, it.bot, strokeWidth = 1.5f) }
            }

            ShapeType.CONE -> {
                val r = 1f; val h = 1.3f
                val steps = 120

                val apex = pt(0f, -h, 0f)

                data class BasePoint(val angle: Float, val projectedZ: Float, val pos: Offset)

                val base = (0..steps).map { i ->
                    val a = i * 2 * PI.toFloat() / steps
                    val wx = r * cos(a)
                    val wz = r * sin(a)
                    val rotZ = -wx * sin(displayAngle) + wz * cos(displayAngle)
                    BasePoint(a, rotZ, pt(wx, h, wz))
                }

                val backBase  = base.filter { it.projectedZ >= 0 }
                val frontBase = base.filter { it.projectedZ <  0 }

                // ── 1. Orqa konus yuzasi ──
                if (backBase.size >= 2) {
                    val path = Path().apply {
                        moveTo(apex.x, apex.y)
                        backBase.forEach { lineTo(it.pos.x, it.pos.y) }
                        close()
                    }
                    drawPath(path, Brush.linearGradient(
                        listOf(secondaryColor.copy(.32f), secondaryColor.copy(.15f))
                    ))
                }

                // ── 2. Asos disk ──
                val baseCapPath = Path().apply {
                    moveTo(base.first().pos.x, base.first().pos.y)
                    base.forEach { lineTo(it.pos.x, it.pos.y) }
                    close()
                }
                drawPath(baseCapPath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.68f), secondaryColor.copy(.42f))
                ))
                drawPath(Path().apply {
                    moveTo(base.first().pos.x, base.first().pos.y)
                    base.forEach { lineTo(it.pos.x, it.pos.y) }
                }, color = edgeColor, style = Stroke(1.5f))

                // ── 3. Old konus yuzasi ──
                if (frontBase.size >= 2) {
                    val path = Path().apply {
                        moveTo(apex.x, apex.y)
                        frontBase.forEach { lineTo(it.pos.x, it.pos.y) }
                        close()
                    }
                    drawPath(path, fill)
                }

                // Qirralar — eng chap va eng o'ng nuqtalarni topish
                val leftPt  = base.minByOrNull {
                    project(r * cos(it.angle), h, r * sin(it.angle), angleX, displayAngle).x
                }
                val rightPt = base.maxByOrNull {
                    project(r * cos(it.angle), h, r * sin(it.angle), angleX, displayAngle).x
                }
                leftPt?.let  { drawLine(edgeColor, apex, it.pos, strokeWidth = 2f) }
                rightPt?.let { drawLine(edgeColor, apex, it.pos, strokeWidth = 2f) }
            }

            ShapeType.CUBE -> {
                val s = 0.9f
                val corners = listOf(
                    Triple(-s, -s, -s), Triple( s, -s, -s),
                    Triple( s,  s, -s), Triple(-s,  s, -s),
                    Triple(-s, -s,  s), Triple( s, -s,  s),
                    Triple( s,  s,  s), Triple(-s,  s,  s),
                ).map { (x, y, z) -> pt(x, y, z) }

                fun face(i: List<Int>, color: Color) {
                    val path = Path().apply {
                        moveTo(corners[i[0]].x, corners[i[0]].y)
                        i.forEach { lineTo(corners[it].x, corners[it].y)  }
                        close()
                    }
                    drawPath(path, color)
                    drawPath(path, edgeColor, style = Stroke(2f))
                }
                face(listOf(0,1,2,3), secondaryColor.copy(.7f))  // back
                face(listOf(4,5,6,7), primaryColor.copy(.9f))    // front
                face(listOf(1,5,6,2), primaryColor.copy(.75f))   // right
                face(listOf(0,4,7,3), secondaryColor.copy(.55f)) // left
                face(listOf(3,2,6,7), primaryColor.copy(.6f))    // top
                face(listOf(0,1,5,4), secondaryColor.copy(.5f))  // bottom
            }

            ShapeType.PYRAMID -> {
                val s = 0.9f; val h = 1.3f
                val apex  = pt(0f, -h, 0f)
                val base0 = pt(-s,  h, -s)
                val base1 = pt( s,  h, -s)
                val base2 = pt( s,  h,  s)
                val base3 = pt(-s,  h,  s)

                fun triPath(a: Offset, b: Offset, c: Offset) = Path().apply {
                    moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close()
                }
                drawPath(triPath(apex, base0, base1), Brush.linearGradient(listOf(primaryColor.copy(.9f), secondaryColor.copy(.7f))))
                drawPath(triPath(apex, base1, base2), Brush.linearGradient(listOf(primaryColor.copy(.75f), secondaryColor.copy(.55f))))
                drawPath(triPath(apex, base2, base3), Brush.linearGradient(listOf(primaryColor.copy(.65f), secondaryColor.copy(.45f))))
                drawPath(triPath(apex, base3, base0), Brush.linearGradient(listOf(primaryColor.copy(.55f), secondaryColor.copy(.4f))))
                // Base
                val basePath = Path().apply {
                    moveTo(base0.x, base0.y); lineTo(base1.x, base1.y)
                    lineTo(base2.x, base2.y); lineTo(base3.x, base3.y); close()
                }
                drawPath(basePath, secondaryColor.copy(.45f))
                // Edges
                listOf(base0 to base1, base1 to base2, base2 to base3, base3 to base0).forEach { (a, b) ->
                    drawLine(edgeColor, a, b, strokeWidth = 1.5f)
                }
                listOf(base0, base1, base2, base3).forEach {
                    drawLine(edgeColor, apex, it, strokeWidth = 1.5f)
                }
            }
        }
    }
}
