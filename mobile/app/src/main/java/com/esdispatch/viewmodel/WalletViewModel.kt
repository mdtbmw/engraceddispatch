package com.esdispatch.viewmodel

import androidx.lifecycle.viewModelScope
import com.esdispatch.data.CardInfo
import com.esdispatch.data.FirebaseManager
import com.esdispatch.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class WalletViewModel : AuthViewModel() {

    protected val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    protected val _paymentCards = MutableStateFlow<List<CardInfo>>(emptyList())
    val paymentCards: StateFlow<List<CardInfo>> = _paymentCards.asStateFlow()

    protected val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun topUpWallet(
        amount: Double,
        reference: String? = null,
        onComplete: ((Boolean, String) -> Unit)? = null
    ) {
        val uid = _firebaseUserId.value
        if (uid == null) {
            onComplete?.invoke(false, "User must be signed in to perform wallet transactions.")
            return
        }

        if (amount == 0.0) {
            onComplete?.invoke(false, "Invalid transaction amount.")
            return
        }

        val isTopUp = amount > 0
        val title = if (isTopUp) "Wallet Top Up (Paystack)" else "Cash Withdrawal"
        val displayAmt = if (amount < 0) -amount else amount
        val txRef = reference ?: "TX-PAY-${System.currentTimeMillis()}"

        com.esdispatch.data.FirebaseManager.updateUserWalletBalance(uid, amount) { success, newBalance ->
            if (success) {
                _walletBalance.value = newBalance
                savePref("wallet_balance", newBalance)

                val localTx = Transaction(
                    id = txRef,
                    title = title,
                    date = "Today",
                    amount = displayAmt,
                    isTopUp = isTopUp,
                    userId = uid,
                    reference = txRef
                )
                _transactions.value = listOf(localTx) + _transactions.value

                com.esdispatch.data.FirebaseManager.recordLedgerTransaction(
                    userId = uid,
                    amount = amount,
                    title = title,
                    isTopUp = isTopUp,
                    reference = txRef
                ) { _ -> }

                val notifTitle = if (isTopUp) "Wallet Credited! 💳⚡" else "Wallet Debited! 💸"
                val notifMessage = if (isTopUp) {
                    "Your ESDispatch wallet has been topped up with ₦${String.format("%,.2f", displayAmt)}. Real-time logistics power unlocked! 🚀✨"
                } else {
                    "Your ESDispatch wallet has been debited by ₦${String.format("%,.2f", displayAmt)}."
                }
                addNotification(notifTitle, notifMessage)

                appContext?.let { ctx ->
                    try {
                        com.esdispatch.data.MyFirebaseMessagingService.showNotification(
                            context = ctx,
                            title = notifTitle,
                            message = notifMessage,
                            parcelId = null
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("WalletNotif", "Error showing wallet notification: ${e.message}")
                    }
                }

                onComplete?.invoke(true, "Wallet successfully updated. New balance: ₦${String.format("%,.2f", newBalance)}")
            } else {
                onComplete?.invoke(false, "Failed to process wallet transaction. Please check your network and retry.")
            }
        }
    }

    fun adminFundUserWallet(userId: String, userName: String, amount: Double, onResult: (Boolean, String) -> Unit) {
            if (amount <= 0) { onResult(false, "Amount must be positive"); return }
            val db = com.esdispatch.data.FirebaseManager.firestore ?: run {
                onResult(false, "Firestore unavailable"); return
            }
            viewModelScope.launch {
                try {
                    db.collection("users").document(userId).get().addOnSuccessListener { snap ->
                        val currentBalance = (snap.get("walletBalance") as? Number)?.toDouble() ?: 0.0
                        val newBalance = currentBalance + amount
                        db.collection("users").document(userId).update("walletBalance", newBalance)
                        val txRef = "ESD-ADMIN-${System.currentTimeMillis()}"
                        val txMap = hashMapOf(
                            "id" to txRef, "title" to "Admin Credit",
                            "date" to "Today", "amount" to amount,
                            "isTopUp" to true, "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("users").document(userId).collection("transactions").document(txRef).set(txMap)
                        logAdminActivity("Wallet Credit", "Credited $amount to $userName ($userId)")
                        onResult(true, "Wallet credited successfully")
                    }.addOnFailureListener { e ->
                        onResult(false, e.message ?: "Failed to fetch user")
                    }
                } catch (e: Exception) {
                    onResult(false, e.message ?: "Unknown error")
                }
            }
        }


    fun addPaymentCard(card: CardInfo, onResult: (Boolean) -> Unit) {
        val current = _paymentCards.value.toMutableList()
        current.add(card)
        _paymentCards.value = current
        onResult(true)
    }

    fun removePaymentCard(card: CardInfo, onResult: (Boolean) -> Unit) {
        val current = _paymentCards.value.toMutableList()
        current.remove(card)
        _paymentCards.value = current
        onResult(true)
    }

    fun fetchUserPaymentCards() {
        // Implementation
    }
}
