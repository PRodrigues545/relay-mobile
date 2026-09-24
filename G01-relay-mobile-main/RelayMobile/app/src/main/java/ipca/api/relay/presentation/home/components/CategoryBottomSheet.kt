package ipca.api.relay.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.domain.category.CategoryProvider.iconForCategory
import ipca.api.relay.domain.model.RemoteCategory
import ipca.api.relay.presentation.home.ALL_CATEGORY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBottomSheet(
    categories: List<RemoteCategory>,
    selectedCategory: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val primary = Color(0xFF6666FF)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        contentColor = Color.Black
    ) {
        Text(
            text = "Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 12.dp)
        )

        // "Todos" stays at the top of the sheet too, so users can clear from
        // inside the sheet without first dismissing it.
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                CategoryRow(
                    name = "Todos",
                    icon = iconForCategory("All"),  // falls through to MoreHoriz
                    selected = selectedCategory == ALL_CATEGORY,
                    onClick = { onSelect(ALL_CATEGORY) },
                    primaryColor = primary
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
            items(categories) { category ->
                CategoryRow(
                    name = category.name,
                    icon = iconForCategory(category.name),
                    selected = selectedCategory == category.name,
                    onClick = { onSelect(category.name) },
                    primaryColor = primary
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CategoryRow(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = name,
                color = if (selected) primaryColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 16.sp
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = primaryColor.copy(alpha = 0.6f)
        )
    }
}