package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.model.Currency
import com.example.ui.theme.FutaBackground
import com.example.ui.theme.FutaCyan
import com.example.ui.theme.FutaDarkCard
import com.example.ui.theme.FutaGold
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel

data class DemoMerchant(
    val name: String,
    val code: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantQrScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(FutaScreen.HOME)
    }

    val account by viewModel.account.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedCurrency by remember { mutableStateOf(uiState.activeCurrency) }
    var merchantName by remember { mutableStateOf("Kin Mart Supermarché - Gombe") }
    var merchantCode by remember { mutableStateOf("84201") }
    var amountText by remember { mutableStateOf("15000") }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val maxAvailable = if (selectedCurrency == Currency.CDF) account.cdfBalance else account.usdBalance
    val isValid = amountValue in 1.0..maxAvailable && merchantName.isNotBlank()

    val demoMerchants = listOf(
        DemoMerchant("Kin Mart Gombe", "84201", "Supermarché"),
        DemoMerchant("Peloustore Limete", "71039", "Alimentation"),
        DemoMerchant("Pharmacie de la Paix", "55102", "Santé"),
        DemoMerchant("Kiosque Mama Fifi Bandal", "31048", "Commerce"),
        DemoMerchant("Taxi Express Kin", "99214", "Transport")
    )

    // Animated scanner beam
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scannerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scannerOffset"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Scanner & Payer Marchand",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FutaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                        modifier = Modifier.testTag("back_button_merchant")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanner Viewport Box
            Card(
                modifier = Modifier
                    .size(230.dp)
                    .testTag("scanner_viewport"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FutaDarkCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Reticle border
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .border(2.dp, FutaCyan, RoundedCornerShape(14.dp))
                    )

                    // Moving laser scanning line
                    Box(
                        modifier = Modifier
                            .offset(y = (scannerOffset - 90).dp)
                            .fillMaxWidth(0.9f)
                            .height(2.dp)
                            .background(FutaCyan)
                    )

                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = FutaGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Paiement marchand 100% GRATUIT pour vous (0 FC de frais)",
                    color = FutaGreenDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Select Demo Merchants in Kinshasa
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Commerces partenaires détectés à proximité :",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FutaTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(demoMerchants) { merchant ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    merchantName = merchant.name
                                    merchantCode = merchant.code
                                },
                            color = if (merchantCode == merchant.code) FutaPrimary.copy(alpha = 0.12f) else Color.White,
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = FutaPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = merchant.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FutaTextPrimary
                                    )
                                    Text(
                                        text = "Code #${merchant.code}",
                                        fontSize = 9.sp,
                                        color = FutaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Currency chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCurrency == Currency.CDF,
                            onClick = {
                                selectedCurrency = Currency.CDF
                                amountText = "15000"
                            },
                            label = { Text("Francs (CDF)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FutaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedCurrency == Currency.USD,
                            onClick = {
                                selectedCurrency = Currency.USD
                                amountText = "5.50"
                            },
                            label = { Text("Dollars (USD)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FutaPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = { merchantName = it },
                        label = { Text("Nom du marchand / établissement") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_merchant_name"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = merchantCode,
                        onValueChange = { merchantCode = it },
                        label = { Text("Code Marchand (5 chiffres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_merchant_code"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Montant à régler") },
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
                            .testTag("input_merchant_amount"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.payMerchant(
                        merchantName = merchantName,
                        merchantCode = merchantCode,
                        amount = amountValue,
                        currency = selectedCurrency
                    )
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_merchant_payment_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
            ) {
                Text(
                    text = if (isValid) "Payer immédiatement (0 FC de frais)" else "Vérifier le montant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
