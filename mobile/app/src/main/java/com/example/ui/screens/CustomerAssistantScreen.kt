package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.components.RoundedSheet
import com.example.ui.components.ScreenHeader
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.HeaderBgColor
import com.example.viewmodel.DeliveryViewModel

@Composable
fun CustomerAssistantScreen(
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val chatMessages by viewModel.aiChatMessages.collectAsState()
    val isThinking by viewModel.aiIsThinking.collectAsState()
    val isLight = MaterialTheme.colorScheme.background == BackgroundLight
    val surfaceColor = AppBackground

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HeaderBgColor)
    ) {
        ScreenHeader(
            title = "AI Shipping Assistant",
            onBack = onBack
        )

        RoundedSheet(
            modifier = Modifier.weight(1f),
            containerColor = surfaceColor
        ) {
            CustomerAssistantTab(
                messages = chatMessages,
                isThinking = isThinking,
                onSendMessage = { viewModel.sendChatMessage(it) },
                onClearChat = { viewModel.clearChat() }
            )
        }
    }
}
