package ipca.api.relay.presentation.profile

//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.Tune
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController

//@Composable
//fun HistoryScreen(navController: NavHostController) {
//    var filter by remember { mutableStateOf("Todos") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = 16.dp)
//    ) {
//        RelayTopBar(
//            title = "History",
//            onBackClick = { navController.popBackStack() }
//        )
//
//        Row(
//            modifier = Modifier
//                .width(180.dp)
//                .height(40.dp)
//                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
//                .padding(horizontal = 8.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.Tune,
//                contentDescription = null,
//                tint = RelayDark
//            )
//
//            Spacer(Modifier.width(8.dp))
//
//            Text(
//                text = filter,
//                color = Color.Gray,
//                modifier = Modifier.weight(1f)
//            )
//
//            Icon(
//                imageVector = Icons.Default.KeyboardArrowDown,
//                contentDescription = null,
//                tint = Color.Gray
//            )
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        LazyColumn {
//            items(mockHistoryItems) { item ->
//                HistoryItemCard(item)
//            }
//        }
//    }
//}