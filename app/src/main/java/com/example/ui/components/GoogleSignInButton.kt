package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthService
import com.google.firebase.auth.FirebaseUser

@Composable
fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    label: String = "Continue with Google",
    testTag: String = "google_signin_button",
    onSuccess: (FirebaseUser) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember { AuthService(context) }
    var isLoading by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = {
            val activity = context as? Activity
            if (activity == null) {
                onError("Activity context not available")
                return@OutlinedButton
            }
            isLoading = true
            authService.signInWithGoogle(
                activity = activity,
                scope = coroutineScope,
                onSuccess = { user ->
                    isLoading = false
                    onSuccess(user)
                },
                onError = { err ->
                    isLoading = false
                    onError(err)
                },
                onCancelled = {
                    isLoading = false
                }
            )
        },
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Connecting to Google...",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            } else {
                GoogleGIcon(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

/**
 * High-fidelity Vector Google 'G' Icon drawn mathematically
 */
@Composable
fun GoogleGIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val center = Offset(size / 2f, size / 2f)
        val radius = size / 2f
        val innerRadius = radius * 0.55f

        // Google 4-color arcs
        // Red top
        val redPath = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(0f, 0f, size, size),
                startAngleDegrees = 200f,
                sweepAngleDegrees = 115f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path = redPath, color = Color(0xFFEA4335), style = Fill)

        // Yellow left
        val yellowPath = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(0f, 0f, size, size),
                startAngleDegrees = 135f,
                sweepAngleDegrees = 65f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path = yellowPath, color = Color(0xFFFBBC05), style = Fill)

        // Green bottom
        val greenPath = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(0f, 0f, size, size),
                startAngleDegrees = 45f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path = greenPath, color = Color(0xFF34A853), style = Fill)

        // Blue right
        val bluePath = Path().apply {
            moveTo(center.x, center.y)
            arcTo(
                rect = Rect(0f, 0f, size, size),
                startAngleDegrees = -45f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(path = bluePath, color = Color(0xFF4285F4), style = Fill)

        // Inner circle cutout (creates ring)
        drawCircle(
            color = Color.White,
            radius = innerRadius,
            center = center
        )

        // Horizontal blue bar of the G
        val barThickness = radius * 0.38f
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x, center.y - barThickness / 2f),
            size = androidx.compose.ui.geometry.Size(radius, barThickness)
        )
    }
}
