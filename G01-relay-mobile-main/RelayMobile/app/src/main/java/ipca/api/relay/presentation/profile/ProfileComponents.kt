package ipca.api.relay.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ipca.api.relay.presentation.components.Logo

val RelayPurple = Color(0xFF6666FF)
val RelayDark = Color(0xFF2A2A5E)
val RelayText = Color(0xFF52527A)

@Composable
fun RelayTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )
            Spacer(Modifier.width(16.dp))
        }

        Text(
            text = title,
            color = Color.Black,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )

        Logo(modifier = Modifier.size(56.dp))
    }
}

@Composable
fun AvatarPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, RelayDark, CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = RelayDark,
            modifier = Modifier.size(42.dp)
        )
    }
}

@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(5) { index ->
            Text(
                text = if (index < rating) "★" else "☆",
                color = RelayDark,
                fontSize = 26.sp
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = RelayPurple,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = value,
            color = RelayDark,
            fontSize = 16.sp,
            modifier = Modifier.weight(1.3f)
        )
    }
}

@Composable
fun ProfileOptionRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = RelayPurple,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(30.dp)
                .border(2.dp, RelayPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = RelayPurple
            )
        }
    }
}

@Composable
fun ReviewItem(review: ReviewUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        AvatarPlaceholder(modifier = Modifier.size(64.dp))

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = review.name,
                color = RelayPurple,
                fontSize = 16.sp
            )

            RatingStars(rating = review.rating)

            review.comment?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, RelayText, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = it,
                        color = RelayDark,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: HistoryItemUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 104.dp, height = 104.dp)
                .border(2.dp, RelayDark, RoundedCornerShape(6.dp))
                .background(Color(0xFFEDEDFD)),
            contentAlignment = Alignment.BottomEnd
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = RelayDark,
                modifier = Modifier.align(Alignment.Center)
            )

            Text(
                text = item.price,
                color = RelayDark,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(2.dp)
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                color = RelayPurple,
                fontSize = 16.sp
            )

            Text(
                text = item.description,
                color = RelayDark,
                fontSize = 14.sp,
                lineHeight = 15.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.type) {
                        HistoryType.PURCHASE -> Icons.Default.Wallet
                        HistoryType.SALE -> Icons.Default.Wallet
                        HistoryType.EXCHANGE -> Icons.Default.SwapVert
                    },
                    contentDescription = null,
                    tint = RelayDark,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = item.person,
                    color = RelayDark,
                    fontSize = 14.sp
                )
            }
        }
    }
}