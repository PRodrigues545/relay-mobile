package ipca.api.relay.presentation.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

private val ConditionOptions = listOf(
    "NEW" to "New",
    "LIKE_NEW" to "Like new",
    "GOOD" to "Good",
    "FAIR" to "Fair condition",
    "POOR" to "Poor"
)

@Composable
fun CreateResourceScreen(
    navController: NavHostController,
    viewModel: CreateResourceViewModel,
    editingId: Int? = null
) {
    // Lifecycle-aware: stops collecting when the screen is backgrounded so
    // the underlying flow doesn't keep delivering values to a hidden screen.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val primary = Color(0xFF6666FF)

    // Seed VM when edit id arrives (idempotent — VM ignores repeat calls).
    LaunchedEffect(editingId) {
        if (editingId != null) viewModel.loadForEdit(editingId)
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.popBackStack()
        }
    }

    val isSell = uiState.type == "SELL"
    val selectedCategoryName = uiState.categories
        .firstOrNull { it.id == uiState.categoryId }
        ?.name
        ?: if (uiState.categoriesLoading) "Loading…" else "Select"

    val screenTitle = if (uiState.isEditMode) "Edit Listing" else "Publish Item"
    val submitLabel = if (uiState.isEditMode) "Save" else "Publish"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        // 🔝 HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Text(
                    text = screenTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.submit() },
                enabled = !uiState.isLoading && !uiState.categoriesLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(submitLabel, color = Color.White)
                }
            }
        }

        // 🔄 TABS — wired to VM
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(
                text = "Sell",
                isSelected = isSell,
                primaryColor = primary
            ) {
                viewModel.onTypeChange("SELL")
            }
            TabButton(
                text = "Borrow",
                isSelected = !isSell,
                primaryColor = primary
            ) {
                viewModel.onTypeChange("BORROW")
                viewModel.onPriceChange("0")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🖼️ IMAGE PICKER — preview newly-picked Uri, else existing image, else placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, primary, RoundedCornerShape(12.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageModel: Any? = uiState.imageUri ?: uiState.existingImageUrl
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, primary, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = primary)
                        Text("Select Image", color = primary)
                    }
                }
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp)
            }

            // 📌 TÍTULO
            Text("Title:", color = primary, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
            )

            // 📝 DESCRIÇÃO
            Text("Description:", color = primary, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
            )

            // 📂 CATEGORIA — backend-driven
            Text("Category:", color = primary, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    enabled = uiState.categories.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = uiState.categories.isNotEmpty()) {
                            categoryExpanded = true
                        }
                )

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.onCategoryChange(category.id)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // 🏷️ ESTADO (condition)
            Text("Condition:", color = primary, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedTextField(
                    value = ConditionOptions.find { it.first == uiState.condition }?.second ?: "Select",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )
                Box(modifier = Modifier
                    .matchParentSize()
                    .clickable { conditionExpanded = true })

                DropdownMenu(
                    expanded = conditionExpanded,
                    onDismissRequest = { conditionExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    ConditionOptions.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.onConditionChange(code)
                                conditionExpanded = false
                            }
                        )
                    }
                }
            }

            // 💰 PREÇO (only for SELL)
            if (isSell) {
                Text("Price:", color = primary, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = uiState.price,
                    onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) viewModel.onPriceChange(it) },
                    modifier = Modifier.width(150.dp),
                    shape = RoundedCornerShape(8.dp),
                    suffix = { Text("€") },
                    placeholder = { Text("0.00") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )
            } else {
                Text("Maximum Duration:", color = primary, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = uiState.borrowDuration,
                    onValueChange = { viewModel.onBorrowDurationChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: 7 dias") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )

                Text("Zones:", color = primary, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = uiState.zones,
                    onValueChange = { viewModel.onZonesChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ex: Centro, University") },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primary)
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .padding(4.dp)
            .background(
                if (isSelected) primaryColor else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, primaryColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else primaryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}