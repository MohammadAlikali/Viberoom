package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _accessTime: ImageVector? = null
val Icons.Filled.AccessTime: ImageVector
    get() {
        _accessTime?.let { return it }
        _accessTime = materialIcon(name = "Filled.AccessTime") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
            materialPath {
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
        return _accessTime!!
    }

private var _autoAwesome: ImageVector? = null
val Icons.Filled.AutoAwesome: ImageVector
    get() {
        _autoAwesome?.let { return it }
        _autoAwesome = materialIcon(name = "Filled.AutoAwesome") {
            materialPath {
                moveTo(19.0f, 9.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 5.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 1.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 5.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 9.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.0f, 4.0f)
                lineTo(6.5f, 9.5f)
                lineTo(1.0f, 12.0f)
                lineToRelative(5.5f, 2.5f)
                lineTo(9.0f, 20.0f)
                lineToRelative(2.5f, -5.5f)
                lineTo(17.0f, 12.0f)
                lineToRelative(-5.5f, -2.5f)
                close()
                moveTo(19.0f, 15.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 19.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 23.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 19.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 15.0f)
                close()
            }
        }
        return _autoAwesome!!
    }

private var _block: ImageVector? = null
val Icons.Filled.Block: ImageVector
    get() {
        _block?.let { return it }
        _block = materialIcon(name = "Filled.Block") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(4.0f, 12.0f)
                curveToRelative(0.0f, -4.42f, 3.58f, -8.0f, 8.0f, -8.0f)
                curveToRelative(1.85f, 0.0f, 3.55f, 0.63f, 4.9f, 1.69f)
                lineTo(5.69f, 16.9f)
                curveTo(4.63f, 15.55f, 4.0f, 13.85f, 4.0f, 12.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-1.85f, 0.0f, -3.55f, -0.63f, -4.9f, -1.69f)
                lineTo(18.31f, 7.1f)
                curveTo(19.37f, 8.45f, 20.0f, 10.15f, 20.0f, 12.0f)
                curveToRelative(0.0f, 4.42f, -3.58f, 8.0f, -8.0f, 8.0f)
                close()
            }
        }
        return _block!!
    }

