package ipca.api.relay.navigation

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import ipca.api.relay.presentation.auth.login.LoginScreen
import ipca.api.relay.presentation.auth.register.RegisterScreen
import ipca.api.relay.presentation.recoverPassword.forgotPassword.ForgotPasswordScreen
import ipca.api.relay.presentation.recoverPassword.resetPassword.ResetPasswordScreen
import ipca.api.relay.presentation.recoverPassword.validateToken.ValidateTokenScreen
import kotlinx.serialization.Serializable



fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
){
    navigation<AuthGraph>(startDestination = Login){
        composable<Login>{
            LoginScreen(
                onRegisterClick = { navController.navigate(Register) },
                onForgotPasswordClick = { navController.navigate(ForgotPassword) },
                onLoginSuccess = onLoginSuccess,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        composable<Register>{
            Scaffold{ innerPadding ->
                RegisterScreen(
                    onRegisterSuccess = onLoginSuccess,
                    modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp)
                )
            }
        }
        composable<ForgotPassword>{
            ForgotPasswordScreen(
                onNavigateToValidateToken = {
                    navController.navigate(ValidateToken)
                }
            )
        }
        composable<ValidateToken>{
            ValidateTokenScreen(
                onNavigateToResetPassword = { token ->
                    navController.navigate(ResetPassword(token = token))
                }
            )
        }
        composable<ResetPassword>{ backStackEntry ->
            val route = backStackEntry.toRoute<ResetPassword>()
            ResetPasswordScreen(
                token = route.token,
                onNavigateToLogin = {
                    navController.navigate(Login){
                        popUpTo<AuthGraph> {inclusive = true}
                    }
                }
            )
        }
    }
}