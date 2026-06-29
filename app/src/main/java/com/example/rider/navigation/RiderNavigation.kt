package com.example.rider.navigation

sealed class RiderView {
    data object Splash : RiderView()
    data object Login : RiderView()
    data object Dashboard : RiderView()
    data class DeliveryDetail(val trackingNumber: String) : RiderView()
    data object Profile : RiderView()
    data object Settings : RiderView()
    data object DeliveryHistory : RiderView()
}
