import os
import re

file_path = r"d:\Eng App\mobile\app\src\main\java\com\esdispatch\viewmodel\DeliveryViewModel.kt"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# We need to extract the functions carefully. We'll find them and extract them.
def extract_function(name, text):
    # Find the start of the function
    start_idx = text.find(f"fun {name}(")
    if start_idx == -1:
        start_idx = text.find(f"fun {name} ")
    if start_idx == -1:
        return "", text
        
    # Find the matching closing brace
    open_braces = 0
    in_function = False
    end_idx = -1
    
    for i in range(start_idx, len(text)):
        if text[i] == '{':
            open_braces += 1
            in_function = True
        elif text[i] == '}':
            open_braces -= 1
            
        if in_function and open_braces == 0:
            end_idx = i + 1
            break
            
    if end_idx != -1:
        func_text = text[start_idx:end_idx]
        # Remove from text, including preceding spaces
        # Find preceding newline
        nl_idx = text.rfind('\n', 0, start_idx)
        if nl_idx != -1:
            start_idx = nl_idx + 1
            
        new_text = text[:start_idx] + text[end_idx:]
        return func_text, new_text
    
    return "", text

funcs_to_extract = [
    "topUpWallet",
    "adminFundUserWallet",
    "fetchUserPaymentCards",
    "addPaymentCard",
    "removePaymentCard"
]

extracted_funcs = []
for f_name in funcs_to_extract:
    func_text, content = extract_function(f_name, content)
    # The first one might be private, fetchUserPaymentCards
    if "private fun fetchUserPaymentCards" in content:
        func_text2, content = extract_function("fetchUserPaymentCards", content.replace("private fun", "protected fun"))
    if func_text:
        extracted_funcs.append(func_text)

# Also replace inheritance
content = content.replace("class DeliveryViewModel : AuthViewModel() {", "class DeliveryViewModel : WalletViewModel() {")

# Remove state flows
flows_to_remove = [
    r'\s*private val _walletBalance = MutableStateFlow\([^)]+\)\s*val walletBalance: StateFlow<Double> = _walletBalance\.asStateFlow\(\)\n',
    r'\s*private val _paymentCards = MutableStateFlow\([^)]+\)\s*val paymentCards: StateFlow<List<CardInfo>> = _paymentCards\.asStateFlow\(\)\n',
    r'\s*private val _transactions = MutableStateFlow\([^)]+\)\s*val transactions: StateFlow<List<Transaction>> = _transactions\.asStateFlow\(\)\n'
]

for p in flows_to_remove:
    content = re.sub(p, '\n', content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

# Now write WalletViewModel.kt
wallet_content = """package com.esdispatch.viewmodel

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

"""
for func in extracted_funcs:
    # ensure it's protected or public
    if func.startswith("private fun"):
        func = func.replace("private fun", "protected fun")
    wallet_content += "    " + func.replace("\n", "\n    ") + "\n\n"

wallet_content += "}\n"

with open(r"d:\Eng App\mobile\app\src\main\java\com\esdispatch\viewmodel\WalletViewModel.kt", "w", encoding="utf-8") as f:
    f.write(wallet_content)

print("Extracted WalletViewModel")
