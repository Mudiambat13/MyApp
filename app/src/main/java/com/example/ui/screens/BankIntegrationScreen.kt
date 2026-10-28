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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Currency
import com.example.data.model.LinkedAccount
import com.example.data.model.LinkedCategory
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
fun BankIntegrationScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(FutaScreen.HOME)
    }

    val linkedAccounts by viewModel.linkedAccounts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val account by viewModel.account.collectAsState()

    var activeActionAccount by remember { mutableStateOf<LinkedAccount?>(null) }
    var actionType by remember { mutableStateOf("DEPOSIT") } // "DEPOSIT" or "WITHDRAW"
    var actionAmountText by remember { mutableStateOf("25000") }
    var actionCurrency by remember { mutableStateOf(uiState.activeCurrency) }

    // Link new account dialog state
    var showLinkDialog by remember { mutableStateOf(false) }
    var newProviderName by remember { mutableStateOf("Rawbank (Illicocash)") }
    var newCategory by remember { mutableStateOf(LinkedCategory.BANK) }
    var newAccountNumber by remember { mutableStateOf("") }
    var newHolderName by remember { mutableStateOf(account.userName) }

    val dfCdf = DecimalFormat("#,###")
    val dfUsd = DecimalFormat("#,##0.00")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Passerelle Bancaire & Télécom",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "Interopérabilité 100% Congolaise",
                            fontSize = 11.sp,
                            color = FutaGreenDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                        modifier = Modifier.testTag("back_button_banks")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = FutaTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLinkDialog = true },
                        modifier = Modifier.testTag("link_new_account_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Lier un compte",
                            tint = FutaPrimary
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Explanation Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FutaPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Dépôts bancaires gratuits • 0 FC",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FutaPrimary
                            )
                            Text(
                                text = "Transférez vos fonds entre vos comptes Rawbank, Equity, TMB, M-Pesa ou Orange Money instantanément sans file d'attente.",
                                fontSize = 11.sp,
                                color = FutaTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Linked Accounts Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mes Banques & Comptes Liés",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                    Text(
                        text = "${linkedAccounts.size} connectés",
                        fontSize = 12.sp,
                        color = FutaGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // List of linked accounts
            items(linkedAccounts, key = { it.id }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("linked_account_${item.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(item.accentColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.logoText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.providerName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FutaTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Actif",
                                        tint = FutaGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "N°: ${item.accountNumber} • ${item.holderName}",
                                    fontSize = 11.sp,
                                    color = FutaTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dual Action Buttons: Top up Futa (Deposit) or Withdraw to Bank
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeActionAccount = item
                                    actionType = "DEPOSIT"
                                    actionAmountText = if (actionCurrency == Currency.CDF) "50000" else "20"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("deposit_button_${item.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FutaGreenDark)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Recharger (0 FC)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    activeActionAccount = item
                                    actionType = "WITHDRAW"
                                    actionAmountText = if (actionCurrency == Currency.CDF) "50000" else "20"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("withdraw_button_${item.id}"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = FutaPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Virer (1%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FutaPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Link new account CTA
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showLinkDialog = true }
                        .testTag("add_new_bank_card"),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter",
                            tint = FutaPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connecter une autre banque ou mobile money",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaPrimary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Sheet for Deposit or Withdrawal
    activeActionAccount?.let { targetAccount ->
        val isDeposit = actionType == "DEPOSIT"
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val amountNum = actionAmountText.toDoubleOrNull() ?: 0.0
        val fee = if (isDeposit) 0.0 else viewModel.calculateTransferFee(amountNum, actionCurrency)
        val maxAvailable = if (actionCurrency == Currency.CDF) account.cdfBalance else account.usdBalance
        val isValid = if (isDeposit) amountNum > 0 else (amountNum > 0 && amountNum + fee <= maxAvailable)

        ModalBottomSheet(
            onDismissRequest = { activeActionAccount = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .testTag("bank_action_sheet")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isDeposit) "Recharger depuis ${targetAccount.providerName}" else "Virement vers ${targetAccount.providerName}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                    IconButton(onClick = { activeActionAccount = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Currency chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = actionCurrency == Currency.CDF,
                        onClick = {
                            actionCurrency = Currency.CDF
                            actionAmountText = "50000"
                        },
                        label = { Text("CDF (Francs)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FutaPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = actionCurrency == Currency.USD,
                        onClick = {
                            actionCurrency = Currency.USD
                            actionAmountText = "20"
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
                    value = actionAmountText,
                    onValueChange = { actionAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(if (isDeposit) "Montant à créditer" else "Montant à virer") },
                    trailingIcon = {
                        Text(
                            text = if (actionCurrency == Currency.CDF) "FC" else "$",
                            fontWeight = FontWeight.Bold,
                            color = FutaPrimary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bank_action_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fee note
                Surface(
                    color = if (isDeposit) Color(0xFFD1FAE5) else Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isDeposit) "Frais de recharge : 0 FC (100% GRATUIT chez FutaPay)" else "Frais de virement : ${if (actionCurrency == Currency.CDF) "${dfCdf.format(fee)} FC" else "$${dfUsd.format(fee)}"} (1% max)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeposit) FutaGreenDark else FutaPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (isDeposit) {
                            viewModel.depositFromLinked(targetAccount, amountNum, actionCurrency)
                        } else {
                            viewModel.withdrawToLinked(targetAccount, amountNum, actionCurrency)
                        }
                        activeActionAccount = null
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_bank_action_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDeposit) FutaGreenDark else FutaPrimary
                    )
                ) {
                    Text(
                        text = if (isDeposit) "Valider la recharge immédiate" else "Confirmer le virement",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Dialog for linking a new Congolese bank or telecom account
    if (showLinkDialog) {
        val providers = listOf(
            "Rawbank (Illicocash)",
            "Equity BCDC",
            "Trust Merchant Bank (TMB)",
            "M-Pesa (Vodacom)",
            "Orange Money RDC",
            "Airtel Money RDC",
            "Afrimoney (Africell)"
        )

        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = {
                Text(
                    text = "Connecter un compte bancaire ou mobile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Sélectionnez votre établissement financier :",
                        fontSize = 12.sp,
                        color = FutaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        items(providers) { p ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        newProviderName = p
                                        newCategory = if (p.contains("M-Pesa") || p.contains("Orange") || p.contains("Airtel") || p.contains("Afrimoney")) {
                                            LinkedCategory.MOBILE_MONEY
                                        } else {
                                            LinkedCategory.BANK
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                color = if (newProviderName == p) FutaPrimary.copy(alpha = 0.12f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (p.contains("Bank") || p.contains("Rawbank") || p.contains("Equity") || p.contains("TMB")) Icons.Default.AccountBalance else Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = FutaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = p,
                                        fontSize = 13.sp,
                                        fontWeight = if (newProviderName == p) FontWeight.Bold else FontWeight.Normal,
                                        color = FutaTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newAccountNumber,
                        onValueChange = { newAccountNumber = it },
                        label = { Text("Numéro de compte ou Téléphone") },
                        placeholder = { Text("ex: 05100-01048192-01") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_account_number"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccountNumber.isNotBlank()) {
                            viewModel.linkAccount(
                                providerName = newProviderName,
                                category = newCategory,
                                accountNumber = newAccountNumber,
                                holderName = newHolderName
                            )
                            showLinkDialog = false
                            newAccountNumber = ""
                        }
                    },
                    enabled = newAccountNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary),
                    modifier = Modifier.testTag("submit_link_account_button")
                ) {
                    Text("Lier le compte")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
