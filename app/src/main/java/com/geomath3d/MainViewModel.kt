package com.geomath3d

import androidx.lifecycle.ViewModel
import com.geomath3d.data.ShapeCalculator
import com.geomath3d.data.ShapeResult
import com.geomath3d.data.ShapeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalcUiState(
    val selectedShape: ShapeType = ShapeType.SPHERE,
    val radius: Float = 5f,
    val height: Float = 10f,
    val result: ShapeResult = ShapeCalculator.calculate(ShapeType.SPHERE, 5f, 10f),
    val rotationAngle: Float = 0f,
)

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalcUiState())
    val state: StateFlow<CalcUiState> = _state.asStateFlow()

    fun selectShape(type: ShapeType) {
        _state.update { it.copy(selectedShape = type).recalculate() }
    }

    fun setRadius(v: Float) {
        _state.update { it.copy(radius = v).recalculate() }
    }

    fun setHeight(v: Float) {
        _state.update { it.copy(height = v).recalculate() }
    }

    fun setRotation(angle: Float) {
        _state.update { it.copy(rotationAngle = angle) }
    }

    private fun CalcUiState.recalculate() = copy(
        result = ShapeCalculator.calculate(selectedShape, radius, height)
    )
}
