package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsCalculatorDialog(
    initialCurrency: Currency,
    onCalculateFee: (Double, Currency) -> Double,
    onCalculateClassicFee: (Double, Currency) -> Double,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }
    var inputAmountText by remember {
        mutableStateOf(if (initialCurrency == Currency.CDF) "100000" else "50")
    }

    val amountValue = inputAmountText.toDoubleOrNull() ?: 0.0
    val futaFee = onCalculateFee(amountValue, selectedCurrency)
    val classicFee = onCalculateClassicFee(amountValue, selectedCurrency)
    val savings = (classicFee - futaFee).coerceAtLeast(0.0)
    val savingsPercent = if (classicFee > 0) ((savings / classicFee) * 100).toInt() else 70

    val df = if (selectedCurrency == Currency.CDF) DecimalFormat("#,###") else DecimalFormat("#,##0.00")
    val suffix = if (selectedCurrency == Currency.CDF) " FC" else ""
    val prefix = if (selectedCurrency == Currency.USD) "$" else ""

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("savings_calculator_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Comparateur d'économies",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                    Text(
                        text = "Futa 1% vs Opérateurs traditionnels RDC",
                        fontSize = 12.sp,
                        color = FutaTextSecondary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_calculator_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currency toggle chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedCurrency == Currency.CDF,
                    onClick = {
                        selectedCurrency = Currency.CDF
                        inputAmountText = "100000"
                    },
                    label = { Text("Francs Congolais (CDF)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FutaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("calc_chip_cdf")
                )
                FilterChip(
                    selected = selectedCurrency == Currency.USD,
                    onClick = {
                        selectedCurrency = Currency.USD
                        inputAmountText = "50"
                    },
                    label = { Text("Dollar US (USD)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FutaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("calc_chip_usd")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = inputAmountText,
                onValueChange = { inputAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Montant à envoyer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(
                        text = if (selectedCurrency == Currency.CDF) "FC" else "USD",
                        fontWeight = FontWeight.Bold,
                        color = FutaPrimary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_amount_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FutaPrimary,
                    focusedLabelColor = FutaPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Comparison Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Futa Fee
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "FUTA (1%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$prefix${df.format(futaFee)}$suffix",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FutaPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Plafond à 5 000 FC max",
                            fontSize = 10.sp,
                            color = FutaTextSecondary
                        )
                    }
                }

                // Telecom Classic Fee
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Autres (~3.5%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE11D48)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$prefix${df.format(classicFee)}$suffix",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE11D48)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ Taxe de retrait",
                            fontSize = 10.sp,
                            color = FutaTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Big Savings highlight
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(FutaGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Économies",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Vous économisez :",
                            fontSize = 12.sp,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$prefix${df.format(savings)}$suffix (-$savingsPercent%)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FutaGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("understand_savings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
            ) {
                Text(
                    text = "Profiter des frais à 1%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
