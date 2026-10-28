package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentPoint
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
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsMapScreen(
    viewModel: FutaViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(FutaScreen.HOME)
    }

    val agents by viewModel.agentPoints.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val account by viewModel.account.collectAsState()

    var selectedCommune by remember { mutableStateOf("Toutes") }
    var onlyUsdAvailable by remember { mutableStateOf(false) }

    var showGenerateCodeModal by remember { mutableStateOf(false) }
    var withdrawAmountText by remember { mutableStateOf("50000") }
    var withdrawCurrency by remember { mutableStateOf(uiState.activeCurrency) }

    val filteredAgents = remember(agents, selectedCommune, onlyUsdAvailable) {
        agents.filter { agent ->
            (selectedCommune == "Toutes" || agent.commune.equals(selectedCommune, true)) &&
                    (!onlyUsdAvailable || agent.hasUsdCash)
        }
    }

    val dfCdf = DecimalFormat("#,###")
    val dfUsd = DecimalFormat("#,##0.00")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Agents & Kiosques Futa",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FutaTextPrimary
                        )
                        Text(
                            text = "Dépôt 100% Gratuit • Retrait 1%",
                            fontSize = 11.sp,
                            color = FutaGreenDark
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(FutaScreen.HOME) },
                        modifier = Modifier.testTag("back_button_agents")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Withdrawal Code Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = FutaPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Retrait Cash chez l'Agent",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Sans carte • Code sécurisé 15 min",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = FutaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showGenerateCodeModal = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("generate_withdraw_code_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = FutaPrimary)
                        ) {
                            Text(
                                text = "Générer un code de retrait",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Filters
            item {
                Column {
                    Text(
                        text = "Communes de Kinshasa :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FutaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val communes = listOf("Toutes", "Gombe", "Bandalungwa", "Limete", "Kintambo", "Matete")
                        items(communes) { c ->
                            FilterChip(
                                selected = selectedCommune == c,
                                onClick = { selectedCommune = c },
                                label = { Text(c, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = FutaPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Agents count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Points relais agréés Futa (${filteredAgents.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = FutaTextPrimary
                    )
                }
            }

            // Agents List
            items(filteredAgents, key = { it.id }) { agent ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("agent_card_${agent.id}"),
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0ECFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = FutaPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = agent.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FutaTextPrimary
                                    )
                                    if (agent.verifiedBadge) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Vérifié",
                                            tint = FutaGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${agent.commune} • ${agent.distanceKm} km",
                                    fontSize = 11.sp,
                                    color = FutaPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = agent.address,
                            fontSize = 12.sp,
                            color = FutaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cash Availability Badges
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = if (agent.hasCdfCash) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (agent.hasCdfCash) "✓ Cash CDF dispo" else "✗ CDF épuisé",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (agent.hasCdfCash) FutaGreenDark else Color(0xFFDC2626),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = if (agent.hasUsdCash) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (agent.hasUsdCash) "✓ Cash USD dispo ($)" else "Pas de USD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (agent.hasUsdCash) Color(0xFF0369A1) else FutaTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = FutaTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = agent.hours,
                                    fontSize = 11.sp,
                                    color = FutaTextSecondary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.setNotification("Appel vers l'agent ${agent.phone}...")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Appeler",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Appeler", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Generate Withdrawal Code Dialog
    if (showGenerateCodeModal) {
        val amountNum = withdrawAmountText.toDoubleOrNull() ?: 0.0
        val maxAvailable = if (withdrawCurrency == Currency.CDF) account.cdfBalance else account.usdBalance
        val isValid = amountNum in 1.0..maxAvailable

        AlertDialog(
            onDismissRequest = { showGenerateCodeModal = false },
            title = {
                Text(
                    text = "Générer un code de retrait",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Présentez ce code à n'importe quel kiosque Futa agréé pour retirer vos espèces. Valable 15 minutes.",
                        fontSize = 12.sp,
                        color = FutaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = withdrawCurrency == Currency.CDF,
                            onClick = {
                                withdrawCurrency = Currency.CDF
                                withdrawAmountText = "50000"
                            },
                            label = { Text("CDF") }
                        )
                        FilterChip(
                            selected = withdrawCurrency == Currency.USD,
                            onClick = {
                                withdrawCurrency = Currency.USD
                                withdrawAmountText = "20"
                            },
                            label = { Text("USD") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = withdrawAmountText,
                        onValueChange = { withdrawAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Montant à retirer") },
                        trailingIcon = {
                            Text(
                                text = if (withdrawCurrency == Currency.CDF) "FC" else "$",
                                fontWeight = FontWeight.Bold,
                                color = FutaPrimary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.generateWithdrawalCode(amountNum, withdrawCurrency)
                        showGenerateCodeModal = false
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
                ) {
                    Text("Générer mon code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGenerateCodeModal = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Active Withdrawal Code Modal
    uiState.activeWithdrawalCode?.let { codeData ->
        AlertDialog(
            onDismissRequest = { viewModel.clearWithdrawalCode() },
            title = {
                Text(
                    text = "Code de Retrait Kiosque",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Montant : ${if (codeData.currency == Currency.CDF) "${dfCdf.format(codeData.amount)} FC" else "$${dfUsd.format(codeData.amount)}"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FutaPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = codeData.code,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FutaTextPrimary,
                            letterSpacing = 4.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Donnez ce code et votre nom à l'agent Futa. Expire dans 15 minutes.",
                        fontSize = 11.sp,
                        color = FutaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearWithdrawalCode() },
                    colors = ButtonDefaults.buttonColors(containerColor = FutaPrimary)
                ) {
                    Text("J'ai retiré mon cash")
                }
            }
        )
    }
}
