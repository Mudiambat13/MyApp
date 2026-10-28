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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricAuthManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Currency
import com.example.ui.theme.FutaBackground
import com.example.ui.theme.FutaBorder
import com.example.ui.theme.FutaGreen
import com.example.ui.theme.FutaGreenDark
import com.example.ui.theme.FutaPrimary
import com.example.ui.theme.FutaTextPrimary
import com.example.ui.theme.FutaTextSecondary
import com.example.ui.viewmodel.FutaScreen
import com.example.ui.viewmodel.FutaViewModel
import java.text.DecimalFormat

data class QuickContact(
    val name: String,
    val phone: String,
    val initials: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(FutaScreen.HOME)
    }

    val account by viewModel.account.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var selectedCurrency by remember { mutableStateOf(uiState.activeCurrency) }
    var recipientName by remember { mutableStateOf("") }
    var recipientPhone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0
    val fee = viewModel.calculateTransferFee(amountValue, selectedCurrency)
    val classicFee = viewModel.calculateClassicFee(amountValue, selectedCurrency)
    val savings = (classicFee - fee).coerceAtLeast(0.0)

    val totalDeduction = amountValue + fee
    val availableBalance = if (selectedCurrency == Currency.CDF) account.cdfBalance else account.usdBalance
    val isAmountValid = amountValue > 0 && totalDeduction <= availableBalance && recipientPhone.isNotBlank()

    val df = if (selectedCurrency == Currency.CDF) DecimalFormat("#,###") else DecimalFormat("#,##0.00")
    val suffix = if (selectedCurrency == Currency.CDF) " FC" else ""
    val prefix = if (selectedCurrency == Currency.USD) "$" else ""

    val frequentContacts = listOf(
        QuickContact("Grace Mwamba", "+243 81 294 8831", "GM"),
        QuickContact("Papa Jean", "+243 82 550 1934", "PJ"),
        QuickContact("Dieudonné Kabeya", "+243 89 104 2209", "DK"),
        QuickContact("Sarah Ilunga", "+243 84 991 3820", "SI"),
        QuickContact("Mama Fifi Kiosque", "+243 82 331 0948", "MF")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Envoyer de l'argent",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FutaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                        modifier = Modifier.testTag("back_button_send")
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Currency Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCurrency == Currency.CDF,
                    onClick = { selectedCurrency = Currency.CDF },
                    label = { Text("Francs Congolais (CDF)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FutaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("send_chip_cdf")
                )
                FilterChip(
                    selected = selectedCurrency == Currency.USD,
                    onClick = { selectedCurrency = Currency.USD },
                    label = { Text("Dollars US (USD)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FutaPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("send_chip_usd")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frequent Contacts Row
            Text(
                text = "Contacts fréquents en RDC",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FutaTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(frequentContacts) { contact ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                recipientName = contact.name
                                recipientPhone = contact.phone
                            }
                            .testTag("contact_${contact.initials}"),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FutaPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.initials,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FutaPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = contact.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FutaTextPrimary
                                )
                                Text(
                                    text = contact.phone,
                                    fontSize = 10.sp,
                                    color = FutaTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recipient Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Destinataire",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Numéro de téléphone (+243)") },
                        placeholder = { Text("+243 82 000 0000") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = FutaPrimary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipient_phone"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FutaPrimary,
                            focusedLabelColor = FutaPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Nom du destinataire") },
                        placeholder = { Text("ex: Dieudonné Kabeya") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = FutaPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_recipient_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FutaPrimary,
                            focusedLabelColor = FutaPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Montant à transférer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "Solde : ${if (selectedCurrency == Currency.CDF) account.formattedCdf() else account.formattedUsd()}",
                            fontSize = 12.sp,
                            color = FutaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Montant") },
                        trailingIcon = {
                            Text(
                                text = if (selectedCurrency == Currency.CDF) "FC" else "USD",
                                fontWeight = FontWeight.Bold,
                                color = FutaPrimary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_send_amount"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FutaPrimary,
                            focusedLabelColor = FutaPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Motif (facultatif)") },
                        placeholder = { Text("ex: Remboursement, Cadeau...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_send_note"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FutaPrimary,
                            focusedLabelColor = FutaPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transparent 1% Fee Breakdown
            if (amountValue > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Frais Futa (1% seulement)",
                                fontSize = 13.sp,
                                color = FutaTextSecondary
                            )
                            Text(
                                text = "$prefix${df.format(fee)}$suffix",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FutaPrimary
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFDCFCE7))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total débité",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FutaTextPrimary
                            )
                            Text(
                                text = "$prefix${df.format(totalDeduction)}$suffix",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FutaPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Savings comparison badge
                        Surface(
                            color = FutaGreenDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Vous économisez $prefix${df.format(savings)}$suffix par rapport aux réseaux télécoms classiques !",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Spacer(modifier = Modifier.height(20.dp))

            val context = LocalContext.current
            val activity = context as? FragmentActivity

            Button(
                onClick = {
                    if (account.isBiometricEnabled && activity != null && BiometricAuthManager.isBiometricSupported(context)) {
                        BiometricAuthManager.authenticate(
                            activity = activity,
                            title = "Sécurité FutaPay - Transfert",
                            subtitle = "Confirmez l'envoi de $prefix${df.format(amountValue)}$suffix à ${recipientName.ifBlank { recipientPhone }}",
                            negativeButtonText = "Utiliser mon code PIN",
                            onSuccess = {
                                val name = recipientName.ifBlank { "Contact Futa" }
                                viewModel.sendMoney(
                                    recipientName = name,
                                    recipientPhone = recipientPhone,
                                    amount = amountValue,
                                    currency = selectedCurrency,
                                    notes = noteText.ifBlank { null }
                                )
                            },
                            onNegativeButtonOrFallback = {
                                showPinDialog = true
                            },
                            onError = { _, _ ->
                                showPinDialog = true
                            }
                        )
                    } else {
                        showPinDialog = true
                    }
                },
                enabled = isAmountValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_send_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (account.isBiometricEnabled) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isAmountValid) "Confirmer le transfert" else if (amountValue > availableBalance) "Solde insuffisant" else "Remplir tous les champs",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 4-Digit Security PIN Dialog (Wave style safety confirmation with biometric option)
    if (showPinDialog) {
        val context = LocalContext.current
        val activity = context as? FragmentActivity

        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                pinInput = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Code PIN",
                    tint = FutaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Valider par code PIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Entrez votre code secret à 4 chiffres pour confirmer l'envoi de $prefix${df.format(amountValue)}$suffix à ${recipientName.ifBlank { recipientPhone }}.",
                        fontSize = 13.sp,
                        color = FutaTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { ch -> ch.isDigit() }) {
                                pinInput = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        placeholder = { Text("••••") },
                        modifier = Modifier.testTag("send_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (account.isBiometricEnabled && activity != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                showPinDialog = false
                                BiometricAuthManager.authenticate(
                                    activity = activity,
                                    title = "Sécurité FutaPay - Transfert",
                                    subtitle = "Confirmez l'envoi de $prefix${df.format(amountValue)}$suffix",
                                    negativeButtonText = "Annuler",
                                    onSuccess = {
                                        val name = recipientName.ifBlank { "Contact Futa" }
                                        viewModel.sendMoney(
                                            recipientName = name,
                                            recipientPhone = recipientPhone,
                                            amount = amountValue,
                                            currency = selectedCurrency,
                                            notes = noteText.ifBlank { null }
                                        )
                                    },
                                    onNegativeButtonOrFallback = {
                                        showPinDialog = true
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Ou valider par Empreinte / Visage", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = recipientName.ifBlank { "Contact Futa" }
                        viewModel.sendMoney(
                            recipientName = name,
                            recipientPhone = recipientPhone,
                            amount = amountValue,
                            currency = selectedCurrency,
                            notes = noteText.ifBlank { null }
                        )
                        showPinDialog = false
                        pinInput = ""
                    },
                    enabled = pinInput.length == 4,
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary),
                    modifier = Modifier.testTag("confirm_pin_button")
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPinDialog = false
                        pinInput = ""
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}
