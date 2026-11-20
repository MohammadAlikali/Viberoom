package androidx.compose.material.icons.outlined

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _chatBubbleOutline: ImageVector? = null
val Icons.Outlined.ChatBubbleOutline: ImageVector
    get() {
        _chatBubbleOutline?.let { return it }
        _chatBubbleOutline = materialIcon(name = "Outlined.ChatBubbleOutline") {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(20.0f, 16.0f)
                lineTo(6.0f, 16.0f)
                lineToRelative(-2.0f, 2.0f)
                lineTo(4.0f, 4.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(12.0f)
                close()
            }
        }
        return _chatBubbleOutline!!
    }

private var _explore: ImageVector? = null
val Icons.Outlined.Explore: ImageVector
    get() {
        _explore?.let { return it }
        _explore = materialIcon(name = "Outlined.Explore") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(6.5f, 17.5f)
                lineToRelative(7.51f, -3.49f)
                lineTo(17.5f, 6.5f)
                lineTo(9.99f, 9.99f)
                lineTo(6.5f, 17.5f)
                close()
                moveTo(12.0f, 10.9f)
                curveToRelative(0.61f, 0.0f, 1.1f, 0.49f, 1.1f, 1.1f)
                reflectiveCurveToRelative(-0.49f, 1.1f, -1.1f, 1.1f)
                reflectiveCurveToRelative(-1.1f, -0.49f, -1.1f, -1.1f)
                reflectiveCurveToRelative(0.49f, -1.1f, 1.1f, -1.1f)
                close()
            }
        }
        return _explore!!
    }

private var _micNone: ImageVector? = null
val Icons.Outlined.MicNone: ImageVector
    get() {
        _micNone?.let { return it }
        _micNone = materialIcon(name = "Outlined.MicNone") {
            materialPath {
                moveTo(12.0f, 14.0f)
                curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
                lineTo(15.0f, 5.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveTo(9.0f, 3.34f, 9.0f, 5.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                close()
                moveTo(11.0f, 5.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                verticalLineToRelative(6.0f)
                curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                lineTo(11.0f, 5.0f)
                close()
                moveTo(17.0f, 11.0f)
                curveToRelative(0.0f, 2.76f, -2.24f, 5.0f, -5.0f, 5.0f)
                reflectiveCurveToRelative(-5.0f, -2.24f, -5.0f, -5.0f)
                lineTo(5.0f, 11.0f)
                curveToRelative(0.0f, 3.53f, 2.61f, 6.43f, 6.0f, 6.92f)
                lineTo(11.0f, 21.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-3.08f)
                curveToRelative(3.39f, -0.49f, 6.0f, -3.39f, 6.0f, -6.92f)
                horizontalLineToRelative(-2.0f)
                close()
            }
        }
        return _micNone!!
    }

private var _personOutline: ImageVector? = null
val Icons.Outlined.PersonOutline: ImageVector
    get() {
        _personOutline?.let { return it }
        _personOutline = materialIcon(name = "Outlined.PersonOutline") {
            materialPath {
                moveTo(12.0f, 5.9f)
                curveToRelative(1.16f, 0.0f, 2.1f, 0.94f, 2.1f, 2.1f)
                reflectiveCurveToRelative(-0.94f, 2.1f, -2.1f, 2.1f)
                reflectiveCurveTo(9.9f, 9.16f, 9.9f, 8.0f)
                reflectiveCurveToRelative(0.94f, -2.1f, 2.1f, -2.1f)
                moveToRelative(0.0f, 9.0f)
                curveToRelative(2.97f, 0.0f, 6.1f, 1.46f, 6.1f, 2.1f)
                verticalLineToRelative(1.1f)
                lineTo(5.9f, 18.1f)
                lineTo(5.9f, 17.0f)
                curveToRelative(0.0f, -0.64f, 3.13f, -2.1f, 6.1f, -2.1f)
                moveTo(12.0f, 4.0f)
                curveTo(9.79f, 4.0f, 8.0f, 5.79f, 8.0f, 8.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                reflectiveCurveToRelative(-1.79f, -4.0f, -4.0f, -4.0f)
                close()
                moveTo(12.0f, 13.0f)
                curveToRelative(-2.67f, 0.0f, -8.0f, 1.34f, -8.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(16.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -2.66f, -5.33f, -4.0f, -8.0f, -4.0f)
                close()
            }
        }
        return _personOutline!!
    }