private var _bookmark: ImageVector? = null
val Icons.Filled.Bookmark: ImageVector
    get() {
        _bookmark?.let { return it }
        _bookmark = materialIcon(name = "Filled.Bookmark") {
            materialPath {
                moveTo(17.0f, 3.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(5.0f, 21.0f)
                lineToRelative(7.0f, -3.0f)
                lineToRelative(7.0f, 3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _bookmark!!
    }

private var _bookmarkAdd: ImageVector? = null
val Icons.Filled.BookmarkAdd: ImageVector
    get() {
        _bookmarkAdd?.let { return it }
        _bookmarkAdd = materialIcon(name = "Filled.BookmarkAdd") {
            materialPath {
                moveTo(21.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(19.0f, 21.0f)
                lineToRelative(-7.0f, -3.0f)
                lineToRelative(-7.0f, 3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                lineToRelative(7.0f, 0.0f)
                curveToRelative(-0.63f, 0.84f, -1.0f, 1.87f, -1.0f, 3.0f)
                curveToRelative(0.0f, 2.76f, 2.24f, 5.0f, 5.0f, 5.0f)
                curveToRelative(0.34f, 0.0f, 0.68f, -0.03f, 1.0f, -0.1f)
                verticalLineTo(21.0f)
                close()
            }
        }
        return _bookmarkAdd!!
    }

private var _bookmarkBorder: ImageVector? = null
val Icons.Filled.BookmarkBorder: ImageVector
    get() {
        _bookmarkBorder?.let { return it }
        _bookmarkBorder = materialIcon(name = "Filled.BookmarkBorder") {
            materialPath {
                moveTo(17.0f, 3.0f)
                lineTo(7.0f, 3.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(5.0f, 21.0f)
                lineToRelative(7.0f, -3.0f)
                lineToRelative(7.0f, 3.0f)
                lineTo(19.0f, 5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(17.0f, 18.0f)
                lineToRelative(-5.0f, -2.18f)
                lineTo(7.0f, 18.0f)
                lineTo(7.0f, 5.0f)
                horizontalLineToRelative(10.0f)
                verticalLineToRelative(13.0f)
                close()
            }
        }
        return _bookmarkBorder!!
    }

private var _brightnessAuto: ImageVector? = null
val Icons.Filled.BrightnessAuto: ImageVector
    get() {
        _brightnessAuto?.let { return it }
        _brightnessAuto = materialIcon(name = "Filled.BrightnessAuto") {
            materialPath {
                moveTo(10.85f, 12.65f)
                horizontalLineToRelative(2.3f)
                lineTo(12.0f, 9.0f)
                lineToRelative(-1.15f, 3.65f)
                close()
                moveTo(20.0f, 8.69f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-4.69f)
                lineTo(12.0f, 0.69f)
                lineTo(8.69f, 4.0f)
                horizontalLineTo(4.0f)
                verticalLineToRelative(4.69f)
                lineTo(0.69f, 12.0f)
                lineTo(4.0f, 15.31f)
                verticalLineTo(20.0f)
                horizontalLineToRelative(4.69f)
                lineTo(12.0f, 23.31f)
                lineTo(15.31f, 20.0f)
                horizontalLineTo(20.0f)
                verticalLineToRelative(-4.69f)
                lineTo(23.31f, 12.0f)
                lineTo(20.0f, 8.69f)
                close()
                moveTo(14.3f, 16.0f)
                lineToRelative(-0.7f, -2.0f)
                horizontalLineToRelative(-3.2f)
                lineToRelative(-0.7f, 2.0f)
                horizontalLineTo(7.8f)
                lineTo(11.0f, 7.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(3.2f, 9.0f)
                horizontalLineToRelative(-1.9f)
                close()
            }
        }
        return _brightnessAuto!!
    }

private var _callEnd: ImageVector? = null
val Icons.Filled.CallEnd: ImageVector
    get() {
        _callEnd?.let { return it }
        _callEnd = materialIcon(name = "Filled.CallEnd") {
            materialPath {
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.6f, 0.0f, -3.15f, 0.25f, -4.6f, 0.72f)
                verticalLineToRelative(3.1f)
                curveToRelative(0.0f, 0.39f, -0.23f, 0.74f, -0.56f, 0.9f)
                curveToRelative(-0.98f, 0.49f, -1.87f, 1.12f, -2.66f, 1.85f)
                curveToRelative(-0.18f, 0.18f, -0.43f, 0.28f, -0.7f, 0.28f)
                curveToRelative(-0.28f, 0.0f, -0.53f, -0.11f, -0.71f, -0.29f)
                lineTo(0.29f, 13.08f)
                curveToRelative(-0.18f, -0.17f, -0.29f, -0.42f, -0.29f, -0.7f)
                curveToRelative(0.0f, -0.28f, 0.11f, -0.53f, 0.29f, -0.71f)
                curveTo(3.34f, 8.78f, 7.46f, 7.0f, 12.0f, 7.0f)
                reflectiveCurveToRelative(8.66f, 1.78f, 11.71f, 4.67f)
                curveToRelative(0.18f, 0.18f, 0.29f, 0.43f, 0.29f, 0.71f)
                curveToRelative(0.0f, 0.28f, -0.11f, 0.53f, -0.29f, 0.71f)
                lineToRelative(-2.48f, 2.48f)
                curveToRelative(-0.18f, 0.18f, -0.43f, 0.29f, -0.71f, 0.29f)
                curveToRelative(-0.27f, 0.0f, -0.52f, -0.11f, -0.7f, -0.28f)
                curveToRelative(-0.79f, -0.74f, -1.69f, -1.36f, -2.67f, -1.85f)
                curveToRelative(-0.33f, -0.16f, -0.56f, -0.5f, -0.56f, -0.9f)
                verticalLineToRelative(-3.1f)
                curveTo(15.15f, 9.25f, 13.6f, 9.0f, 12.0f, 9.0f)
                close()
            }
        }
        return _callEnd!!
    }

private var _cameraAlt: ImageVector? = null
val Icons.Filled.CameraAlt: ImageVector
    get() {
        _cameraAlt?.let { return it }
        _cameraAlt = materialIcon(name = "Filled.CameraAlt") {
            materialPath {
                moveTo(12.0f, 12.0f)
                moveToRelative(-3.2f, 0.0f)
                arcToRelative(3.2f, 3.2f, 0.0f, true, true, 6.4f, 0.0f)
                arcToRelative(3.2f, 3.2f, 0.0f, true, true, -6.4f, 0.0f)
            }
            materialPath {
                moveTo(9.0f, 2.0f)
                lineTo(7.17f, 4.0f)
                lineTo(4.0f, 4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(12.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 6.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-3.17f)
                lineTo(15.0f, 2.0f)
                lineTo(9.0f, 2.0f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
            }
        }
        return _cameraAlt!!
    }

private var _chatBubble: ImageVector? = null
val Icons.Filled.ChatBubble: ImageVector
    get() {
        _chatBubble?.let { return it }
        _chatBubble = materialIcon(name = "Filled.ChatBubble") {
            materialPath {
                moveTo(20.0f, 2.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(18.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
            }
        }
        return _chatBubble!!
    }

private var _chatBubbleOutline: ImageVector? = null
val Icons.Filled.ChatBubbleOutline: ImageVector
    get() {
        _chatBubbleOutline?.let { return it }
        _chatBubbleOutline = materialIcon(name = "Filled.ChatBubbleOutline") {
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

private var _chevronRight: ImageVector? = null
val Icons.Filled.ChevronRight: ImageVector
    get() {
        _chevronRight?.let { return it }
        _chevronRight = materialIcon(name = "Filled.ChevronRight") {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        return _chevronRight!!
    }
