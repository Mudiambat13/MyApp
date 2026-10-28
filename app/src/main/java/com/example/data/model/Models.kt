package com.example.data.model

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Currency(val code: String, val symbol: String) {
    CDF("CDF", "FC"),
    USD("USD", "$")
}

data class WalletAccount(
    val userName: String = "Jonathan Mudiambata",
    val userPhone: String = "+243 82 145 9081",
    val futaId: String = "FT-821459",
    val cdfBalance: Double = 485000.0,
    val usdBalance: Double = 175.50,
    val exchangeRateUsdToCdf: Double = 2850.0,
    val isCardFrozen: Boolean = false,
    val virtualCardNumber: String = "4243 •••• •••• 9081",
    val isBalanceVisible: Boolean = true
) {
    fun formattedCdf(): String {
        val formatter = DecimalFormat("#,###")
        return "${formatter.format(cdfBalance)} FC"
    }

    fun formattedUsd(): String {
        val formatter = DecimalFormat("#,##0.00")
        return "$${formatter.format(usdBalance)}"
    }
}

enum class TransactionType {
    TRANSFER_SENT,
    TRANSFER_RECEIVED,
    BANK_TOPUP,
    BANK_WITHDRAWAL,
    BILL_PAYMENT,
    MERCHANT_PAY,
    TONTINE_CONTRIBUTION,
    CURRENCY_EXCHANGE
}

enum class TransactionStatus {
    COMPLETED,
    PENDING,
    FAILED
}

data class Transaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val currency: Currency,
    val fee: Double,
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String,
    val partnerName: String? = null,
    val recipientPhone: String? = null,
    val notes: String? = null
) {
    fun formattedAmount(): String {
        val sign = when (type) {
            TransactionType.TRANSFER_RECEIVED,
            TransactionType.BANK_TOPUP -> "+"
            else -> "-"
        }
        val formatter = if (currency == Currency.CDF) DecimalFormat("#,###") else DecimalFormat("#,##0.00")
        val suffix = if (currency == Currency.CDF) " FC" else ""
        val prefix = if (currency == Currency.USD) "$" else ""
        return "$sign$prefix${formatter.format(amount)}$suffix"
    }

    fun formattedFee(): String {
        if (fee <= 0.0) return "Gratuit (0 FC)"
        val formatter = if (currency == Currency.CDF) DecimalFormat("#,###") else DecimalFormat("#,##0.00")
        val suffix = if (currency == Currency.CDF) " FC" else ""
        val prefix = if (currency == Currency.USD) "$" else ""
        return "Frais: $prefix${formatter.format(fee)}$suffix (1%)"
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.FRENCH)
        return sdf.format(Date(timestamp))
    }
}

enum class LinkedCategory {
    BANK,
    MOBILE_MONEY
}

data class LinkedAccount(
    val id: String,
    val providerName: String,
    val category: LinkedCategory,
    val accountNumber: String,
    val holderName: String,
    val isConnected: Boolean = true,
    val lastSyncTime: String = "Synchronisé à l'instant",
    val accentColorHex: Long = 0xFF0056D2,
    val logoText: String
)

data class AgentPoint(
    val id: String,
    val name: String,
    val commune: String,
    val address: String,
    val distanceKm: Double,
    val hasCdfCash: Boolean = true,
    val hasUsdCash: Boolean = true,
    val phone: String,
    val hours: String = "07:30 - 20:30",
    val verifiedBadge: Boolean = true
)

data class BillProvider(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val inputPrompt: String,
    val inputPlaceholder: String,
    val suggestedAmountsCdf: List<Double>,
    val suggestedAmountsUsd: List<Double>,
    val isInstantToken: Boolean = false
)

data class TontineGroup(
    val id: String,
    val title: String,
    val targetCycleAmount: Double,
    val currency: Currency,
    val frequency: String,
    val membersCount: Int,
    val currentCycleRound: Int,
    val totalRounds: Int,
    val nextRecipient: String,
    val hasUserPaidThisRound: Boolean
)
