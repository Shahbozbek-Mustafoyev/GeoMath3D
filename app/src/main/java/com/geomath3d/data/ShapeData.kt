package com.geomath3d.data

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

// ── Shape types ───────────────────────────────────────────────────────────
enum class ShapeType(val labelUz: String, val emoji: String) {
    SPHERE   ("Shar",     "🔵"),
    CYLINDER ("Silindr",  "🥫"),
    CONE     ("Konus",    "🍦"),
    CUBE     ("Kub",      "🟦"),
    PYRAMID  ("Piramida", "🔺"),
}

// ── Calculation result ────────────────────────────────────────────────────
data class ShapeResult(
    val volume: Double,
    val surfaceArea: Double,
    val formulaVolume: String,
    val formulaSurface: String,
    val paramLabels: List<Pair<String, Double>>,  // label → value
)

// ── Calculator ─────────────────────────────────────────────────────────────
object ShapeCalculator {

    fun calculate(type: ShapeType, r: Float, h: Float): ShapeResult {
        val rd = r.toDouble()
        val hd = h.toDouble()
        return when (type) {
            ShapeType.SPHERE -> {
                val V = (4.0 / 3.0) * PI * rd.pow(3)
                val S = 4.0 * PI * rd.pow(2)
                ShapeResult(
                    volume = V, surfaceArea = S,
                    formulaVolume  = "V = 4/3 · π · r³",
                    formulaSurface = "S = 4 · π · r²",
                    paramLabels = listOf("r (radius)" to rd),
                )
            }
            ShapeType.CYLINDER -> {
                val V = PI * rd.pow(2) * hd
                val S = 2 * PI * rd * (rd + hd)
                ShapeResult(
                    volume = V, surfaceArea = S,
                    formulaVolume  = "V = π · r² · h",
                    formulaSurface = "S = 2π · r · (r + h)",
                    paramLabels = listOf("r (radius)" to rd, "h (balandlik)" to hd),
                )
            }
            ShapeType.CONE -> {
                val l = sqrt(rd.pow(2) + hd.pow(2))
                val V = PI * rd.pow(2) * hd / 3.0
                val S = PI * rd * (rd + l)
                ShapeResult(
                    volume = V, surfaceArea = S,
                    formulaVolume  = "V = π · r² · h / 3",
                    formulaSurface = "S = π · r · (r + l)",
                    paramLabels = listOf("r (radius)" to rd, "h (balandlik)" to hd),
                )
            }
            ShapeType.CUBE -> {
                val V = rd.pow(3)
                val S = 6.0 * rd.pow(2)
                ShapeResult(
                    volume = V, surfaceArea = S,
                    formulaVolume  = "V = a³",
                    formulaSurface = "S = 6 · a²",
                    paramLabels = listOf("a (tomon)" to rd),
                )
            }
            ShapeType.PYRAMID -> {
                val slantH = sqrt((rd / 2).pow(2) + hd.pow(2))
                val V = rd.pow(2) * hd / 3.0
                val S = rd.pow(2) + 2 * rd * slantH
                ShapeResult(
                    volume = V, surfaceArea = S,
                    formulaVolume  = "V = a² · h / 3",
                    formulaSurface = "S = a² + 2a · l",
                    paramLabels = listOf("a (asos tomoni)" to rd, "h (balandlik)" to hd),
                )
            }
        }
    }
}

// ── Lesson plan model ─────────────────────────────────────────────────────
data class LessonPlan(
    val id: Int,
    val title: String,
    val grade: String,
    val shapeType: ShapeType,
    val progressPercent: Int,
    val durationMin: Int = 45,
)

val sampleLessons = listOf(
    LessonPlan(1, "Shar va uning xossalari",     "5-sinf", ShapeType.SPHERE,   90),
    LessonPlan(2, "Silindr — hajm hisoblash",     "5-sinf", ShapeType.CYLINDER, 60),
    LessonPlan(3, "Piramida va konus",            "6-sinf", ShapeType.PYRAMID,  20),
    LessonPlan(4, "Kub va kuboid",                "6-sinf", ShapeType.CUBE,      5),
)
