package com.example.data.models

data class Transaction(
    val id: Long = 0,
    val title: String,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val createdAt: Long = System.currentTimeMillis(),
    val reference: String = "",
    val status: TransactionStatus = TransactionStatus.COMPLETED
)

enum class TransactionType { CREDIT, DEBIT }
enum class TransactionStatus { PENDING, COMPLETED, FAILED }
