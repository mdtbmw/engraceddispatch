package com.esdispatch.util

/**
 * Cargo Feasibility and Payload Intelligence Engine.
 * 
 * Accurately analyzes shipment items, distinguishes between carryable food meals
 * and uncarryable bulk freight, and enforces payload & dimension thresholds for
 * motorcycle dispatch and economy cargo vehicles.
 */
object CargoFeasibilityValidator {

    const val MAX_MOTORCYCLE_PAYLOAD_KG = 20.0
    const val MAX_MOTORCYCLE_DIMENSION_CM = 45.0
    const val MAX_ECONOMY_PAYLOAD_KG = 200.0
    const val MAX_ECONOMY_DIMENSION_CM = 250.0

    data class FeasibilityResult(
        val isFeasible: Boolean,
        val warningMessage: String? = null,
        val requiresSpecialHandling: Boolean = false
    )

    // Regex patterns for clearly uncarryable bulk freight for motorcycles
    private val HEAVY_BULK_KEYWORDS = listOf(
        Regex("""\b(bag|bags|sack|sacks)\s+of\s+(rice|beans|garri|flour|cement|sugar|fertilizer)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(50kg|25kg|100kg)\s*(rice|beans|garri|cement|flour)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(cement|sharp\s*sand|gravel|granite|tiles|c-stone)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(generator|gen|elepaq|sumec|lutian|tiger\s*gen|diesel\s*plant)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(refrigerator|fridge|deep\s*freezer|ice\s*chest)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(washing\s*machine|laundry\s*washer|dryer)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(mattress|bed\s*frame|foam\s*mattress|orthopedic\s*bed)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(gas\s*cylinder|12\.5kg\s*cylinder|25kg\s*cylinder|50kg\s*cylinder)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(sofa|couch|wardrobe|dining\s*table|office\s*desk|cabinet)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(car\s*engine|gearbox|axle|shock\s*absorber\s*set|iron\s*rods?)\b""", RegexOption.IGNORE_CASE)
    )

    // Allowed edible meal keywords (exempt from bulk produce triggers)
    private val FOOD_EXEMPTIONS = listOf(
        Regex("""\b(fried\s*rice|jollof\s*rice|white\s*rice|plate\s*of\s*rice|portion\s*of\s*rice|rice\s*and\s*stew|rice\s*meal|takeout\s*rice|bowl\s*of\s*rice)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(soup|stew|swallow|eba|amala|semo|pasta|noodles|burger|shawarma|pizza|cake|pastry|small\s*chops)\b""", RegexOption.IGNORE_CASE)
    )

    /**
     * Evaluates whether an item is safe and feasible to be dispatched on an Express motorcycle.
     */
    fun validateExpressCargo(
        itemName: String,
        category: String,
        weightKg: Double
    ): FeasibilityResult {
        val trimmedItem = itemName.trim()

        // 1. Weight Threshold check (Max 20kg for motorcycle dispatch)
        if (weightKg > MAX_MOTORCYCLE_PAYLOAD_KG) {
            return FeasibilityResult(
                isFeasible = false,
                warningMessage = "Motorcycle Payload Exceeded: Max payload is 20kg (current: ${weightKg}kg). Please choose Economy Cargo or reduce parcel weight."
            )
        }

        if (trimmedItem.isBlank()) {
            return FeasibilityResult(isFeasible = true)
        }

        // 2. Check if item is an edible food portion
        val isFoodMeal = FOOD_EXEMPTIONS.any { it.containsMatchIn(trimmedItem) } || category.equals("Food & Takeout", ignoreCase = true)

        if (isFoodMeal) {
            // Edible food is allowed as long as it does not exceed motorcycle weight
            return FeasibilityResult(isFeasible = true)
        }

        // 3. Check for forbidden uncarryable heavy/bulky freight
        for (pattern in HEAVY_BULK_KEYWORDS) {
            if (pattern.containsMatchIn(trimmedItem)) {
                return FeasibilityResult(
                    isFeasible = false,
                    warningMessage = "Uncarryable Motorcycle Cargo: Dispatch motorcycles cannot transport bulk heavy freight (e.g., bags of rice/cement, generators, appliances, mattresses). Max limit is 20kg / 45cm³. Please use Economy Cargo."
                )
            }
        }

        return FeasibilityResult(isFeasible = true)
    }

    /**
     * Validates dimensions and weight for Economy Cargo delivery.
     */
    fun validateEconomyCargo(
        itemName: String,
        weightKg: Double,
        lengthCm: Double,
        widthCm: Double,
        heightCm: Double
    ): FeasibilityResult {
        if (weightKg > MAX_ECONOMY_PAYLOAD_KG) {
            return FeasibilityResult(
                isFeasible = false,
                warningMessage = "Economy Freight Limit Exceeded: Maximum payload per vehicle is ${MAX_ECONOMY_PAYLOAD_KG}kg (current: ${weightKg}kg)."
            )
        }

        if (lengthCm > MAX_ECONOMY_DIMENSION_CM || widthCm > MAX_ECONOMY_DIMENSION_CM || heightCm > MAX_ECONOMY_DIMENSION_CM) {
            return FeasibilityResult(
                isFeasible = false,
                warningMessage = "Dimension Limit Exceeded: Maximum item dimension for standard cargo is ${MAX_ECONOMY_DIMENSION_CM}cm."
            )
        }

        return FeasibilityResult(isFeasible = true)
    }

    /**
     * Helper to check if a cargo item strictly violates dispatch constraints.
     */
    fun isStrictlyInfeasible(itemName: String, weightKg: Double): Boolean {
        return !validateExpressCargo(itemName, "", weightKg).isFeasible
    }
}
