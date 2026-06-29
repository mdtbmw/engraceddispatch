package com.example.ui.navigation

sealed class AppView {
    data object Splash : AppView()
    data object Login : AppView()
    data object Dashboard : AppView()
    data class ActiveTracking(val trackingNumber: String) : AppView()
    data object OrderLogs : AppView()
    data object Wallet : AppView()
    data object Profile : AppView()
    data object Settings : AppView()
    data object Notifications : AppView()
    data object Scanner : AppView()
    data class MapNavigation(val trackingNumber: String) : AppView()
}
