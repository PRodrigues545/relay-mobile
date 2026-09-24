package ipca.api.relay.presentation.auth.register.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.presentation.auth.components.AuthLabeledInput
import ipca.api.relay.presentation.auth.components.PasswordInput
import ipca.api.relay.presentation.auth.register.RegisterUiState
import ipca.api.relay.presentation.components.Logo
import ipca.api.relay.presentation.components.PrimaryButton

@Composable
fun RegisterStepOne(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onVisibilityChange: () -> Unit,
    onUsernameChange: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Logo(modifier = Modifier.size(128.dp))
        Spacer(Modifier.height(32.dp))

        AuthLabeledInput(
            label = "Username* (You won't be able to change this later)",
            value = state.username,
            onValueChange = onUsernameChange,
            placeholder = "Enter your username",
            error = state.usernameError
        )

        Spacer(Modifier.height(16.dp))

        AuthLabeledInput(
            label = "Email*",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "Enter your email",
            error = state.emailError
        )

        Spacer(Modifier.height(16.dp))

        PasswordInput(
            label = "Password*",
            placeholder = "Enter your password",
            value = state.password,
            onValueChange = onPasswordChange,
            showPassword = state.showPassword,
            onToggleVisibility = onVisibilityChange,
            error = state.passwordError
        )

        Spacer(Modifier.height(16.dp))

        PasswordInput(
            label = "Confirm Password*",
            placeholder = "Repeat your password",
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            showPassword = state.showPassword,
            onToggleVisibility = onVisibilityChange,
            error = state.confirmPasswordError
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "* Required fields",
            fontSize = 12.sp,
            color = Color(0xFF9494D1),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            onClick = onContinueClick,
            text = "Continue",
            modifier = Modifier.height(48.dp).fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}