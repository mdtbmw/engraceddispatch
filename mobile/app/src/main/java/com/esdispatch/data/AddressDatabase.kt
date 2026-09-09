package com.esdispatch.data

/**
 * ESDispatch Address Database — Benin City, Edo State Exclusive
 * Contains comprehensive real addresses and verified landmarks within Benin City, Edo State.
 * Strictly bounded to Benin City operations (No external/Lagos entries).
 */
object AddressDatabase {

    data class AddressEntry(
        val displayName: String,
        val city: String = "Benin City",
        val lat: Double,
        val lng: Double,
        val tags: List<String> = emptyList()
    ) {
        fun toSearchResult(): com.esdispatch.utils.SearchResultItem {
            val parts = displayName.split(",", limit = 2)
            val title = parts.getOrNull(0)?.trim() ?: displayName
            val address = if (parts.size > 1) parts[1].trim() else "Benin City, Edo State"
            return com.esdispatch.utils.SearchResultItem(
                title = title,
                fullAddress = address,
                lat = lat,
                lng = lng
            )
        }
    }

    val entries: List<AddressEntry> = listOf(
        // =============================================================
        // AIRPORT ROAD & OKO / OGBA DISTRICT
        // =============================================================
        AddressEntry("Benin City Airport (BNI), Airport Road, Benin City", "Benin City", 6.3166, 5.5995, listOf("airport", "travel", "bni", "benin airport", "flight", "terminal")),
        AddressEntry("Airport Road, GRA / Oko, Benin City", "Benin City", 6.3217, 5.6018, listOf("airport road", "oko", "air force")),
        AddressEntry("Air Force Base, Airport Road, Benin City", "Benin City", 6.3142, 5.5982, listOf("air force", "base", "airport road")),
        AddressEntry("Oko Central, Airport Road, Benin City", "Benin City", 6.3088, 5.5942, listOf("oko", "oko central", "airport road")),
        AddressEntry("Ogba Zoo & Nature Park, Airport Road Extension, Benin City", "Benin City", 6.2995, 5.5891, listOf("ogba zoo", "nature park", "zoo", "ogba")),
        AddressEntry("Evbuoriaria Industrial Layout, Sapele Road / Bypass, Benin City", "Benin City", 6.2941, 5.6312, listOf("evbuoriaria", "industrial", "sapele road")),

        // =============================================================
        // GRA (GOVERNMENT RESERVATION AREA) & ENVIRONS
        // =============================================================
        AddressEntry("GRA Phase 1, Benin City", "Benin City", 6.3422, 5.6307, listOf("gra", "estate", "phase 1", "residential")),
        AddressEntry("GRA Phase 2, Benin City", "Benin City", 6.3481, 5.6412, listOf("gra", "gra phase 2", "estate", "residential")),
        AddressEntry("GRA Phase 3, Benin City", "Benin City", 6.3538, 5.6488, listOf("gra", "gra phase 3", "estate")),
        AddressEntry("Ihama Road, GRA, Benin City", "Benin City", 6.3458, 5.6389, listOf("ihama", "ihama road", "gra", "restaurant", "hotel")),
        AddressEntry("Boundary Road, GRA, Benin City", "Benin City", 6.3412, 5.6345, listOf("boundary road", "gra", "boundary")),
        AddressEntry("Ugbor Road, GRA / Ugbor, Benin City", "Benin City", 6.3531, 5.6411, listOf("ugbor", "ugbor road", "ugbor central")),
        AddressEntry("Country Home Motel Road, Off Sapele Road, Benin City", "Benin City", 6.3298, 5.6361, listOf("country home", "country home motel", "sapele road")),
        AddressEntry("Limit Road, Off Sapele Road, Benin City", "Benin City", 6.3245, 5.6319, listOf("limit road", "sapele road")),
        AddressEntry("Giwa-Amu Street, GRA, Benin City", "Benin City", 6.3435, 5.6372, listOf("giwa-amu", "giwa amu", "gra")),
        AddressEntry("Aideyan Street, Off Golf Course Road, GRA, Benin City", "Benin City", 6.3401, 5.6358, listOf("aideyan", "golf course", "gra")),
        AddressEntry("Reservation Road, GRA, Benin City", "Benin City", 6.3441, 5.6378, listOf("reservation road", "reservation", "gra")),
        AddressEntry("Adesuwa Road (Adesuwa Grammar School Area), GRA, Benin City", "Benin City", 6.3405, 5.6362, listOf("adesuwa", "adesuwa road", "adesuwa grammar")),
        AddressEntry("Edo State Government House, Dennis Osadebay Way, GRA, Benin City", "Benin City", 6.3354, 5.6277, listOf("government house", "state house", "governor", "osadebay")),

        // =============================================================
        // RING ROAD (KING'S SQUARE), OBA MARKET & ROYAL HERITAGE
        // =============================================================
        AddressEntry("Ring Road (King's Square), City Center, Benin City", "Benin City", 6.3315, 5.6262, listOf("ring road", "kings square", "roundabout", "center")),
        AddressEntry("Oba's Palace (Royal Palace), Oba Ovonramwen Square, Benin City", "Benin City", 6.3345, 5.6254, listOf("palace", "king", "oba", "royal", "ovonramwen")),
        AddressEntry("Benin National Museum, King's Square, Benin City", "Benin City", 6.3341, 5.6264, listOf("museum", "national museum", "benin museum", "heritage")),
        AddressEntry("Oba Market, Oba Market Road, Benin City", "Benin City", 6.3339, 5.6267, listOf("market", "oba market", "shopping", "trade")),
        AddressEntry("Igun Street (Bronze Casters Cultural Heritage), Benin City", "Benin City", 6.3333, 5.6261, listOf("igun street", "igun", "bronze", "casters", "craft")),
        AddressEntry("Mission Road, Benin City", "Benin City", 6.3361, 5.6283, listOf("mission road", "mission", "shopping", "plaza")),
        AddressEntry("Upper Mission Road, Benin City", "Benin City", 6.3378, 5.6305, listOf("upper mission", "mission road", "commercial")),
        AddressEntry("Forestry Road, Benin City", "Benin City", 6.3394, 5.6318, listOf("forestry", "forestry road", "banks", "commercial")),
        AddressEntry("Akpakpava Road, Benin City", "Benin City", 6.3328, 5.6248, listOf("akpakpava", "akpakpava road", "transport", "offices")),
        AddressEntry("Benin City Bus Terminal, Akpakpava Road, Benin City", "Benin City", 6.3318, 5.6233, listOf("bus", "terminal", "transport", "park", "akpakpava")),
        AddressEntry("Dawson Road, Benin City", "Benin City", 6.3349, 5.6289, listOf("dawson road", "dawson", "commercial")),
        AddressEntry("Wire Road, Benin City", "Benin City", 6.3321, 5.6212, listOf("wire road", "wire")),
        AddressEntry("Plymouth Road, Benin City", "Benin City", 6.3310, 5.6238, listOf("plymouth", "plymouth road")),
        AddressEntry("Central Hospital, Sapele Road / King's Square, Benin City", "Benin City", 6.3340, 5.6262, listOf("hospital", "central hospital", "medical", "health")),
        AddressEntry("Federal High Court, Adesuwa Road, Benin City", "Benin City", 6.3341, 5.6272, listOf("court", "federal high court", "judiciary")),

        // =============================================================
        // UGBOWO & USELU (UNIVERSITY / HEALTH HUBS)
        // =============================================================
        AddressEntry("University of Benin (UNIBEN), Ugbowo Main Campus, Benin City", "Benin City", 6.3782, 5.6283, listOf("uniben", "university of benin", "ugbowo", "campus", "university")),
        AddressEntry("UNIBEN Main Gate, Ugbowo Lagos Road, Benin City", "Benin City", 6.3812, 5.6291, listOf("uniben gate", "main gate", "ugbowo gate")),
        AddressEntry("UNIBEN Teaching Hospital (UBTH), Ugbowo, Benin City", "Benin City", 6.3769, 5.6290, listOf("hospital", "ubth", "teaching hospital", "medical", "clinic")),
        AddressEntry("Ekosodin Village (UNIBEN Back Gate), Ugbowo, Benin City", "Benin City", 6.3892, 5.6241, listOf("ekosodin", "uniben back gate", "student village")),
        AddressEntry("Uselu Market, Uselu, Benin City", "Benin City", 6.3752, 5.6208, listOf("market", "uselu", "uselu market", "shopping")),
        AddressEntry("Uselu Shell, Ugbowo Lagos Road, Benin City", "Benin City", 6.3725, 5.6155, listOf("uselu", "uselu shell", "lagos road")),
        AddressEntry("Five Junction, Uselu / Textile Mill Road, Benin City", "Benin City", 6.3612, 5.6189, listOf("five junction", "textile mill", "uselu")),
        AddressEntry("Textile Mill Road, Benin City", "Benin City", 6.3412, 5.6388, listOf("textile mill", "textile mill road", "ogida")),
        AddressEntry("Siluko Road, Benin City", "Benin City", 6.3282, 5.6348, listOf("siluko", "siluko road", "ogida")),
        AddressEntry("Oliha Market, Siluko Road, Benin City", "Benin City", 6.3312, 5.6239, listOf("oliha", "oliha market", "market", "siluko")),
        AddressEntry("Technical College Road, Benin City", "Benin City", 6.3512, 5.6210, listOf("technical college", "technical college road")),
        AddressEntry("Oluku Bypass / Toll Gate Area, Ugbowo Extension, Benin City", "Benin City", 6.4211, 5.6318, listOf("oluku", "oluku bypass", "bypass", "toll gate")),

        // =============================================================
        // IKPOBA HILL, ADUWAWA, RAMAT & AGABOR AXIS
        // =============================================================
        AddressEntry("Ikpoba Hill, Benin City", "Benin City", 6.3499, 5.6353, listOf("ikpoba hill", "ikpoba", "hill")),
        AddressEntry("Ramat Park, Ikpoba Hill, Benin City", "Benin City", 6.3582, 5.6412, listOf("ramat park", "park", "ikpoba hill", "transport")),
        AddressEntry("Oregbeni Housing Estate, Ikpoba Hill, Benin City", "Benin City", 6.3498, 5.6352, listOf("estate", "oregbeni", "ikpoba", "residential")),
        AddressEntry("Federal Secretariat Complex, Aduwawa, Benin City", "Benin City", 6.3688, 5.6491, listOf("secretariat", "federal secretariat", "aduwawa")),
        AddressEntry("Aduwawa Market, Aduwawa, Benin City", "Benin City", 6.3622, 5.6458, listOf("aduwawa", "aduwawa market", "market")),
        AddressEntry("Guinness Nigeria Brewery, Ikpoba Hill, Benin City", "Benin City", 6.3518, 5.6389, listOf("guinness", "brewery", "ikpoba")),
        AddressEntry("New Benin Market, New Benin, Benin City", "Benin City", 6.3302, 5.6222, listOf("market", "new benin", "shopping", "new benin market")),
        AddressEntry("New Benin Commercial Quarters, Benin City", "Benin City", 6.3305, 5.6225, listOf("new benin", "quarters")),
        AddressEntry("Murtala Muhammed Way, Benin City", "Benin City", 6.3411, 5.6382, listOf("murtala muhammed", "mm way", "ikpoba")),
        AddressEntry("Lucky Way (Lucky Igbinedion Way), Ikpoba Hill, Benin City", "Benin City", 6.3429, 5.6347, listOf("lucky way", "lucky igbinedion")),
        AddressEntry("Upper Mission Extension, Aduwawa, Benin City", "Benin City", 6.3712, 5.6482, listOf("upper mission extension", "aduwawa")),

        // =============================================================
        // SAPELE ROAD & UPPER SAKPONBA AXIS
        // =============================================================
        AddressEntry("Sapele Road, Benin City", "Benin City", 6.3271, 5.6219, listOf("sapele road", "sapele")),
        AddressEntry("Stella Obasanjo Women & Children Hospital, Sapele Road, Benin City", "Benin City", 6.3112, 5.6389, listOf("stella obasanjo", "hospital", "sapele road")),
        AddressEntry("Sapele Road Bypass Junction, Benin City", "Benin City", 6.2891, 5.6421, listOf("sapele bypass", "bypass junction", "bypass")),
        AddressEntry("Upper Sakponba Road, Benin City", "Benin City", 6.3365, 5.6232, listOf("upper sakponba", "sakponba", "sakponba road")),
        AddressEntry("Dumez Road, Upper Sakponba, Benin City", "Benin City", 6.3212, 5.6410, listOf("dumez", "dumez road", "upper sakponba")),
        AddressEntry("St. Saviour Road, Upper Sakponba, Benin City", "Benin City", 6.3289, 5.6455, listOf("st saviour", "saint saviour", "sakponba")),
        AddressEntry("Erediauwa Street, Off Upper Sakponba / Sapele Road, Benin City", "Benin City", 6.3195, 5.6322, listOf("erediauwa", "erediauwa street")),
        AddressEntry("Evbuotubu Community, Ekenwan / Upper Siluko, Benin City", "Benin City", 6.3981, 5.6411, listOf("evbuotubu", "community")),

        // =============================================================
        // EKENWAN & WESTERN BENIN
        // =============================================================
        AddressEntry("Ekenwan Road (UNIBEN Ekenwan Campus), Benin City", "Benin City", 6.3311, 5.6104, listOf("ekenwan road", "ekenwan", "uniben ekenwan")),
        AddressEntry("Trans-Ekehuan Road, Benin City", "Benin City", 6.3201, 5.6028, listOf("trans-ekehuan", "ekehuan", "trans ekehuan road")),
        AddressEntry("Ekenwan Military Barracks, Ekenwan Road, Benin City", "Benin City", 6.3188, 5.6012, listOf("barracks", "ekenwan barracks", "military"))
    )

