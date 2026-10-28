package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaPrimaryLight
import com.example.ui.theme.FutaTextPrimary

data class ActionItem(
    val title: String,
    val icon: ImageVector,
    val badge: String? = null,
    val iconColor: Color,
    val bgColor: Color,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsGrid(
    onSendClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onBankClick: () -> Unit,
    onBillsClick: () -> Unit,
    onAgentsClick: () -> Unit,
    onTontineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        ActionItem(
            title = "Envoyer",
            icon = Icons.Default.Send,
            badge = "1% seulement",
            iconColor = Color.White,
            bgColor = FutaPrimary,
            testTag = "action_send",
            onClick = onSendClick
        ),
        ActionItem(
            title = "Payer Marchand",
            icon = Icons.Default.QrCodeScanner,
            badge = "0 FC",
            iconColor = Color.White,
            bgColor = Color(0xFF0088CC),
            testTag = "action_merchant",
            onClick = onScanQrClick
        ),
        ActionItem(
            title = "Banques & Télécoms",
            icon = Icons.Default.AccountBalance,
            badge = "Direct",
            iconColor = Color(0xFF9333EA),
            bgColor = Color(0xFFF3E8FF),
            testTag = "action_banks",
            onClick = onBankClick
        ),
        ActionItem(
            title = "Factures RDC",
            icon = Icons.Default.ReceiptLong,
            badge = "SNEL/Canal",
            iconColor = Color(0xFFEA580C),
            bgColor = Color(0xFFFFEDD5),
            testTag = "action_bills",
            onClick = onBillsClick
        ),
        ActionItem(
            title = "Agents & Retrait",
            icon = Icons.Default.LocationOn,
            badge = "Kinshasa",
            iconColor = FutaGreen,
            bgColor = Color(0xFFD1FAE5),
            testTag = "action_agents",
            onClick = onAgentsClick
        ),
        ActionItem(
            title = "Tontine Kitemo",
            icon = Icons.Default.Diversity3,
            badge = "Épargne",
            iconColor = Color(0xFFB45309),
            bgColor = Color(0xFFFEF3C7),
            testTag = "action_tontine",
            onClick = onTontineClick
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_grid"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Services Futa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FutaTextPrimary
                )
                Text(
                    text = "Sans frais cachés",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = FutaGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2 rows of 3 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..2) {
                    ActionTile(
                        item = actions[i],
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 3..5) {
                    ActionTile(
                        item = actions[i],
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionTile(
    item: ActionItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { item.onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag(item.testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            item.badge?.let { badgeText ->
                Surface(
                    color = if (badgeText.contains("1%") || badgeText == "0 FC") FutaGreen else FutaPrimary,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(top = 0.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = FutaTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}
