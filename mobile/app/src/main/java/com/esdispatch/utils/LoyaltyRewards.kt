package com.esdispatch.utils

/**
 * Single source of truth for the ESDispatch VIP loyalty system so the dashboard,
 * profile and marketplace all use identical tiers and discounts.
 */
data class LoyaltyTierInfo(
    val name: String,
    val progress: Float,
    val nextLabel: String
)

object LoyaltyRewards {

    const val DISCOUNT_THRESHOLD_POINTS = 500
    const val DISCOUNT_POINTS_COST = 1000
    const val DISCOUNT_AMOUNT = 1000.0

    fun tierFor(points: Int): LoyaltyTierInfo = when {
        points < 100 -> LoyaltyTierInfo("Bronze Club", (points / 100f).coerceIn(0f, 1f), "100 Pts for Silver Tier")
        points < 500 -> LoyaltyTierInfo("Silver Tier", ((points - 100) / 400f).coerceIn(0f, 1f), "500 Pts for Gold Elite")
        points < 1000 -> LoyaltyTierInfo("Gold Elite", ((points - 500) / 500f).coerceIn(0f, 1f), "1,000 Pts for Platinum VIP")
        else -> LoyaltyTierInfo("Platinum VIP", 1f, "Max Level Achieved \uD83D\uDC51")
    }

    fun pointsDiscount(points: Int, redeem: Boolean): Double =
        if (redeem && points >= DISCOUNT_THRESHOLD_POINTS) DISCOUNT_AMOUNT else 0.0
}
