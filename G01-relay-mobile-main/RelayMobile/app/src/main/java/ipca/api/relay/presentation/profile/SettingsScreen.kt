package ipca.api.relay.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import ipca.api.relay.presentation.common.NotificationPermission
import ipca.api.relay.presentation.common.NotificationPermissionHandler

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ProfileNavigationEvent.NavigateToLogin -> onLogout()
            }
        }
    }

    // Sync the switch with real OS state on entry and whenever the user
    // comes back from system Settings (lifecycle ON_RESUME).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshNotificationPermission(
                    NotificationPermission.isGranted(context)
                )
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Drives the permission request when the user flips the switch ON.
    var requestPermissionTrigger by remember { mutableStateOf(false) }

    // Shown when the user flips the switch OFF — we can't revoke programmatically.
    var showDisableDialog by remember { mutableStateOf(false) }

    NotificationPermissionHandler(
        trigger = requestPermissionTrigger,
        onResult = { granted ->
            viewModel.refreshNotificationPermission(granted)
            requestPermissionTrigger = false
        }
    )

    if (showDisableDialog) {
        AlertDialog(
            onDismissRequest = { showDisableDialog = false },
            title = { Text("Turn off notifications") },
            text = {
                Text(
                    "To turn off notifications, you'll need to disable them in " +
                            "your phone's system settings."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDisableDialog = false
                    NotificationPermission.openAppNotificationSettings(context)
                }) { Text("Open Settings") }
            },
            dismissButton = {
                TextButton(onClick = { showDisableDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        RelayTopBar(
            title = "Settings",
            onBackClick = { navController.popBackStack() }
        )

        SettingsSwitchRow(
            label = "Notifications",
            checked = viewModel.notificationsEnabled,
            // Pre-Tiramisu it's permanently on; disable the switch.
            enabled = NotificationPermission.requiresRuntimePermission,
            onCheckedChange = { wantsOn ->
                if (wantsOn) {
                    // Trigger the rationale + system prompt flow.
                    requestPermissionTrigger = true
                } else {
                    // Can't revoke from code — send the user to system settings.
                    showDisableDialog = true
                }
            }
        )

        SettingsSwitchRow(
            label = "Show location on profile",
            checked = viewModel.showLocationInProfile,
            onCheckedChange = viewModel::updateShowLocation
        )

        SettingsTextRow("Log Out") { viewModel.logout() }
        SettingsTextRow("Delete my account") { /* TODO */ }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = RelayPurple,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RelayPurple
            )
        )
    }
}

@Composable
private fun SettingsTextRow(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = RelayPurple,
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 24.dp)
    )
}