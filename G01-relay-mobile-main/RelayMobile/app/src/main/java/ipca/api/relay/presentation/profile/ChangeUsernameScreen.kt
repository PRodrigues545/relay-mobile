package ipca.api.relay.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ipca.api.relay.presentation.components.Logo
import ipca.api.relay.presentation.components.PrimaryButton

@Composable
fun ChangeUsernameScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel
) {
    var username by remember {
        mutableStateOf(viewModel.user.username)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp) // 👈 evita ficar colado às bordas
    ) {
        Spacer(Modifier.height(64.dp))

        Logo(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(96.dp))

        Text(
            text = "New Username",
            color = RelayText
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = {
                Text(
                    text = "Enter your new username",
                    fontStyle = FontStyle.Italic
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),

            // 👇 AQUI está a correção da cor
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = RelayDark,
                unfocusedTextColor = RelayDark,
                focusedBorderColor = RelayPurple,
                unfocusedBorderColor = RelayDark,
                cursorColor = RelayPurple,
                focusedPlaceholderColor = Color(0xFF9494D1),
                unfocusedPlaceholderColor = Color(0xFF9494D1)
            )
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text = "Confirm",
            onClick = {
                viewModel.updateUsername(username)
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(Modifier.height(32.dp))
    }
}