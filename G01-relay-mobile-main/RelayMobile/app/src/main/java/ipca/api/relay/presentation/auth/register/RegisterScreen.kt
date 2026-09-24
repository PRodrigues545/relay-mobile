package ipca.api.relay.presentation.auth.register


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ipca.api.relay.presentation.auth.register.steps.RegisterStepOne
import ipca.api.relay.presentation.auth.register.steps.RegisterStepTwo

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(factory = RegisterViewModel.Factory),
    onRegisterSuccess: () -> Unit = {}
) {
    val state = viewModel.uiState

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is RegisterNavigationEvent.NavigateToHome -> onRegisterSuccess()
            }
        }
    }

    when (state.currentStep) {
        RegisterStep.CREDENTIALS -> RegisterStepOne(
            state = state,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onVisibilityChange = viewModel::onPasswordVisibilityToggle,
            onUsernameChange = viewModel::onUsernameChange,
            onContinueClick = viewModel::onStepOneContinue,
            modifier = modifier
        )
        RegisterStep.PERSONAL_INFO -> RegisterStepTwo(
            state = state,
            onNameChange = viewModel::onNameChange,
            onUniversitySelected  = viewModel::onUniversitySelected ,
            onCourseSelected  = viewModel::onCourseSelected ,
            onAddressChange = viewModel::onAddressChange,
            onAddressSelected = viewModel::onAddressSelected,
            onRegisterClick = viewModel::onRegisterClick,
            onSkipClick = viewModel::onRegisterClick,
            onPreviousClick = viewModel::previousStep,
            imageUri = viewModel.imageUri,
            onImageSelected = viewModel::onImageSelected,
            modifier = modifier
        )
    }
}