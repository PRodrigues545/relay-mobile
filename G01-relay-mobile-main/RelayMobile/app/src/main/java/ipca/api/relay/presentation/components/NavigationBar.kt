package ipca.api.relay.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import ipca.api.relay.navigation.*

@Composable
fun RelayNavigationBar(
    currentDestination: NavDestination?,
    onNavigate: (Any) -> Unit
) {
    val items = listOf(
        Triple("Home", HomeGraph, Icons.Default.Home),
        Triple("Requests", RequestsGraph, Icons.Default.Assignment),
        Triple("Publish", CreateGraph, Icons.Default.AddCircle),
        Triple("Messages", MessagesGraph, Icons.Default.Message),
        Triple("Profile", ProfileGraph, Icons.Default.Person)
    )

    val selectedColor = Color(0xFF6666FF)
    val borderColor = Color(0xFF2A2A5E)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f))
            .navigationBarsPadding() // 👈 isto resolve
            .height(80.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    )  {
        items.forEach { (label, graphRoute, icon) ->

            // Lógica infalível: verifica se o grafo está na hierarquia da rota atual
            val isSelected = currentDestination?.hierarchy?.any {
                it.route?.contains(graphRoute::class.simpleName ?: "") == true
            } == true

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(graphRoute) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (isSelected) selectedColor else Color.White,
                            shape = CircleShape
                        )
                        .border(1.dp, if (isSelected) selectedColor else borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) Color.White else borderColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isSelected) selectedColor else borderColor,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}