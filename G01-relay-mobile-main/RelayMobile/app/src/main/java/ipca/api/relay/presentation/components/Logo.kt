package ipca.api.relay.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import ipca.api.relay.R

@Composable
fun Logo(
    modifier: Modifier = Modifier,
){
    Icon(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Logo", //TODO: String Resources
        modifier = modifier,
    )
}