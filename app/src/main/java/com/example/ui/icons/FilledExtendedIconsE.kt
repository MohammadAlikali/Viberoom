package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _visibility: ImageVector? = null
val Icons.Filled.Visibility: ImageVector
    get() {
        _visibility?.let { return it }
        _visibility = materialIcon(name = "Filled.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
        return _visibility!!
    }

private var _volumeUp: ImageVector? = null
val Icons.Filled.VolumeUp: ImageVector
    get() {
        _volumeUp?.let { return it }
        _volumeUp = materialIcon(name = "Filled.VolumeUp") {
            materialPath {
                moveTo(3.0f, 9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(4.0f)
                lineToRelative(5.0f, 5.0f)
                lineTo(12.0f, 4.0f)
                lineTo(7.0f, 9.0f)
                lineTo(3.0f, 9.0f)
                close()
                moveTo(16.5f, 12.0f)
                curveToRelative(0.0f, -1.77f, -1.02f, -3.29f, -2.5f, -4.03f)
                verticalLineToRelative(8.05f)
                curveToRelative(1.48f, -0.73f, 2.5f, -2.25f, 2.5f, -4.02f)
                close()
                moveTo(14.0f, 3.23f)
                verticalLineToRelative(2.06f)
                curveToRelative(2.89f, 0.86f, 5.0f, 3.54f, 5.0f, 6.71f)
                reflectiveCurveToRelative(-2.11f, 5.85f, -5.0f, 6.71f)
                verticalLineToRelative(2.06f)
                curveToRelative(4.01f, -0.91f, 7.0f, -4.49f, 7.0f, -8.77f)
                reflectiveCurveToRelative(-2.99f, -7.86f, -7.0f, -8.77f)
                close()
            }
        }
        return _volumeUp!!
    }

private var _wavingHand: ImageVector? = null
val Icons.Filled.WavingHand: ImageVector
    get() {
        _wavingHand?.let { return it }
        _wavingHand = materialIcon(name = "Filled.WavingHand") {
            materialPath {
                moveTo(23.0f, 17.0f)
                curveToRelative(0.0f, 3.31f, -2.69f, 6.0f, -6.0f, 6.0f)
                verticalLineToRelative(-1.5f)
                curveToRelative(2.48f, 0.0f, 4.5f, -2.02f, 4.5f, -4.5f)
                horizontalLineTo(23.0f)
                close()
                moveTo(1.0f, 7.0f)
                curveToRelative(0.0f, -3.31f, 2.69f, -6.0f, 6.0f, -6.0f)
                verticalLineToRelative(1.5f)
                curveTo(4.52f, 2.5f, 2.5f, 4.52f, 2.5f, 7.0f)
                horizontalLineTo(1.0f)
                close()
                moveTo(8.01f, 4.32f)
                lineToRelative(-4.6f, 4.6f)
                curveToRelative(-3.22f, 3.22f, -3.22f, 8.45f, 0.0f, 11.67f)
                reflectiveCurveToRelative(8.45f, 3.22f, 11.67f, 0.0f)
                lineToRelative(7.07f, -7.07f)
                curveToRelative(0.49f, -0.49f, 0.49f, -1.28f, 0.0f, -1.77f)
                curveToRelative(-0.49f, -0.49f, -1.28f, -0.49f, -1.77f, 0.0f)
                lineToRelative(-4.42f, 4.42f)
                lineToRelative(-0.71f, -0.71f)
                lineToRelative(6.54f, -6.54f)
                curveToRelative(0.49f, -0.49f, 0.49f, -1.28f, 0.0f, -1.77f)
                reflectiveCurveToRelative(-1.28f, -0.49f, -1.77f, 0.0f)
                lineToRelative(-5.83f, 5.83f)
                lineToRelative(-0.71f, -0.71f)
                lineToRelative(6.89f, -6.89f)
                curveToRelative(0.49f, -0.49f, 0.49f, -1.28f, 0.0f, -1.77f)
                reflectiveCurveToRelative(-1.28f, -0.49f, -1.77f, 0.0f)
                lineToRelative(-6.89f, 6.89f)
                lineTo(11.02f, 9.8f)
                lineToRelative(5.48f, -5.48f)
                curveToRelative(0.49f, -0.49f, 0.49f, -1.28f, 0.0f, -1.77f)
                reflectiveCurveToRelative(-1.28f, -0.49f, -1.77f, 0.0f)
                lineToRelative(-7.62f, 7.62f)
                curveToRelative(1.22f, 1.57f, 1.11f, 3.84f, -0.33f, 5.28f)
                lineToRelative(-0.71f, -0.71f)
                curveToRelative(1.17f, -1.17f, 1.17f, -3.07f, 0.0f, -4.24f)
                lineToRelative(-0.35f, -0.35f)
                lineToRelative(4.07f, -4.07f)
                curveToRelative(0.49f, -0.49f, 0.49f, -1.28f, 0.0f, -1.77f)
                curveTo(9.29f, 3.83f, 8.5f, 3.83f, 8.01f, 4.32f)
                close()
            }
        }
        return _wavingHand!!
    }

private var _zoomIn: ImageVector? = null
val Icons.Filled.ZoomIn: ImageVector
    get() {
        _zoomIn?.let { return it }
        _zoomIn = materialIcon(name = "Filled.ZoomIn") {
            materialPath {
                moveTo(15.5f, 14.0f)
                horizontalLineToRelative(-0.79f)
                lineToRelative(-0.28f, -0.27f)
                curveTo(15.41f, 12.59f, 16.0f, 11.11f, 16.0f, 9.5f)
                curveTo(16.0f, 5.91f, 13.09f, 3.0f, 9.5f, 3.0f)
                reflectiveCurveTo(3.0f, 5.91f, 3.0f, 9.5f)
                reflectiveCurveTo(5.91f, 16.0f, 9.5f, 16.0f)
                curveToRelative(1.61f, 0.0f, 3.09f, -0.59f, 4.23f, -1.57f)
                lineToRelative(0.27f, 0.28f)
                verticalLineToRelative(0.79f)
                lineToRelative(5.0f, 4.99f)
                lineTo(20.49f, 19.0f)
                lineToRelative(-4.99f, -5.0f)
                close()
                moveTo(9.5f, 14.0f)
                curveTo(7.01f, 14.0f, 5.0f, 11.99f, 5.0f, 9.5f)
                reflectiveCurveTo(7.01f, 5.0f, 9.5f, 5.0f)
                reflectiveCurveTo(14.0f, 7.01f, 14.0f, 9.5f)
                reflectiveCurveTo(11.99f, 14.0f, 9.5f, 14.0f)
                close()
            }
            materialPath {
                moveTo(12.0f, 10.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(1.0f)
                close()
            }
        }
        return _zoomIn!!
    }
