package androidx.compose.material.icons.automirrored.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.ImageVector

private var _chat: ImageVector? = null
val Icons.AutoMirrored.Filled.Chat: ImageVector
    get() {
        _chat?.let { return it }
        _chat = materialIcon(name = "AutoMirrored.Filled.Chat", autoMirror = true) {
            materialPath {
                moveTo(20.0f, 2.0f)
                lineTo(4.0f, 2.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                lineTo(2.0f, 22.0f)
                lineToRelative(4.0f, -4.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                lineTo(22.0f, 4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(6.0f, 9.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(2.0f)
                lineTo(6.0f, 11.0f)
                lineTo(6.0f, 9.0f)
                close()
                moveTo(14.0f, 14.0f)
                lineTo(6.0f, 14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(2.0f)
                close()
                moveTo(18.0f, 8.0f)
                lineTo(6.0f, 8.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return _chat!!
    }

private var _forward: ImageVector? = null
val Icons.AutoMirrored.Filled.Forward: ImageVector
    get() {
        _forward?.let { return it }
        _forward = materialIcon(name = "AutoMirrored.Filled.Forward", autoMirror = true) {
            materialPath {
                moveTo(12.0f, 8.0f)
                verticalLineTo(4.0f)
                lineToRelative(8.0f, 8.0f)
                lineToRelative(-8.0f, 8.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(8.0f)
                close()
            }
        }
        return _forward!!
    }

private var _logout: ImageVector? = null
val Icons.AutoMirrored.Filled.Logout: ImageVector
    get() {
        _logout?.let { return it }
        _logout = materialIcon(name = "AutoMirrored.Filled.Logout", autoMirror = true) {
            materialPath {
                moveTo(17.0f, 7.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(18.17f, 11.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.17f)
                lineToRelative(-2.58f, 2.58f)
                lineTo(17.0f, 17.0f)
                lineToRelative(5.0f, -5.0f)
                close()
                moveTo(4.0f, 5.0f)
                horizontalLineToRelative(8.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(4.0f)
                verticalLineTo(5.0f)
                close()
            }
        }
        return _logout!!
    }

private var _reply: ImageVector? = null
val Icons.AutoMirrored.Filled.Reply: ImageVector
    get() {
        _reply?.let { return it }
        _reply = materialIcon(name = "AutoMirrored.Filled.Reply", autoMirror = true) {
            materialPath {
                moveTo(10.0f, 9.0f)
                verticalLineTo(5.0f)
                lineToRelative(-7.0f, 7.0f)
                lineToRelative(7.0f, 7.0f)
                verticalLineToRelative(-4.1f)
                curveToRelative(5.0f, 0.0f, 8.5f, 1.6f, 11.0f, 5.1f)
                curveToRelative(-1.0f, -5.0f, -4.0f, -10.0f, -11.0f, -11.0f)
                close()
            }
        }
        return _reply!!
    }
