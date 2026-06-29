package com.example.ui.navigation

sealed class AppView {
    data object Splash : AppView()
    data object Login : AppView()
    data object SignUp : AppView()
    data object Dashboard : AppView()
    data object BookingDetails : AppView()
    data object BookingExpress : AppView()
    data object BookingEconomy : AppView()
    data object BookingBatch : AppView()
    data object BookingMulti : AppView()
    data class ActiveTracking(val trackingNumber: String) : AppView()
    data object OrderLogs : AppView()
    data object Wallet : AppView()
    data object Profile : AppView()
    data object Settings : AppView()
    data object RiderReview : AppView()
    data object Notifications : AppView()
    data object Promotions : AppView()
    data object AddressBook : AppView()
    data object Referral : AppView()
    data object Scanner : AppView()
}
