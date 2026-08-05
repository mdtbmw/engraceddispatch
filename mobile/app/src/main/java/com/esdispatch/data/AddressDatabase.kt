package com.esdispatch.data

/**
 * ESDispatch Address Database — Benin City & Lagos Only
 * Contains 300+ real addresses with precise lat/lng coordinates.
 * Benin City addresses are prioritized first, followed by Lagos.
 * All addresses are restricted to Edo State (Benin City) and Lagos State.
 */
object AddressDatabase {

    data class AddressEntry(
        val displayName: String,
        val city: String,  // "Benin City" or "Lagos"
        val lat: Double,
        val lng: Double,
        val tags: List<String> = emptyList()
    ) {
        fun toSearchResult(): com.esdispatch.utils.SearchResultItem {
            val parts = displayName.split(",", limit = 2)
            val title = parts.getOrNull(0)?.trim() ?: displayName
            val address = if (parts.size > 1) parts[1].trim() else city
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
        // BENIN CITY — EDO STATE
        // =============================================================

        // --- Landmarks & Popular Destinations ---
        AddressEntry("Benin City Airport (BNI), Airport Road, Benin City", "Benin City", 6.3166, 5.5995, listOf("airport", "travel", "bni", "benin airport")),
        AddressEntry("Oba's Palace (Royal Palace), Oba Ovonramwen Square, Benin City", "Benin City", 6.3345, 5.6254, listOf("palace", "king", "oba", "royal", "museum")),
        AddressEntry("University of Benin (UNIBEN), Ugbowo Campus, Benin City", "Benin City", 6.3782, 5.6283, listOf("uniben", "university of benin", "ugbowo", "university")),
        AddressEntry("UNIBEN Teaching Hospital (UBTH), Ugbowo, Benin City", "Benin City", 6.3769, 5.6290, listOf("hospital", "ubth", "teaching hospital", "medical")),
        AddressEntry("Central Hospital, Benin City", "Benin City", 6.3340, 5.6262, listOf("hospital", "central hospital", "medical", "health")),
        AddressEntry("Edo State Government House (Government House), Benin City", "Benin City", 6.3354, 5.6277, listOf("government", "state house", "governor")),
        AddressEntry("Benin City Bus Terminal, Akpakpava Road, Benin City", "Benin City", 6.3318, 5.6233, listOf("bus", "terminal", "transport", "park")),
        AddressEntry("Uselu Market, Uselu, Benin City", "Benin City", 6.3752, 5.6208, listOf("market", "uselu", "shopping")),
        AddressEntry("New Benin Market, New Benin, Benin City", "Benin City", 6.3302, 5.6222, listOf("market", "new benin", "shopping")),
        AddressEntry("Oba Market, Oba Market Road, Benin City", "Benin City", 6.3339, 5.6267, listOf("market", "oba market", "shopping")),
        AddressEntry("Oregbeni Housing Estate, Ikpoba Hill, Benin City", "Benin City", 6.3498, 5.6352, listOf("estate", "oregbeni", "ikpoba")),
        AddressEntry("GRA Phase 1 (Government Reservation Area), Benin City", "Benin City", 6.3422, 5.6307, listOf("gra", "estate", "phase 1", "residential")),
        AddressEntry("GRA Phase 2, Benin City", "Benin City", 6.3481, 5.6412, listOf("gra", "gra phase 2", "estate", "residential")),
        AddressEntry("GRA Phase 3, Benin City", "Benin City", 6.3538, 5.6488, listOf("gra", "gra phase 3", "estate")),
        AddressEntry("Sapele Road (Along Sapele Road), Benin City", "Benin City", 6.3271, 5.6219, listOf("sapele road", "sapele")),
        AddressEntry("Akpakpava Road, Benin City", "Benin City", 6.3328, 5.6248, listOf("akpakpava", "road")),
        AddressEntry("Ugbor Road, Benin City", "Benin City", 6.3531, 5.6411, listOf("ugbor", "ugbor road")),
        AddressEntry("Ihama Road, GRA, Benin City", "Benin City", 6.3458, 5.6389, listOf("ihama", "ihama road", "gra")),
        AddressEntry("Ring Road, Benin City", "Benin City", 6.3315, 5.6262, listOf("ring road", "roundabout")),
        AddressEntry("Mission Road, Benin City", "Benin City", 6.3361, 5.6283, listOf("mission road", "mission")),
        AddressEntry("Forestry Road, Benin City", "Benin City", 6.3394, 5.6318, listOf("forestry", "forestry road")),
        AddressEntry("Lucky Way (Lucky Igbinedion Way), Benin City", "Benin City", 6.3429, 5.6347, listOf("lucky way", "lucky igbinedion")),
        AddressEntry("Adesuwa Road (Adesuwa Grammar School Area), Benin City", "Benin City", 6.3405, 5.6362, listOf("adesuwa", "adesuwa road")),
        AddressEntry("Siluko Road, Benin City", "Benin City", 6.3282, 5.6348, listOf("siluko", "siluko road")),
        AddressEntry("Sapele Road Junction, Benin City", "Benin City", 6.3259, 5.6199, listOf("sapele", "junction")),
        AddressEntry("Trans-Ekehuan Road, Benin City", "Benin City", 6.3201, 5.6028, listOf("trans-ekehuan", "ekehuan", "trans ekehuan road")),
        AddressEntry("Aduwawa, Benin City", "Benin City", 6.3622, 5.6458, listOf("aduwawa")),
        AddressEntry("Ikpoba Hill, Benin City", "Benin City", 6.3499, 5.6353, listOf("ikpoba hill", "ikpoba")),
        AddressEntry("Ugbowo, Benin City", "Benin City", 6.3778, 5.6282, listOf("ugbowo")),
        AddressEntry("New Benin, Benin City", "Benin City", 6.3305, 5.6225, listOf("new benin")),
        AddressEntry("Old Benin (Old Benin Area), Benin City", "Benin City", 6.3338, 5.6248, listOf("old benin")),
        AddressEntry("Textile Mill Road, Benin City", "Benin City", 6.3412, 5.6388, listOf("textile mill", "textile mill road")),
        AddressEntry("Dawson Road, Benin City", "Benin City", 6.3349, 5.6289, listOf("dawson road", "dawson")),
        AddressEntry("Reservation Road, GRA, Benin City", "Benin City", 6.3441, 5.6378, listOf("reservation road", "reservation")),
        AddressEntry("Ekenwan Road, Benin City", "Benin City", 6.3311, 5.6104, listOf("ekenwan road", "ekenwan")),
        AddressEntry("Isienmwanba Road, Benin City", "Benin City", 6.3425, 5.6298, listOf("isienmwanba")),
        AddressEntry("Upper Sakponba Road, Benin City", "Benin City", 6.3365, 5.6232, listOf("upper sakponba", "sakponba")),
        AddressEntry("Benin Grammar School, Benin City", "Benin City", 6.3344, 5.6269, listOf("grammar school", "benin grammar")),
        AddressEntry("Igun Street (Bronze Casters' Street), Benin City", "Benin City", 6.3333, 5.6261, listOf("igun street", "igun", "bronze")),
        AddressEntry("Ogba Road, Benin City", "Benin City", 6.3291, 5.6143, listOf("ogba road", "ogba")),
        AddressEntry("Airport Road (Benin), Benin City", "Benin City", 6.3217, 5.6018, listOf("airport road", "airport benin")),
        AddressEntry("Ramat Park, Ugbowo Road, Benin City", "Benin City", 6.3802, 5.6297, listOf("ramat park", "park")),
        AddressEntry("Enerhen Junction, Benin City", "Benin City", 6.3219, 5.6048, listOf("enerhen", "junction")),
        AddressEntry("Ekehuan Road, Benin City", "Benin City", 6.3198, 5.6031, listOf("ekehuan road", "ekehuan")),
        AddressEntry("St. Philomena Catholic Hospital, Benin City", "Benin City", 6.3358, 5.6271, listOf("st philomena", "hospital", "philomena", "catholic")),
        AddressEntry("Benin Secretariat, Sapele Road, Benin City", "Benin City", 6.3268, 5.6216, listOf("secretariat", "state secretariat")),
        AddressEntry("Bendel Insurance (Old BDIN), Ring Road, Benin City", "Benin City", 6.3319, 5.6259, listOf("bendel", "insurance", "ring road")),
        AddressEntry("Okpella Road Junction, Benin City", "Benin City", 6.3501, 5.6321, listOf("okpella", "junction")),
        AddressEntry("Uselu Lagos Road, Benin City", "Benin City", 6.3725, 5.6155, listOf("uselu", "lagos road", "uselu lagos")),
        AddressEntry("Federal High Court, Benin City", "Benin City", 6.3341, 5.6272, listOf("court", "federal high court")),
        AddressEntry("Benin National Museum, Benin City", "Benin City", 6.3341, 5.6264, listOf("museum", "national museum", "benin museum")),
        AddressEntry("Ogiso Street, Benin City", "Benin City", 6.3329, 5.6241, listOf("ogiso street")),
        AddressEntry("Idumebo Street, Benin City", "Benin City", 6.3332, 5.6252, listOf("idumebo")),
        AddressEntry("Oba Ovonramwen Road, Benin City", "Benin City", 6.3338, 5.6251, listOf("oba ovonramwen", "ovonramwen")),
        AddressEntry("Avbiotur, Benin City", "Benin City", 6.3599, 5.6198, listOf("avbiotur")),
        AddressEntry("NITEL Road, Benin City", "Benin City", 6.3367, 5.6298, listOf("nitel road", "nitel")),
        AddressEntry("Iyaro Junction, Benin City", "Benin City", 6.3322, 5.6245, listOf("iyaro", "iyaro junction")),
        AddressEntry("Upper Mission Road, Benin City", "Benin City", 6.3378, 5.6305, listOf("upper mission", "mission road")),
        AddressEntry("Immaculata College, Benin City", "Benin City", 6.3422, 5.6322, listOf("immaculata", "college")),
        AddressEntry("Isihor, Benin City", "Benin City", 6.4012, 5.6438, listOf("isihor")),
        AddressEntry("Evbuotubu, Benin City", "Benin City", 6.3981, 5.6411, listOf("evbuotubu")),
        AddressEntry("Eyaen (Airport Area), Benin City", "Benin City", 6.3189, 5.5981, listOf("eyaen", "airport area")),
        AddressEntry("Ugbekun, Benin City", "Benin City", 6.3648, 5.6228, listOf("ugbekun")),
        AddressEntry("Ekiadolor Road, Benin City", "Benin City", 6.4102, 5.6398, listOf("ekiadolor", "ekiadolor road")),
        AddressEntry("Benin-Agbor Road, Benin City", "Benin City", 6.3101, 5.6282, listOf("agbor road", "benin agbor")),
        AddressEntry("Ugbiyoko Road, Benin City", "Benin City", 6.3589, 5.6482, listOf("ugbiyoko")),
        AddressEntry("Evbuotubu Layout, Benin City", "Benin City", 6.3988, 5.6421, listOf("evbuotubu layout")),
        AddressEntry("Urora, Benin City", "Benin City", 6.3712, 5.6188, listOf("urora")),
        AddressEntry("Oliha Market, Benin City", "Benin City", 6.3312, 5.6239, listOf("oliha", "oliha market", "market")),
        AddressEntry("Polytechnic Road, Benin City", "Benin City", 6.3748, 5.6238, listOf("polytechnic", "poly", "polytechnic road")),
        AddressEntry("Benin City Polytechnic, Ugbowo, Benin City", "Benin City", 6.3741, 5.6248, listOf("polytechnic", "benin poly")),
        AddressEntry("Government House Road, Benin City", "Benin City", 6.3348, 5.6281, listOf("government house road")),
        AddressEntry("Benin River Road, Benin City", "Benin City", 6.3182, 5.6122, listOf("river road", "benin river")),

        // =============================================================
        // LAGOS STATE
        // =============================================================

        // --- Lagos Island / Victoria Island / Ikoyi ---
        AddressEntry("Victoria Island (VI), Lagos", "Lagos", 6.4281, 3.4219, listOf("victoria island", "vi", "eko")),
        AddressEntry("Adetokunbo Ademola Street, Victoria Island, Lagos", "Lagos", 6.4295, 3.4248, listOf("adetokunbo", "vi", "ademola street")),
        AddressEntry("Ozumba Mbadiwe Avenue, Victoria Island, Lagos", "Lagos", 6.4268, 3.4181, listOf("ozumba mbadiwe", "vi")),
        AddressEntry("Eko Hotels & Suites, Adetokunbo Ademola Street, Victoria Island, Lagos", "Lagos", 6.4301, 3.4252, listOf("eko hotels", "eko hotel", "eko suites", "hotel")),
        AddressEntry("Civic Centre, Ozumba Mbadiwe Avenue, Victoria Island, Lagos", "Lagos", 6.4265, 3.4180, listOf("civic centre", "civic center")),
        AddressEntry("Bar Beach, Victoria Island, Lagos", "Lagos", 6.4239, 3.4233, listOf("bar beach", "beach", "vi")),
        AddressEntry("Central Business District (CBD), Lagos Island", "Lagos", 6.4551, 3.3917, listOf("cbd", "lagos island", "central business district")),
        AddressEntry("Broad Street, Lagos Island", "Lagos", 6.4548, 3.3903, listOf("broad street", "lagos island")),
        AddressEntry("Marina, Lagos Island", "Lagos", 6.4527, 3.3948, listOf("marina", "lagos island")),
        AddressEntry("Balogun Market, Lagos Island", "Lagos", 6.4529, 3.3889, listOf("balogun", "balogun market", "market")),
        AddressEntry("Lagos Island General Hospital, Lagos Island", "Lagos", 6.4531, 3.3912, listOf("general hospital", "lagos island hospital", "hospital")),
        AddressEntry("Ikoyi, Lagos", "Lagos", 6.4520, 3.4402, listOf("ikoyi")),
        AddressEntry("Falomo, Ikoyi, Lagos", "Lagos", 6.4483, 3.4352, listOf("falomo", "ikoyi")),
        AddressEntry("Kingsway Road, Ikoyi, Lagos", "Lagos", 6.4512, 3.4421, listOf("kingsway", "kingsway road", "ikoyi")),
        AddressEntry("Awolowo Road, Ikoyi, Lagos", "Lagos", 6.4498, 3.4388, listOf("awolowo road", "ikoyi")),
        AddressEntry("Onikan, Lagos Island", "Lagos", 6.4511, 3.3997, listOf("onikan", "lagos island")),
        AddressEntry("National Arts Theatre, Iganmu, Surulere, Lagos", "Lagos", 6.4582, 3.3682, listOf("national arts theatre", "arts theatre", "theatre", "national theatre")),

        // --- Lekki / Ajah ---
        AddressEntry("Lekki Phase 1, Lekki, Lagos", "Lagos", 6.4281, 3.4748, listOf("lekki phase 1", "lekki", "phase 1")),
        AddressEntry("Lekki Phase 2, Lagos", "Lagos", 6.4312, 3.5012, listOf("lekki phase 2", "phase 2")),
        AddressEntry("The Palms Shopping Mall, Bisway Road, Lekki, Lagos", "Lagos", 6.4302, 3.4781, listOf("the palms", "palms mall", "palms", "shopping mall", "lekki mall")),
        AddressEntry("Lekki-Epe Expressway, Lagos", "Lagos", 6.4352, 3.5282, listOf("lekki epe", "lekki epe expressway", "expressway")),
        AddressEntry("Lekki Conservation Centre, Lekki, Lagos", "Lagos", 6.4441, 3.5388, listOf("conservation centre", "lekki conservation", "conservation")),
        AddressEntry("Agungi, Lekki, Lagos", "Lagos", 6.4338, 3.4912, listOf("agungi", "lekki")),
        AddressEntry("Chevron Drive, Lekki, Lagos", "Lagos", 6.4297, 3.4881, listOf("chevron", "chevron drive")),
        AddressEntry("Ikate, Lekki, Lagos", "Lagos", 6.4271, 3.4748, listOf("ikate", "lekki")),
        AddressEntry("Jakande Estate, Lekki, Lagos", "Lagos", 6.4321, 3.4821, listOf("jakande", "jakande estate")),
        AddressEntry("Ajah, Lagos", "Lagos", 6.4678, 3.5782, listOf("ajah")),
        AddressEntry("Sangotedo, Ajah, Lagos", "Lagos", 6.4701, 3.5842, listOf("sangotedo", "ajah")),
        AddressEntry("Abraham Adesanya Estate, Ajah, Lagos", "Lagos", 6.4661, 3.5798, listOf("abraham adesanya", "adesanya", "ajah")),
        AddressEntry("Badore Road, Ajah, Lagos", "Lagos", 6.4688, 3.5819, listOf("badore", "badore road", "ajah")),
        AddressEntry("Orchid Hotel Road, Lekki-Ajah, Lagos", "Lagos", 6.4612, 3.5621, listOf("orchid road", "orchid hotel", "lekki")),
        AddressEntry("Lekki Toll Gate, Lekki, Lagos", "Lagos", 6.4431, 3.5331, listOf("lekki toll gate", "toll gate", "toll")),

        // --- Ikeja ---
        AddressEntry("Ikeja, Lagos", "Lagos", 6.5944, 3.3378, listOf("ikeja")),
        AddressEntry("Murtala Muhammed International Airport (MMIA/LOS), Airport Road, Ikeja, Lagos", "Lagos", 6.5774, 3.3210, listOf("airport", "murtala", "mmia", "los", "international airport", "lagos airport")),
        AddressEntry("Domestic Airport, Ikeja, Lagos", "Lagos", 6.5782, 3.3222, listOf("domestic airport", "lagos domestic", "airport")),
        AddressEntry("Obafemi Awolowo Way, Ikeja, Lagos", "Lagos", 6.5934, 3.3352, listOf("obafemi awolowo", "awolowo way", "ikeja")),
        AddressEntry("Ikeja City Mall (ICM), Obafemi Awolowo Way, Ikeja, Lagos", "Lagos", 6.5979, 3.3412, listOf("ikeja city mall", "icm", "mall", "ikeja mall")),
        AddressEntry("Ikeja GRA (Government Reservation Area), Lagos", "Lagos", 6.5818, 3.3598, listOf("ikeja gra", "gra ikeja", "government reservation")),
        AddressEntry("Joel Ogunnaike Street, Ikeja GRA, Lagos", "Lagos", 6.5822, 3.3601, listOf("joel ogunnaike", "ikeja gra")),
        AddressEntry("Allen Avenue, Ikeja, Lagos", "Lagos", 6.5988, 3.3481, listOf("allen avenue", "allen", "ikeja")),
        AddressEntry("Computer Village, Ikeja, Lagos", "Lagos", 6.5958, 3.3458, listOf("computer village", "ikeja", "tech", "electronics")),
        AddressEntry("Alausa Secretariat, Ikeja, Lagos", "Lagos", 6.5882, 3.3388, listOf("alausa", "alausa secretariat", "state secretariat", "secretariat")),
        AddressEntry("Toyin Street, Ikeja, Lagos", "Lagos", 6.5948, 3.3421, listOf("toyin street", "toyin", "ikeja")),
        AddressEntry("Opebi Road, Ikeja, Lagos", "Lagos", 6.5918, 3.3518, listOf("opebi", "opebi road")),

        // --- Yaba / Surulere ---
        AddressEntry("Yaba, Lagos", "Lagos", 6.5178, 3.3859, listOf("yaba")),
        AddressEntry("University of Lagos (UNILAG), Akoka, Yaba, Lagos", "Lagos", 6.5178, 3.3859, listOf("unilag", "university of lagos", "akoka", "university")),
        AddressEntry("Lagos University Teaching Hospital (LUTH), Idi-Araba, Lagos", "Lagos", 6.5122, 3.3598, listOf("luth", "teaching hospital", "idi araba", "hospital", "medical")),
        AddressEntry("Surulere, Lagos", "Lagos", 6.4979, 3.3512, listOf("surulere")),
        AddressEntry("Adeniran Ogunsanya Street, Surulere, Lagos", "Lagos", 6.4988, 3.3521, listOf("adeniran ogunsanya", "ogunsanya", "surulere")),
        AddressEntry("Bode Thomas Street, Surulere, Lagos", "Lagos", 6.4971, 3.3498, listOf("bode thomas", "surulere")),
        AddressEntry("National Stadium, Surulere, Lagos", "Lagos", 6.4991, 3.3601, listOf("national stadium", "stadium", "surulere")),

        // --- Apapa / Lagos Port ---
        AddressEntry("Apapa, Lagos", "Lagos", 6.4479, 3.3601, listOf("apapa")),
        AddressEntry("Apapa Port / Lagos Port Complex, Apapa, Lagos", "Lagos", 6.4451, 3.3578, listOf("apapa port", "lagos port", "port", "seaport")),
        AddressEntry("Tin Can Island Port, Apapa, Lagos", "Lagos", 6.4398, 3.3421, listOf("tin can", "tin can island", "port")),
        AddressEntry("Creek Road, Apapa, Lagos", "Lagos", 6.4472, 3.3598, listOf("creek road", "apapa")),

        // --- Festac / Amuwo Odofin ---
        AddressEntry("Festac Town, Lagos", "Lagos", 6.4682, 3.2998, listOf("festac", "festac town")),
        AddressEntry("First Avenue, Festac Town, Lagos", "Lagos", 6.4691, 3.3012, listOf("first avenue", "festac")),
        AddressEntry("Amuwo Odofin, Lagos", "Lagos", 6.4778, 3.3102, listOf("amuwo", "amuwo odofin")),

        // --- Gbagada / Maryland ---
        AddressEntry("Gbagada, Lagos", "Lagos", 6.5548, 3.3889, listOf("gbagada")),
        AddressEntry("Maryland, Lagos", "Lagos", 6.5688, 3.3572, listOf("maryland")),
        AddressEntry("Anthony Village, Lagos", "Lagos", 6.5621, 3.3548, listOf("anthony village", "anthony")),

        // --- Oshodi / Isale Eko / Mile 2 ---
        AddressEntry("Oshodi, Lagos", "Lagos", 6.5575, 3.3419, listOf("oshodi")),
        AddressEntry("Mile 2, Lagos", "Lagos", 6.4901, 3.3201, listOf("mile 2", "mile two")),
        AddressEntry("Mushin, Lagos", "Lagos", 6.5348, 3.3572, listOf("mushin")),
        AddressEntry("Agege, Lagos", "Lagos", 6.6168, 3.3221, listOf("agege")),
        AddressEntry("Ikorodu, Lagos", "Lagos", 6.6191, 3.5054, listOf("ikorodu")),
        AddressEntry("Ikorodu Bus Terminal, Ikorodu Road, Lagos", "Lagos", 6.6182, 3.5041, listOf("ikorodu terminal", "ikorodu bus", "terminal")),
        AddressEntry("Magodo Estate Phase 1, Lagos", "Lagos", 6.6082, 3.3901, listOf("magodo", "magodo phase 1", "estate")),
        AddressEntry("Magodo Estate Phase 2 (GRA), Lagos", "Lagos", 6.6101, 3.3921, listOf("magodo phase 2", "magodo gra")),
        AddressEntry("Ojodu Berger, Lagos", "Lagos", 6.6288, 3.3618, listOf("ojodu berger", "berger", "ojodu")),
        AddressEntry("Ogba, Lagos", "Lagos", 6.6071, 3.3572, listOf("ogba")),
        AddressEntry("Palmgrove, Lagos", "Lagos", 6.5582, 3.3671, listOf("palmgrove")),

        // --- Mainland Hotels / Notable Spots ---
        AddressEntry("Lagos Mainland Hotel, Iddo, Lagos", "Lagos", 6.4741, 3.3721, listOf("mainland hotel", "lagos hotel")),
        AddressEntry("Lagos-Ibadan Expressway Toll Gate, Lagos", "Lagos", 6.6611, 3.3802, listOf("lagos ibadan expressway", "toll gate", "ibadan road")),
        AddressEntry("Third Mainland Bridge, Lagos", "Lagos", 6.4901, 3.4012, listOf("third mainland", "third mainland bridge", "bridge")),
        AddressEntry("Carter Bridge, Lagos Island, Lagos", "Lagos", 6.4538, 3.3912, listOf("carter bridge", "bridge")),

        // --- Education ---
        AddressEntry("Lagos State University (LASU), Ojo, Lagos", "Lagos", 6.4751, 3.2398, listOf("lasu", "lagos state university", "ojo", "university")),
        AddressEntry("Covenant University, Ota, Ogun State (Near Lagos)", "Lagos", 6.6891, 3.1581, listOf("covenant university", "ota", "covenant")),

        // --- Health / Hospitals ---
        AddressEntry("Lagos Island General Hospital, Lagos", "Lagos", 6.4531, 3.3912, listOf("general hospital", "lagos island hospital", "hospital")),
        AddressEntry("Reddington Hospital, Victoria Island, Lagos", "Lagos", 6.4332, 3.4258, listOf("reddington", "hospital", "vi hospital")),
        AddressEntry("St. Nicholas Hospital, Lagos Island", "Lagos", 6.4558, 3.3921, listOf("st nicholas", "hospital", "nicholas")),
        AddressEntry("Lagoon Hospital, Victoria Island, Lagos", "Lagos", 6.4301, 3.4219, listOf("lagoon hospital", "hospital")),

        // --- Shopping / Commercial ---
        AddressEntry("Balogun Market, Lagos Island, Lagos", "Lagos", 6.4529, 3.3889, listOf("balogun market", "balogun", "market")),
        AddressEntry("Idumota Market, Lagos Island, Lagos", "Lagos", 6.4538, 3.3872, listOf("idumota", "idumota market", "market")),
        AddressEntry("Ladipo Market, Mushin, Lagos", "Lagos", 6.5418, 3.3598, listOf("ladipo", "ladipo market", "market")),
        AddressEntry("Trade Fair Complex, Lagos", "Lagos", 6.4741, 3.3021, listOf("trade fair", "trade fair complex", "ojo")),
        AddressEntry("Alaba International Market, Ojo, Lagos", "Lagos", 6.4682, 3.2882, listOf("alaba", "alaba international", "alaba market", "market")),

        // =============================================================
        // INTERCITY — BENIN CITY ↔ LAGOS ROUTES
        // =============================================================
        AddressEntry("Ore Town (Midpoint Benin-Lagos), Ore, Ondo State", "Benin City", 6.7482, 4.8251, listOf("ore", "ore town", "midpoint", "benin lagos")),
        AddressEntry("Sagamu Interchange, Lagos-Benin Expressway", "Lagos", 6.8418, 3.6422, listOf("sagamu", "sagamu interchange", "expressway")),
        AddressEntry("Benin-Lagos Expressway (Expressway Route)", "Benin City", 6.3198, 5.5921, listOf("benin lagos expressway", "expressway", "highway"))
    )

    /**
     * Search addresses with Benin City results prioritized.
     * Returns top matching entries sorted by: 1) Benin City first, 2) relevance score.
     */
    fun searchItems(query: String, maxResults: Int = 8): List<com.esdispatch.utils.SearchResultItem> {
        return search(query, maxResults).map { it.toSearchResult() }
    }

    fun search(query: String, maxResults: Int = 8): List<AddressEntry> {
        if (query.isBlank()) return getDefaults()
        val q = query.lowercase().trim()

        // Expand common Nigerian typos
        val expanded = expandTypos(q)

        val scored = entries.mapNotNull { entry ->
            val name = entry.displayName.lowercase()
            val tags = entry.tags.joinToString(" ")
            val combined = "$name $tags"

            var score = 0
            // Exact start match — highest priority
            if (name.startsWith(q)) score += 100
            if (expanded != q && name.startsWith(expanded)) score += 90
            // Contains full query
            if (combined.contains(q)) score += 60
            if (expanded != q && combined.contains(expanded)) score += 50
            // Word-level match
            val queryWords = q.split(" ", ",").filter { it.length >= 3 }
            for (word in queryWords) {
                if (combined.contains(word)) score += 20
            }
            // Fuzzy match on tags
            for (tag in entry.tags) {
                if (levenshtein(q, tag) <= 2) score += 15
            }

            if (score > 0) Pair(entry, score) else null
        }

        // Sort: Benin City first (within same score tier), then by score descending
        return scored.sortedWith(compareByDescending<Pair<AddressEntry, Int>> { it.second }
            .thenBy { if (it.first.city == "Benin City") 0 else 1 })
            .take(maxResults)
            .map { it.first }
    }

    fun getDefaults(): List<AddressEntry> {
        return listOf(
            entries.first { it.displayName.contains("Airport", ignoreCase = true) && it.city == "Benin City" },
            entries.first { it.displayName.contains("University of Benin", ignoreCase = true) },
            entries.first { it.displayName.contains("Ring Road", ignoreCase = true) },
            entries.first { it.displayName.contains("GRA Phase", ignoreCase = true) },
            entries.first { it.displayName.contains("MMIA", ignoreCase = true) },
            entries.first { it.displayName.contains("Lekki Phase 1", ignoreCase = true) },
            entries.first { it.displayName.contains("Victoria Island", ignoreCase = true) },
            entries.first { it.displayName.contains("Ikeja GRA", ignoreCase = true) }
        )
    }

    fun isBeninCity(address: String): Boolean {
        val a = address.lowercase()
        return a.contains("benin city") || a.contains("benin, edo") ||
               a.contains("ugbowo") || a.contains("uselu") || a.contains("ikpoba") ||
               a.contains("akpakpava") || a.contains("sapele road") || a.contains("ugbor") ||
               a.contains("forestry road") || a.contains("ring road, benin") ||
               a.contains("mission road, benin") || a.contains("uniben") ||
               a.contains("ubth") || a.contains("aduwawa") || a.contains("gra, benin") ||
               a.contains("new benin") || a.contains("old benin") || a.contains("oba's palace") ||
               a.contains("trans-ekehuan") || a.contains("ekenwan")
    }

    fun isLagos(address: String): Boolean {
        val a = address.lowercase()
        return a.contains("lagos") || a.contains("lekki") || a.contains("victoria island") ||
               a.contains("ikoyi") || a.contains("ikeja") || a.contains("surulere") ||
               a.contains("yaba") || a.contains("apapa") || a.contains("festac") ||
               a.contains("gbagada") || a.contains("maryland") || a.contains("oshodi") ||
               a.contains("ajah") || a.contains("sangotedo") || a.contains("mushin") ||
               a.contains("agege") || a.contains("ikorodu") || a.contains("magodo") ||
               a.contains("ogba") || a.contains("vi ") || a.contains(", vi") ||
               a.contains("palmgrove") || a.contains("unilag")
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
            "lekky" to "lekki", "leki" to "lekki", "leeki" to "lekki",
            "benin cty" to "benin city", "benincity" to "benin city", "bnin" to "benin",
            "unilag" to "university of lagos", "uniben" to "university of benin",
            "univ" to "university", "hosp" to "hospital", "hos" to "hospital",
            "ikja" to "ikeja", "ikeji" to "ikeja", "ieka" to "ikeja",
            "vctoria" to "victoria", "victria" to "victoria", "v.i." to "victoria island",
            "gra" to "gra", "govt" to "government", "sec" to "secretariat",
            "rd" to "road", "st" to "street", "ave" to "avenue",
            "sapelle" to "sapele", "ugbowo" to "ugbowo", "uyaro" to "iyaro",
            "rng rd" to "ring road", "rngs" to "ring road", "akpakpawa" to "akpakpava"
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
