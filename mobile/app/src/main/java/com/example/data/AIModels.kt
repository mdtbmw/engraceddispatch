package com.example.data

import java.util.UUID

enum class RiderStatus {
    ONLINE, BUSY, OFFLINE
}

data class Rider(
    val id: String = UUID.randomUUID().toString().substring(0, 6),
    val name: String,
    val phone: String,
    val avatar: String,
    val vehicleType: String, // Bike, Tricycle, Van, Truck
    val status: RiderStatus = RiderStatus.ONLINE,
    val latitude: Double,
    val longitude: Double,
    val currentWorkload: Int = 0,
    val batteryLevel: Int = 92,
    val rating: Double = 4.8,
    val averageDeliveryTimeMin: Int = 22,
    val cancellationHistoryCount: Int = 1,
    val fuelEfficiency: Double = 32.5, // km per liter / charge
    val shiftSchedule: String = "08:00 - 17:00",
    val distanceToPickupKm: Double = 1.2,
    val activeDeliveriesCount: Int = 0
)

data class AIChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class IncidentReport(
    val id: String = "INC-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
    val title: String,
    val timestamp: String,
    val customerName: String,
    val riderName: String,
    val severity: String, // Low, Medium, High, Critical
    val gpsLocation: String,
    val description: String,
    val suggestedAction: String,
    val evidenceUploaded: Boolean = true
)

data class DemandPrediction(
    val hour: String,
    val predictedBookings: Int,
    val recommendedRiders: Int,
    val confidence: Int // percentage
)

data class RiskReport(
    val score: Int, // 0 to 100
    val riskFactors: List<String>,
    val mitigationSuggested: String,
    val label: String // Safe, Moderate Risk, Critical Risk
)

data class PODAnalysis(
    val id: String = "POD-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
    val packageVisible: Boolean,
    val customerReceived: Boolean,
    val imageQuality: String, // Low, Medium, High
    val locationVerified: Boolean,
    val timestampVerified: Boolean,
    val fakeConfidence: Int, // percentage of being fraudulent
    val isApproved: Boolean
)

data class FraudAlert(
    val id: String = "FRD-" + UUID.randomUUID().toString().substring(0, 6).uppercase(),
    val timestamp: String,
    val userName: String,
    val reason: String,
    val severity: String, // Flagged, Suspended
    val score: Int // risk percentage
)

data class SelfLearningWeights(
    val distanceWeight: Float = 0.35f,
    val ratingWeight: Float = 0.25f,
    val workloadWeight: Float = 0.15f,
    val vehicleFitWeight: Float = 0.15f,
    val cancellationWeight: Float = 0.10f
)

data class RiderPerformanceMetric(
    val riderId: String,
    val name: String,
    val completedDeliveries: Int,
    val averageSpeedKmh: Double,
    val dynamicRatingTrend: List<Double>, // Last 5 ratings
    val utilizationRate: Int // Percentage
)

data class DeliveryPerformanceTrend(
    val label: String, // e.g., Day of week
    val totalDeliveries: Int,
    val onTimePercentage: Double,
    val avgCost: Double
)
