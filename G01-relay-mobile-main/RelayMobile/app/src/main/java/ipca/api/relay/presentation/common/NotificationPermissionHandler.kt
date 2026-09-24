package ipca.api.relay.presentation.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Requests POST_NOTIFICATIONS on Android 13+ with a short rationale first.
 * No-op on Android 12 and below (permission granted at install).
 *
 * @param trigger flip to true to start the flow (e.g. after a successful order
 *                or when the user toggles notifications on in settings).
 * @param onResult called once with the final state (granted / denied / not needed).
 */
@Composable
fun NotificationPermissionHandler(
    trigger: Boolean,
    onResult: (granted: Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }
    var consumed by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onResult(granted)
    }

    LaunchedEffect(trigger) {
        if (!trigger || consumed) return@LaunchedEffect

        // Pre-Tiramisu: permission is granted at install.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            consumed = true
            onResult(true)
            return@LaunchedEffect
        }

        val alreadyGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (alreadyGranted) {
            consumed = true
            onResult(true)
        } else {
            // Show rationale dialog first, then request.
            consumed = true
            showRationale = true
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                onResult(false)
            },
            title = { Text("Stay updated on your orders") },
            text = {
                Text(
                    "Relay uses notifications to let you know when your delivery " +
                            "is on the way, has arrived at the meeting point, or needs " +
                            "your attention. You can change this any time in Settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onResult(false)
                }) { Text("Not now") }
            }
        )
    }
}