    /**
     * Search addresses within Benin City.
     */
    fun searchItems(query: String, maxResults: Int = 8): List<com.esdispatch.utils.SearchResultItem> {
        return search(query, maxResults).map { it.toSearchResult() }
    }

    fun search(query: String, maxResults: Int = 8): List<AddressEntry> {
        if (query.isBlank()) return getDefaults()
        val q = query.lowercase().trim()
        val expanded = expandTypos(q)

        val scored = entries.mapNotNull { entry ->
            val name = entry.displayName.lowercase()
            val tags = entry.tags.joinToString(" ")
            val combined = "$name $tags"

            var score = 0
            if (name.startsWith(q)) score += 100
            if (expanded != q && name.startsWith(expanded)) score += 90
            if (combined.contains(q)) score += 60
            if (expanded != q && combined.contains(expanded)) score += 50

            val queryWords = q.split(" ", ",").filter { it.length >= 3 }
            for (word in queryWords) {
                if (combined.contains(word)) score += 20
            }
            for (tag in entry.tags) {
                if (levenshtein(q, tag) <= 2) score += 15
            }

            if (score > 0) Pair(entry, score) else null
        }

        return scored.sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    fun getDefaults(): List<AddressEntry> {
        return listOf(
            entries.first { it.displayName.contains("Airport", ignoreCase = true) },
            entries.first { it.displayName.contains("University of Benin (UNIBEN), Ugbowo", ignoreCase = true) },
            entries.first { it.displayName.contains("Ring Road", ignoreCase = true) },
            entries.first { it.displayName.contains("Oba's Palace", ignoreCase = true) },
            entries.first { it.displayName.contains("Ihama Road", ignoreCase = true) },
            entries.first { it.displayName.contains("UBTH", ignoreCase = true) },
            entries.first { it.displayName.contains("New Benin Market", ignoreCase = true) },
            entries.first { it.displayName.contains("Ramat Park", ignoreCase = true) }
        )
    }

    fun isBeninCity(address: String): Boolean {
        val a = address.lowercase()
        return a.contains("benin") || a.contains("edo") ||
               a.contains("ugbowo") || a.contains("uselu") || a.contains("ikpoba") ||
               a.contains("akpakpava") || a.contains("sapele") || a.contains("ugbor") ||
               a.contains("forestry") || a.contains("ring road") ||
               a.contains("mission") || a.contains("uniben") ||
               a.contains("ubth") || a.contains("aduwawa") || a.contains("gra") ||
               a.contains("new benin") || a.contains("old benin") || a.contains("palace") ||
               a.contains("ekehuan") || a.contains("ekenwan") || a.contains("siluko") ||
               a.contains("sakponba") || a.contains("ihama") || a.contains("airport road") ||
               a.contains("bni") || a.contains("ramat")
    }

    fun isLagos(address: String): Boolean {
        val a = address.lowercase()
        return a.contains("lagos") || a.contains("lekki") || a.contains("victoria island") ||
               a.contains("ikoyi") || a.contains("ikeja") || a.contains("surulere") ||
               a.contains("yaba") || a.contains("apapa") || a.contains("festac") ||
               a.contains("gbagada") || a.contains("maryland") || a.contains("oshodi") ||
               a.contains("ajah") || a.contains("sangotedo") || a.contains("mushin")
    }

    fun getCoordinates(address: String): Pair<Double, Double>? {
        val a = address.lowercase()
        return entries.firstOrNull { entry ->
            val name = entry.displayName.lowercase()
            name.contains(a.take(20)) || a.contains(entry.displayName.lowercase().take(20)) ||
            entry.tags.any { tag -> a.contains(tag) }
        }?.let { Pair(it.lat, it.lng) }
    }

    private fun expandTypos(query: String): String {
        val typoMap = mapOf(
            "airpt" to "airport", "arpt" to "airport", "airpot" to "airport",
            "mll" to "mall", "maket" to "market", "mkt" to "market",
            "benin cty" to "benin city", "benincity" to "benin city", "bnin" to "benin",
            "uniben" to "university of benin", "univ" to "university",
            "hosp" to "hospital", "hos" to "hospital", "ubth" to "uniben teaching hospital",
            "gra" to "gra", "govt" to "government", "sec" to "secretariat",
            "rd" to "road", "st" to "street", "ave" to "avenue",
            "sapelle" to "sapele", "ugbowo" to "ugbowo", "uyaro" to "iyaro",
            "rng rd" to "ring road", "rngs" to "ring road", "akpakpawa" to "akpakpava",
            "sakpoba" to "sakponba", "ekehuan" to "ekenwan"
        )
        var result = query
        for ((typo, fix) in typoMap) {
            if (result.contains(typo)) result = result.replace(typo, fix)
        }
        return result
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
        }
        return dp[s1.length][s2.length]
    }
}
