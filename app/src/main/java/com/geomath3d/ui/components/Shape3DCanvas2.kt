@file:JvmName("Shape3DCanvasKt")

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
fun Shape3DCanvas2(
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
                val steps = 72  // yuqori aniqlik

                // 3D nuqtalarni oldindan hisoblash (loyihalashdan oldin)
                val bottomRaw = (0..steps).map { i ->
                    val a = i * 2 * PI.toFloat() / steps
                    Triple(r * cos(a), h, r * sin(a))
                }
                val topRaw = (0..steps).map { i ->
                    val a = i * 2 * PI.toFloat() / steps
                    Triple(r * cos(a), -h, r * sin(a))
                }

                // Har bir segment uchun z-depth hisoblash (painter's algorithm)
                // Orqa tomonni avval, old tomonni keyin chizish
                val angleYMod = displayAngle % (2 * PI.toFloat())

                // Orqa yuzani chizish (z > 0 bo'lgan yarimdoira)
                val backSidePath = Path().apply {
                    val startI = steps / 2
                    val endI = steps
                    moveTo(pt(topRaw[startI].first, topRaw[startI].second, topRaw[startI].third).x,
                           pt(topRaw[startI].first, topRaw[startI].second, topRaw[startI].third).y)
                    for (i in startI..endI) {
                        val tp = pt(topRaw[i].first, topRaw[i].second, topRaw[i].third)
                        lineTo(tp.x, tp.y)
                    }
                    for (i in endI downTo startI) {
                        val bp = pt(bottomRaw[i].first, bottomRaw[i].second, bottomRaw[i].third)
                        lineTo(bp.x, bp.y)
                    }
                    close()
                }
                drawPath(backSidePath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.45f), secondaryColor.copy(.25f))
                ))

                // Pastki disk (bottom cap) — orqa qismini chizish
                val bottomPath = Path().apply {
                    val b0 = pt(bottomRaw[0].first, bottomRaw[0].second, bottomRaw[0].third)
                    moveTo(b0.x, b0.y)
                    bottomRaw.forEach { (x, y, z) ->
                        val bp = pt(x, y, z)
                        lineTo(bp.x, bp.y)
                    }
                    close()
                }
                drawPath(bottomPath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.6f), secondaryColor.copy(.35f))
                ))
                drawPath(Path().apply {
                    val b0 = pt(bottomRaw[0].first, bottomRaw[0].second, bottomRaw[0].third)
                    moveTo(b0.x, b0.y)
                    bottomRaw.forEach { (x, y, z) -> lineTo(pt(x, y, z).x, pt(x, y, z).y) }
                }, color = darkEdge, style = Stroke(1.5f))

                // Old yuzani chizish (z < 0 bo'lgan yarimdoira) — ustiga
                val frontSidePath = Path().apply {
                    val t0 = pt(topRaw[0].first, topRaw[0].second, topRaw[0].third)
                    moveTo(t0.x, t0.y)
                    for (i in 0..steps / 2) {
                        val tp = pt(topRaw[i].first, topRaw[i].second, topRaw[i].third)
                        lineTo(tp.x, tp.y)
                    }
                    for (i in steps / 2 downTo 0) {
                        val bp = pt(bottomRaw[i].first, bottomRaw[i].second, bottomRaw[i].third)
                        lineTo(bp.x, bp.y)
                    }
                    close()
                }
                drawPath(frontSidePath, fill)

                // Yuqori disk (top cap) — eng ustida
                val topPath = Path().apply {
                    val t0 = pt(topRaw[0].first, topRaw[0].second, topRaw[0].third)
                    moveTo(t0.x, t0.y)
                    topRaw.forEach { (x, y, z) ->
                        val tp = pt(x, y, z)
                        lineTo(tp.x, tp.y)
                    }
                    close()
                }
                drawPath(topPath, Brush.linearGradient(
                    listOf(primaryColor.copy(.92f), primaryColor.copy(.65f))
                ))
                // Top rim
                drawPath(Path().apply {
                    val t0 = pt(topRaw[0].first, topRaw[0].second, topRaw[0].third)
                    moveTo(t0.x, t0.y)
                    topRaw.forEach { (x, y, z) -> lineTo(pt(x, y, z).x, pt(x, y, z).y) }
                }, color = edgeColor, style = Stroke(2f))

                // Yon chiziqlar (silinder qirralari)
                val leftI  = steps / 4
                val rightI = steps * 3 / 4
                drawLine(edgeColor,
                    pt(topRaw[leftI].first,  topRaw[leftI].second,  topRaw[leftI].third),
                    pt(bottomRaw[leftI].first, bottomRaw[leftI].second, bottomRaw[leftI].third),
                    strokeWidth = 1.5f)
                drawLine(edgeColor,
                    pt(topRaw[rightI].first,  topRaw[rightI].second,  topRaw[rightI].third),
                    pt(bottomRaw[rightI].first, bottomRaw[rightI].second, bottomRaw[rightI].third),
                    strokeWidth = 1.5f)
            }

            ShapeType.CONE -> {
                val r = 1f; val h = 1.3f
                val steps = 72

                val apex = pt(0f, -h, 0f)
                val baseRaw = (0..steps).map { i ->
                    val a = i * 2 * PI.toFloat() / steps
                    Triple(r * cos(a), h, r * sin(a))
                }
                val basePts = baseRaw.map { (x, y, z) -> pt(x, y, z) }

                // 1) Orqa konus yuzasi
                val backConePath = Path().apply {
                    moveTo(apex.x, apex.y)
                    for (i in steps / 2..steps) lineTo(basePts[i].x, basePts[i].y)
                    close()
                }
                drawPath(backConePath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.35f), secondaryColor.copy(.2f))
                ))

                // 2) Asos disk (base cap)
                val baseCapPath = Path().apply {
                    moveTo(basePts[0].x, basePts[0].y)
                    basePts.forEach { lineTo(it.x, it.y) }
                    close()
                }
                drawPath(baseCapPath, Brush.linearGradient(
                    listOf(secondaryColor.copy(.65f), secondaryColor.copy(.4f))
                ))
                drawPath(Path().apply {
                    moveTo(basePts[0].x, basePts[0].y)
                    basePts.forEach { lineTo(it.x, it.y) }
                }, edgeColor, style = Stroke(1.5f))

                // 3) Old konus yuzasi — ustiga
                val frontConePath = Path().apply {
                    moveTo(apex.x, apex.y)
                    for (i in 0..steps / 2) lineTo(basePts[i].x, basePts[i].y)
                    close()
                }
                drawPath(frontConePath, fill)

                // Qirralar
                drawLine(edgeColor, apex, basePts[0],          strokeWidth = 2f)
                drawLine(edgeColor, apex, basePts[steps / 2],  strokeWidth = 2f)
                drawLine(edgeColor.copy(.5f), apex, basePts[steps / 4],      strokeWidth = 1f)
                drawLine(edgeColor.copy(.5f), apex, basePts[steps * 3 / 4],  strokeWidth = 1f)
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
