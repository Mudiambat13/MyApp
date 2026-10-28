package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.BillProvider
import com.example.data.model.Currency
import com.example.ui.theme.FutaBackground
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(FutaScreen.HOME)
    }

    val providers by viewModel.billProviders.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val account by viewModel.account.collectAsState()

    var activeProvider by remember { mutableStateOf<BillProvider?>(null) }
    var inputTargetNumber by remember { mutableStateOf("") }
    var inputAmountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf(uiState.activeCurrency) }

    val dfCdf = DecimalFormat("#,###")
    val dfUsd = DecimalFormat("#,##0.00")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Factures & Services RDC",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "0 FC de frais supplémentaires",
                            fontSize = 11.sp,
                            color = FutaGreenDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                        modifier = Modifier.testTag("back_button_bills")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = FutaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FutaBackground)
            )
        },
        containerColor = FutaBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Jetons SNEL CashPower instantanés",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "Recevez votre code de recharge d'électricité prépayée de 20 chiffres directement sur votre reçu.",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }

            items(providers, key = { it.id }) { provider ->
                val icon = when (provider.category) {
                    "Électricité" -> Icons.Default.Lightbulb
                    "Eau" -> Icons.Default.WaterDrop
                    "Télévision" -> Icons.Default.Tv
                    else -> Icons.Default.PhoneAndroid
                }

                val iconBg = when (provider.category) {
                    "Électricité" -> Color(0xFFFEF3C7)
                    "Eau" -> Color(0xFFE0F2FE)
                    "Télévision" -> Color(0xFFF3E8FF)
                    else -> Color(0xFFD1FAE5)
                }

                val iconTint = when (provider.category) {
                    "Électricité" -> Color(0xFFD97706)
                    "Eau" -> Color(0xFF0284C7)
                    "Télévision" -> Color(0xFF9333EA)
                    else -> FutaGreenDark
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            activeProvider = provider
                            inputTargetNumber = if (provider.id == "prov-snel") "01449382103" else ""
                            inputAmountText = if (selectedCurrency == Currency.CDF) {
                                provider.suggestedAmountsCdf.first().toInt().toString()
                            } else {
                                provider.suggestedAmountsUsd.first().toString()
                            }
                        }
                        .testTag("bill_provider_${provider.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = provider.name,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = provider.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FutaTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = FutaGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "0 FC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FutaGreenDark,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = provider.description,
                                fontSize = 11.sp,
                                color = FutaTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Bill Payment Sheet
    activeProvider?.let { provider ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val amountNum = inputAmountText.toDoubleOrNull() ?: 0.0
        val maxAvailable = if (selectedCurrency == Currency.CDF) account.cdfBalance else account.usdBalance
        val isFormValid = amountNum in 1.0..maxAvailable && inputTargetNumber.isNotBlank()

        ModalBottomSheet(
            onDismissRequest = { activeProvider = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .testTag("bill_payment_sheet")
            ) {
                Text(
                    text = "Régler : ${provider.name}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FutaTextPrimary
                )
                Text(
                    text = "Aucun frais de transaction facturé (Frais Futa : 0 FC)",
                    fontSize = 12.sp,
                    color = FutaGreenDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Currency chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedCurrency == Currency.CDF,
                        onClick = {
                            selectedCurrency = Currency.CDF
                            inputAmountText = provider.suggestedAmountsCdf.first().toInt().toString()
                        },
                        label = { Text("CDF (Francs)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FutaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedCurrency == Currency.USD,
                        onClick = {
                            selectedCurrency = Currency.USD
                            inputAmountText = provider.suggestedAmountsUsd.first().toString()
                        },
                        label = { Text("USD (Dollars)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FutaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputTargetNumber,
                    onValueChange = { inputTargetNumber = it },
                    label = { Text(provider.inputPrompt) },
                    placeholder = { Text(provider.inputPlaceholder) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bill_target_number"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Suggested amounts row
                Text(
                    text = "Montants suggérés :",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FutaTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val list = if (selectedCurrency == Currency.CDF) provider.suggestedAmountsCdf else provider.suggestedAmountsUsd
                    items(list) { amt ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    inputAmountText = if (selectedCurrency == Currency.CDF) amt.toInt().toString() else amt.toString()
                                },
                            color = if (inputAmountText == (if (selectedCurrency == Currency.CDF) amt.toInt().toString() else amt.toString())) FutaPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (selectedCurrency == Currency.CDF) "${dfCdf.format(amt)} FC" else "$${dfUsd.format(amt)}",
                                color = if (inputAmountText == (if (selectedCurrency == Currency.CDF) amt.toInt().toString() else amt.toString())) Color.White else FutaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputAmountText,
                    onValueChange = { inputAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Montant exact") },
                    trailingIcon = {
                        Text(
                            text = if (selectedCurrency == Currency.CDF) "FC" else "$",
                            fontWeight = FontWeight.Bold,
                            color = FutaPrimary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_bill_amount"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.payBill(
                            provider = provider,
                            targetNumber = inputTargetNumber,
                            amount = amountNum,
                            currency = selectedCurrency
                        )
                        activeProvider = null
                    },
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_bill_payment_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
                ) {
                    Text(
                        text = if (isFormValid) "Payer immédiatement (0 FC de frais)" else "Remplir tous les champs",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
