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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = transaction.type == TransactionType.TRANSFER_RECEIVED ||
            transaction.type == TransactionType.BANK_TOPUP

    val iconVector = when (transaction.type) {
        TransactionType.TRANSFER_SENT -> Icons.AutoMirrored.Filled.ArrowForward
        TransactionType.TRANSFER_RECEIVED -> Icons.AutoMirrored.Filled.ArrowBack
        TransactionType.BANK_TOPUP -> Icons.Default.AccountBalance
        TransactionType.BANK_WITHDRAWAL -> Icons.Default.AccountBalance
        TransactionType.BILL_PAYMENT -> Icons.Default.Receipt
        TransactionType.MERCHANT_PAY -> Icons.Default.ShoppingBag
        TransactionType.TONTINE_CONTRIBUTION -> Icons.Default.Diversity3
        TransactionType.CURRENCY_EXCHANGE -> Icons.Default.CurrencyExchange
    }

    val iconBgColor = when (transaction.type) {
        TransactionType.TRANSFER_RECEIVED,
        TransactionType.BANK_TOPUP -> Color(0xFFD1FAE5)
        TransactionType.TRANSFER_SENT -> Color(0xFFE0ECFF)
        TransactionType.BILL_PAYMENT -> Color(0xFFFFEDD5)
        TransactionType.MERCHANT_PAY -> Color(0xFFE0F2FE)
        TransactionType.TONTINE_CONTRIBUTION -> Color(0xFFFEF3C7)
        TransactionType.CURRENCY_EXCHANGE -> Color(0xFFF3E8FF)
        TransactionType.BANK_WITHDRAWAL -> Color(0xFFFFE4E6)
    }

    val iconTint = when (transaction.type) {
        TransactionType.TRANSFER_RECEIVED,
        TransactionType.BANK_TOPUP -> FutaGreenDark
        TransactionType.TRANSFER_SENT -> FutaPrimary
        TransactionType.BILL_PAYMENT -> Color(0xFFEA580C)
        TransactionType.MERCHANT_PAY -> Color(0xFF0284C7)
        TransactionType.TONTINE_CONTRIBUTION -> Color(0xFFD97706)
        TransactionType.CURRENCY_EXCHANGE -> Color(0xFF9333EA)
        TransactionType.BANK_WITHDRAWAL -> Color(0xFFE11D48)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .testTag("transaction_item_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = transaction.title,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Date/Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = FutaTextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = transaction.subtitle,
                    fontSize = 12.sp,
                    color = FutaTextSecondary,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount & Fee
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.formattedAmount(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) FutaGreenDark else FutaTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (transaction.fee > 0.0) "1% frais" else "Gratuit",
                fontSize = 11.sp,
                color = if (transaction.fee > 0.0) FutaPrimary else FutaGreenDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
