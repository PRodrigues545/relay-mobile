package ipca.api.relay.presentation.recoverPassword.resetPassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ResetPasswordScreen(
    token: String,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = viewModel(factory = ResetPasswordViewModel.Factory),
    onNavigateToLogin: () -> Unit = {}
) {
    LaunchedEffect(token) {
        viewModel.setToken(token)
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ResetPasswordNavigationEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    val state = viewModel.uiState

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reset Password", fontSize = 24.sp)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            isError = state.error != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = viewModel::onConfirmNewPassword,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Reset Password")
        }
    }
}