package ipca.api.relay.presentation.auth.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.auth.components.AuthLabeledInput
import ipca.api.relay.presentation.auth.components.PasswordInput
import ipca.api.relay.presentation.components.Logo
import ipca.api.relay.presentation.components.PrimaryButton
import ipca.api.relay.presentation.components.SecondaryButton

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    onRegisterClick: () -> Unit = {},
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is LoginNavigationEvent.NavigateToHome -> onLoginSuccess()
                is LoginNavigationEvent.NavigateToForgotPassword -> onForgotPasswordClick()
            }
        }
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onRegisterClick = onRegisterClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onVisibilityChange = viewModel::onPasswordVisibilityToggle,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onVisibilityChange: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo()

        Spacer(Modifier.height(32.dp))

        AuthLabeledInput(
            label = "Email",
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "Enter your email", //TODO: String Resources
            error = state.emailError
        )

        Spacer(Modifier.height(16.dp))

        PasswordInput(
            label = "Password",
            placeholder = "Enter your password",
            value = state.password,
            onValueChange = onPasswordChange,
            showPassword = state.showPassword,
            onToggleVisibility = onVisibilityChange,
            error = state.passwordError
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Forgot password?",
            fontSize = 12.sp,
            lineHeight = 12.sp,
            color = Color(0xFF52527A),
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.End)
                .clickable{ onForgotPasswordClick() }
        )

        // add this just before PrimaryButton
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        } else {
            Spacer(Modifier.height(24.dp))
        }

        PrimaryButton(
            onClick = onLoginClick,
            text = "Login", //TODO: String Resources
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "You don't have an account?", //TODO: String Resources
            fontSize = 12.sp,
            lineHeight = 12.sp,
            color = Color(0xFF52527A),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SecondaryButton(
            onClick = onRegisterClick,
            text = "Register", //TODO: String Resources
            modifier = Modifier
                .height(48.dp)
                .fillMaxWidth()
        )
    }
}