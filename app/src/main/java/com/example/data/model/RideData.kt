package com.example.data.model

data class MountainLocation(
    val name: String,
    val description: String,
    val isNainital: Boolean,
    val altitudeMeters: Int,
    val xRatio: Float, // for mock map drawing (0 to 1)
    val yRatio: Float  // for mock map drawing (0 to 1)
)

data class BikeType(
    val id: String,
    val name: String,
    val description: String,
    val baseFare: Double,
    val perKmRate: Double,
    val maxClimbGrade: String, // e.g. "20% Steep climb"
    val iconName: String,
    val hasBackrest: Boolean
)

data class MountainDriver(
    val name: String,
    val phoneNumber: String,
    val rating: Float,
    val experienceYears: Int,
    val badge: String, // e.g. "Hairpin Legend", "Safari Navigator"
    val bikeNumber: String
)

object RideDataProvider {
    // Locations in Nainital & Ramnagar
    val locations = listOf(
        // Nainital Series (Alpine Altitude)
        MountainLocation("Naini Lake (Boating Point)", "Heart of Nainital. Lakeside boardwalk and entry to market.", true, 1938, 0.15f, 0.50f),
        MountainLocation("Tallital Bus Stand", "Primary gateway entry point into Nainital valley.", true, 1940, 0.20f, 0.85f),
        MountainLocation("Mall Road (Nainital Corner)", "Scenic vehicle-restricted evening walk strip.", true, 1942, 0.28f, 0.40f),
        MountainLocation("Snow View Point", "Accessed via extremely steep turns. Aerial panoramic Himalayan view.", true, 2275, 0.35f, 0.15f),
        MountainLocation("Eco Cave Gardens", "Adventure network of natural rocky mountain caves.", true, 2010, 0.10f, 0.25f),
        MountainLocation("Tiffin Top (Dorothy)", "Scenic pine forest sunset viewpoint. Challenging uphill.", true, 2292, 0.45f, 0.35f),
        MountainLocation("Bhimtal Lakeside", "Expansive scenic valley lake. Highway downhill route.", true, 1370, 0.65f, 0.75f),

        // Ramnagar Series (Forest & River)
        MountainLocation("Ramnagar Railway Station", "Gateway terminal for Jim Corbett Park visitors.", false, 345, 0.10f, 0.90f),
        MountainLocation("Dhikala Forest Gate", "Entry portal to prime Tiger Safari reserves.", false, 380, 0.80f, 0.10f),
        MountainLocation("Bijrani Safari Gateway", "Winding jungle lanes, safari boarding zone.", false, 360, 0.70f, 0.30f),
        MountainLocation("Garjiya Devi Temple", "Famous temple perched on a high boulder in Kosi river.", false, 410, 0.40f, 0.60f),
        MountainLocation("Kaladhungi Corbett Museum", "Quiet forest-hills transitional highway route.", false, 395, 0.60f, 0.80f)
    )

    // Hill Climbing Bike Fleets
    val bikeTypes = listOf(
        BikeType(
            id = "cruiser",
            name = "Mountain Cruise (Royal Enfield)",
            description = "High-displacement cruiser with secure pillion backrest. Perfect for tourists seeking scenic, secure comfort.",
            baseFare = 80.0,
            perKmRate = 18.0,
            maxClimbGrade = "18% Slope Grade",
            iconName = "motorcycle",
            hasBackrest = true
        ),
        BikeType(
            id = "electric",
            name = "Stellar EV Climber (Electric Dual-Motor)",
            description = "High-torque, silent, carbon-neutral electric bike suited beautifully for steep Himalayan hairpins.",
            baseFare = 50.0,
            perKmRate = 12.0,
            maxClimbGrade = "25% Extreme Grade",
            iconName = "bolt",
            hasBackrest = false
        ),
        BikeType(
            id = "standard",
            name = "Himalayan Commuter (Hero Xpulse)",
            description = "High ground clearance, dual-purpose adventure bike. Nimble, economical, ideal for local daily shortcuts.",
            baseFare = 35.0,
            perKmRate = 8.5,
            maxClimbGrade = "15% Standard Grade",
            iconName = "directions_bike",
            hasBackrest = false
        )
    )

    // Experienced local drivers
    val mountainDrivers = listOf(
        MountainDriver("Rajesh Negi", "+91 98765 01010", 4.9f, 8, "Hairpin King - 10k Hills Ascents", "UK-04-A-1234"),
        MountainDriver("Devesh Palariya", "+91 94521 02214", 4.8f, 5, "Corbett Explorer - Wildlife Savvy", "UK-04-B-8521"),
        MountainDriver("Manish Bhandari", "+91 88775 51100", 4.95f, 12, "Rescue Rider - Landslip Safe certified", "UK-04-C-4477"),
        MountainDriver("Aman Bisht", "+91 70512 88990", 4.75f, 3, "Electric Pioneer - Silent Green Pilot", "UK-04-E-5241")
    )

    // Dynamic warning bulletins simulating Uttarakhand road conditions
    val environmentalBulletins = listOf(
        "Landslip alert: Slow movement near Kaladhungi - Ramnagar slide zones. Bikes taking detour.",
        "Monsoon Mist: Heavy fog developing on Haldwani-Nainital steep climbs. Headlights ON.",
        "Wildlife Alert: Forest department warns of wild elephant herds near Dhikala gateway corridor.",
        "Lake Crowds: Tallital bottleneck busy. UKBikers taking secondary forest ridge walk bypass.",
        "Perfect Riding Sun: Mountain skies clear near Snow View. Ideal visibility."
    )
}
