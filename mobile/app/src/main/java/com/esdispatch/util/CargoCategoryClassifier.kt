package com.esdispatch.util

/**
 * Intelligent Natural Language Cargo Category Classifier.
 * 
 * Automatically infers the most accurate parcel category based on free-form
 * user inputs (e.g., "bag of rice" -> Food & Takeout, "iPhone charger" -> Electronics).
 */
object CargoCategoryClassifier {

    private val FOOD_PATTERNS = listOf(
        Regex("""\b(rice|beans|garri|flour|yam|plantain|potato|potatoes|cassava)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(bag\s+of\s+rice|plate\s+of\s+rice|jollof|fried\s*rice|white\s*rice)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(food|meal|takeout|soup|stew|swallow|eba|amala|semo|fufu|poundo)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(burger|shawarma|pizza|sandwich|pie|pastry|cake|cookies|bread|suya|barbecue|bbq)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(meat|chicken|fish|turkey|beef|pork|egg|eggs|seafood|pepper\s*soup)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(noodle|noodles|indomie|spaghetti|pasta|macaroni|cereal|cornflakes|custard)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(groceries|vegetables|tomatoes|pepper|onions|fruits|apples|oranges|bananas)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(juice|drink|drinks|wine|water|smoothie|beverage|tea|coffee|milk|butter)\b""", RegexOption.IGNORE_CASE)
    )

    private val ELECTRONICS_PATTERNS = listOf(
        Regex("""\b(phone|iphone|samsung|android|redmi|infinix|tecno|pixel|xiaomi|nokia|huawei)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(laptop|macbook|dell|hp|lenovo|thinkpad|asus|acer|computer|desktop|pc)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(charger|cable|usb|type\s*c|lightning\s*cable|adapter|cord)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(airpod|airpods|earpod|earpods|earphone|earphones|headphone|headphones|headset|earbud|earbuds)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(power\s*bank|powerbank|battery|inverter|solar|ups)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(ipad|tablet|kindle|screen|display|monitor|smartwatch|apple\s*watch)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(camera|lens|tripod|microphone|speaker|bluetooth\s*speaker|soundbar)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(mouse|keyboard|drive|hard\s*drive|flash\s*drive|ssd|flashdrive|memory\s*card)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(console|playstation|ps4|ps5|xbox|nintendo|gamepad|controller)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(gadget|gadgets|electronic|electronics|hardware)\b""", RegexOption.IGNORE_CASE)
    )

    private val FASHION_PATTERNS = listOf(
        Regex("""\b(cloth|clothes|clothing|shirt|t-shirt|tshirt|tee|polo|blouse|top|hoodie|sweater|jacket|coat)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(trouser|trousers|pant|pants|jeans|shorts|skirt|dress|gown|suit|blazer|tracksuit)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(shoe|shoes|sneaker|sneakers|boot|boots|heel|heels|sandal|sandals|slippers|slides|crocs)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(wig|wigs|hair|braids|extensions|weave|closure|frontal)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(handbag|hand\s*bag|purse|wallet|tote|backpack|crossbody|duffel)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(belt|cap|hat|scarf|sunglasses|glasses|tie|cufflinks|watch|wrist\s*watch)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(underwear|boxers|lingerie|socks|swimwear|pyjamas|sleepwear)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(fabric|lace|ankara|aso\s*ebi|textile|material|tailor)\b""", RegexOption.IGNORE_CASE)
    )

    private val PHARMACY_PATTERNS = listOf(
        Regex("""\b(drug|drugs|medicine|medicines|medication|pill|pills|tablet|tablets|capsule|capsules)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(pharmacy|chemist|prescription|syrup|dosage|antibiotic|antibiotics|painkiller|painkillers)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(paracetamol|panadol|ibuprofen|aspirin|malaria|artemether|lonart|coartem)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(vitamin|vitamins|supplements|cod\s*liver|multivitamin|folic\s*acid)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(bandage|plaster|gauze|cotton\s*wool|antiseptic|dettol|savlon|iodine|inhaler)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(thermometer|bp\s*monitor|glucometer|injection|syringe|drip|test\s*kit)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(ointment|balm|cream|lotion|eyedrops|eye\s*drops|drops|medical|hospital)\b""", RegexOption.IGNORE_CASE)
    )

    private val DOCUMENTS_PATTERNS = listOf(
        Regex("""\b(document|documents|doc|docs|paper|papers|paperwork|letter|letters)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(contract|contracts|agreement|agreements|deed|receipt|receipts|invoice|invoices)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(certificate|certificates|diploma|transcript|degree|credentials|waec|neco|jamb)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(passport|passports|visa|id\s*card|id|nin|driver's\s*license|voter's\s*card)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(file|files|folder|folders|envelope|envelopes|dossier|affidavit|court\s*order)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(book|books|novel|textbook|manual|brochure|catalogue|flyer|flyers)\b""", RegexOption.IGNORE_CASE)
    )

    private val FRAGILE_PATTERNS = listOf(
        Regex("""\b(fragile|glass|glasses|mirror|ceramic|ceramics|porcelain|china|crystal)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(perfume|perfumes|cologne|scent|fragrance|body\s*spray|diffuser)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(bottle|bottles|flagon|jar|vial|decanter|wine\s*glass|mug|cup|plate|dish)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(jewelry|jewellery|gold|silver|diamond|pendant|necklace|ring|earring|bracelet)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(clock|vase|sculpture|artwork|frame|photo\s*frame|ornament|chandelier)\b""", RegexOption.IGNORE_CASE)
    )

    /**
     * Infers the product category from text describing the item.
     * Returns the matching category string, or null if no confident match.
     */
    fun inferCategory(query: String): String? {
        val trimmed = query.trim()
        if (trimmed.length < 2) return null

        // 1. Food patterns (check food before general bags/parcels so "bag of rice" is classified as Food!)
        if (FOOD_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Food & Takeout"
        }

        // 2. Electronics
        if (ELECTRONICS_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Electronics"
        }

        // 3. Pharmacy & Health
        if (PHARMACY_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Pharmacy & Health"
        }

        // 4. Documents & Papers
        if (DOCUMENTS_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Documents"
        }

        // 5. Fragile & Valuables
        if (FRAGILE_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Fragile & Valuables"
        }

        // 6. Fashion & Apparel
        if (FASHION_PATTERNS.any { it.containsMatchIn(trimmed) }) {
            return "Fashion & Apparel"
        }

        return null
    }
}
