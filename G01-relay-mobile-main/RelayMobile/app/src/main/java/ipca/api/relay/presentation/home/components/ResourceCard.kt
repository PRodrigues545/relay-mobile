package ipca.api.relay.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ipca.api.relay.domain.model.Resource

// Card occupies a column of the grid (160.dp wide). Using a fixed 4:5 aspect
// for the image area gives us:
//   1) Predictable grid layout — no per-card height jitter that forces the
//      whole grid to reflow as images stream in.
//   2) Stable item heights, which means Compose can recycle item composables
//      cleanly (key = id in HomeScreen depends on this).
//   3) Crop instead of FillWidth — visually more consistent across mixed
//      portrait/landscape product photos.
private val CARD_ASPECT_RATIO = 4f / 5f
private val ResourceCardShape = RoundedCornerShape(8.dp)

private val LoanPriceColor = Color(0xFF6666FF)
private val ImageBackgroundColor = Color(0xFFD9D9D9)

@Composable
fun ResourceCard(
    resource: Resource,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(CARD_ASPECT_RATIO),
            shape = ResourceCardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ImageBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                if (resource.imageUrl != null) {
                    // Pass the URL directly as the model — Coil builds an
                    // ImageRequest internally with the local ImageLoader's
                    // sensible defaults (crossfade, memory cache, etc.).
                    // The previous version rebuilt a new ImageRequest object
                    // on every recomposition, which prevented Coil's cache
                    // from hitting cleanly and allocated needlessly.
                    AsyncImage(
                        model = resource.imageUrl,
                        contentDescription = resource.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No image",
                        tint = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = resource.title,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Loan label or formatted price line.
        when {
            resource.type == "BORROW" -> Text(
                text = "Loan",
                fontSize = 12.sp,
                color = LoanPriceColor,
                fontWeight = FontWeight.Medium
            )
            resource.price != null -> Text(
                text = formatPrice(resource.price),
                fontSize = 13.sp,
                color = LoanPriceColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatPrice(price: Double): String =
    "%.2f €".format(price).replace('.', ',')