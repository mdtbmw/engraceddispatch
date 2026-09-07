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

    data class FeasibilityResult(
        val isFeasible: Boolean,
        val warningMessage: String? = null,
        val requiresSpecialHandling: Boolean = false
    ) {
        val rejectionReason: String? get() = warningMessage
    }

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
     * Universal evaluation of whether an item is safe and physically feasible to be dispatched
     * on the ESDispatch motorcycle fleet across ALL services (Express, Economy, Batch, Multi).
     */
    fun validateCargo(
        itemName: String,
        category: String = "",
        weightKg: Double = 1.0,
        lengthCm: Double = 20.0,
        widthCm: Double = 15.0,
        heightCm: Double = 10.0
    ): FeasibilityResult {
        val trimmedItem = itemName.trim()

        // 1. Universal Weight Limit Check (Motorcycle dispatch boxes strictly handle up to 20kg)
        if (weightKg > MAX_MOTORCYCLE_PAYLOAD_KG) {
            return FeasibilityResult(
                isFeasible = false,
                warningMessage = "Motorcycle Fleet Limit: All ESDispatch deliveries are currently operated via motorcycle couriers. Maximum payload is 20kg (current: ${String.format("%.1f", weightKg)}kg). Please reduce package weight."
            )
        }

        // 2. Volumetric Dimension Check (Motorcycle carrier dispatch box max 45cm on any axis)
        if (lengthCm > MAX_MOTORCYCLE_DIMENSION_CM || widthCm > MAX_MOTORCYCLE_DIMENSION_CM || heightCm > MAX_MOTORCYCLE_DIMENSION_CM) {
            return FeasibilityResult(
                isFeasible = false,
                warningMessage = "Carrier Box Limit: Parcel dimensions exceed dispatch box limits (Max ${MAX_MOTORCYCLE_DIMENSION_CM}cm per side). Bulky cargo cannot fit inside motorcycle dispatch carriers."
            )
        }

        if (trimmedItem.isBlank()) {
            return FeasibilityResult(isFeasible = true)
        }

        // 3. Edible food exemptions (plate of rice, jollof rice, meals) are safe within weight limit
        val isFoodMeal = FOOD_EXEMPTIONS.any { it.containsMatchIn(trimmedItem) } || category.equals("Food & Takeout", ignoreCase = true) || category.equals("Food & Groceries", ignoreCase = true)
        if (isFoodMeal) {
            return FeasibilityResult(isFeasible = true)
        }

        // 4. Check for forbidden uncarryable heavy/bulky freight
        for (pattern in HEAVY_BULK_KEYWORDS) {
            if (pattern.containsMatchIn(trimmedItem)) {
                return FeasibilityResult(
                    isFeasible = false,
                    warningMessage = "Motorcycle Fleet Limit: Dispatch motorcycles cannot carry heavy or bulk cargo (e.g., bags of rice/cement, generators, refrigerators, mattresses). Max limit is 20kg / 45cm³ carrier box."
                )
            }
        }

        return FeasibilityResult(isFeasible = true)
    }

    /**
     * Backward-compatible alias for Express delivery service.
     */
    fun validateExpressCargo(
        itemName: String,
        category: String,
        weightKg: Double
    ): FeasibilityResult {
        return validateCargo(itemName = itemName, category = category, weightKg = weightKg)
    }

    /**
     * Backward-compatible alias for Economy delivery service.
     * Enforces identical motorcycle physical constraints.
     */
    fun validateEconomyCargo(
        itemName: String,
        weightKg: Double,
        lengthCm: Double = 20.0,
        widthCm: Double = 15.0,
        heightCm: Double = 10.0
    ): FeasibilityResult {
        return validateCargo(itemName = itemName, category = "", weightKg = weightKg, lengthCm = lengthCm, widthCm = widthCm, heightCm = heightCm)
    }

    /**
     * Helper to check if a cargo item strictly violates dispatch constraints.
     */
    fun isStrictlyInfeasible(itemName: String, weightKg: Double): Boolean {
        return !validateCargo(itemName, "", weightKg).isFeasible
    }
}
