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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.WalletAccount
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeDialog(
    account: WalletAccount,
    onExchange: (Double, Currency) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isFromCdf by remember { mutableStateOf(true) }
    var inputAmountText by remember { mutableStateOf("57000") }

    val rate = account.exchangeRateUsdToCdf
    val amountNum = inputAmountText.toDoubleOrNull() ?: 0.0

    val convertedAmount = if (isFromCdf) {
        amountNum / rate
    } else {
        amountNum * rate
    }

    val dfCdf = DecimalFormat("#,###")
    val dfUsd = DecimalFormat("#,##0.00")

    val maxAvailable = if (isFromCdf) account.cdfBalance else account.usdBalance
    val isEnough = amountNum in 1.0..maxAvailable

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
                .testTag("currency_exchange_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Conversion Instantanée Futa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                    Text(
                        text = "Taux officiel BCC : 1 USD = ${dfCdf.format(rate)} FC • 0% Commission",
                        fontSize = 12.sp,
                        color = FutaGreen
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Source Currency Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isFromCdf) "Vous vendez (CDF)" else "Vous vendez (USD)",
                            fontSize = 12.sp,
                            color = FutaTextSecondary
                        )
                        Text(
                            text = "Disponible : ${if (isFromCdf) account.formattedCdf() else account.formattedUsd()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FutaPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputAmountText,
                        onValueChange = { inputAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                text = if (isFromCdf) "FC" else "$",
                                fontWeight = FontWeight.Bold,
                                color = FutaPrimary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FutaPrimary,
                            focusedLabelColor = FutaPrimary
                        )
                    )
                }
            }

            // Swap direction button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FutaPrimary)
                        .clickable {
                            isFromCdf = !isFromCdf
                            inputAmountText = if (isFromCdf) "57000" else "20"
                        }
                        .testTag("swap_direction_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Inverser",
                        tint = Color.White
                    )
                }
            }

            // Target Currency Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isFromCdf) "Vous recevez (USD)" else "Vous recevez (CDF)",
                        fontSize = 12.sp,
                        color = FutaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isFromCdf) "$${dfUsd.format(convertedAmount)}" else "${dfCdf.format(convertedAmount)} FC",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onExchange(amountNum, if (isFromCdf) Currency.CDF else Currency.USD)
                },
                enabled = isEnough,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_exchange_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
            ) {
                Text(
                    text = if (isEnough) "Convertir sans frais" else "Solde insuffisant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
