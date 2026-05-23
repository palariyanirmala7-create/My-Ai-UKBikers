package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.local.RideDatabase
import com.example.data.model.BikeType
import com.example.data.model.MountainDriver
import com.example.data.model.MountainLocation
import com.example.data.model.RideBooking
import com.example.data.model.RideDataProvider
import com.example.data.repository.RideRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class FareResult(
    val distanceKm: Double,
    val baseFare: Double,
    val distanceFare: Double,
    val altitudeSurcharge: Double,
    val totalFare: Double,
    val estMinutes: Int,
    val elevationGain: Int,
    val hazardMultiplier: Double
)

enum class BookingState {
    IDLE,
    CALCULATING,
    SEARCHING_DRIVER,
    ACTIVE_SIMULATION,
    COMPLETED
}

class UKBikersViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RideDatabase.getDatabase(application)
    private val repository = RideRepository(db.rideDao())

    // UI state flows
    val allBookings: StateFlow<List<RideBooking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBookings: StateFlow<List<RideBooking>> = repository.activeBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selection States
    private val _pickupLocation = MutableStateFlow<MountainLocation?>(null)
    val pickupLocation = _pickupLocation.asStateFlow()

    private val _dropoffLocation = MutableStateFlow<MountainLocation?>(null)
    val dropoffLocation = _dropoffLocation.asStateFlow()

    private val _selectedBike = MutableStateFlow<BikeType>(RideDataProvider.bikeTypes[0])
    val selectedBike = _selectedBike.asStateFlow()

    // Calculated Fare Info
    private val _fareResult = MutableStateFlow<FareResult?>(null)
    val fareResult = _fareResult.asStateFlow()

    // Active Ride States
    private val _bookingState = MutableStateFlow(BookingState.IDLE)
    val bookingState = _bookingState.asStateFlow()

    private val _activeDriver = MutableStateFlow<MountainDriver?>(null)
    val activeDriver = _activeDriver.asStateFlow()

    private val _activeRideId = MutableStateFlow<Long?>(null)
    val activeRideId = _activeRideId.asStateFlow()

    // Animation progress for simulated map (0.0f - 1.0f)
    private val _rideProgress = MutableStateFlow(0f)
    val rideProgress = _rideProgress.asStateFlow()

    private val _rideStatusText = MutableStateFlow("Preparing trip parameters...")
    val rideStatusText = _rideStatusText.asStateFlow()

    private val _rideCurrentAltitude = MutableStateFlow(1940)
    val rideCurrentAltitude = _rideCurrentAltitude.asStateFlow()

    // Weather/Environmental bulletins rotation
    private val _activeAlertIndex = MutableStateFlow(0)
    val activeAlertIndex = _activeAlertIndex.asStateFlow()

    // AI Companion Assistant States
    private val _aiResponse = MutableStateFlow("")
    val aiResponse = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading = _isAiLoading.asStateFlow()

    init {
        // Initialize with default choices (Nainital: Lake -> Snow View)
        _pickupLocation.value = RideDataProvider.locations[0] // Naini Lake
        _dropoffLocation.value = RideDataProvider.locations[3] // Snow View
        calculateFare()

        // Start rotating mountain weather alert bulletins for tourism safety guidance
        viewModelScope.launch {
            while (true) {
                delay(12000)
                _activeAlertIndex.value = (_activeAlertIndex.value + 1) % RideDataProvider.environmentalBulletins.size
            }
        }
    }

    fun setLocations(pickup: MountainLocation?, dropoff: MountainLocation?) {
        _pickupLocation.value = pickup
        _dropoffLocation.value = dropoff
        calculateFare()
    }

    fun selectBike(bike: BikeType) {
        _selectedBike.value = bike
        calculateFare()
    }

    fun triggerBulletinsIndex(index: Int) {
        _activeAlertIndex.value = index
    }

    private fun calculateFare() {
        val pickup = _pickupLocation.value ?: return
        val dropoff = _dropoffLocation.value ?: return
        val bike = _selectedBike.value

        // Distance math based on relative coordinates
        val dx = pickup.xRatio - dropoff.xRatio
        val dy = pickup.yRatio - dropoff.yRatio
        // Distances in our coordinate system are factored to feel realistic
        val multiplier = if (pickup.isNainital && dropoff.isNainital) {
            12.0 // Short Nainital hill jumps
        } else if (!pickup.isNainital && !dropoff.isNainital) {
            15.0 // Ramnagar Safari Highway runs
        } else {
            32.0 // Cross mountain road (Nainital to Ramnagar Kaladhungi pass!)
        }
        val distanceKm = (sqrt((dx * dx + dy * dy).toDouble()) * multiplier).coerceIn(1.5, 45.0)

        // Elevation change surcharge (mountain roads tax the motor and fuel!)
        val elevationDiff = dropoff.altitudeMeters - pickup.altitudeMeters
        val positiveClimb = if (elevationDiff > 0) elevationDiff else 0
        // Surcharge calculation: 0.15 INR per meter of vertical ascent
        val altitudeSurcharge = positiveClimb * 0.15

        // Hazard multiplier based on elevations or bike limits
        val hazardMultiplier = if (bike.id == "standard" && positiveClimb > 300) {
            1.2 // Low torque surcharge for standard commuter struggling up steep pass
        } else {
            1.0
        }

        val baseFare = bike.baseFare
        val distanceFare = distanceKm * bike.perKmRate
        val totalFare = ((baseFare + distanceFare + altitudeSurcharge) * hazardMultiplier).roundTo(2)

        // Slow speed for hilly bends (approx 20-30 km/h)
        val minutesPerKm = if (positiveClimb > 250) 3.5 else 2.5
        val estMinutes = (distanceKm * minutesPerKm).coerceAtLeast(5.0).roundToInt()

        _fareResult.value = FareResult(
            distanceKm = distanceKm.roundTo(1),
            baseFare = baseFare,
            distanceFare = distanceFare.roundTo(2),
            altitudeSurcharge = altitudeSurcharge.roundTo(2),
            totalFare = totalFare,
            estMinutes = estMinutes,
            elevationGain = elevationDiff,
            hazardMultiplier = hazardMultiplier
        )
    }

    // Interactive ride booking & simulation
    fun requestBikerRide() {
        val pickup = _pickupLocation.value ?: return
        val dropoff = _dropoffLocation.value ?: return
        val fareRes = _fareResult.value ?: return
        val bike = _selectedBike.value

        viewModelScope.launch {
            _bookingState.value = BookingState.SEARCHING_DRIVER
            // Simulate searching driver in mountains
            delay(2500)

            // Select a compatible driver depending on locations
            val driver = if (!pickup.isNainital) {
                // Ramnagar Driver (Safari Specialist)
                RideDataProvider.mountainDrivers[1] // Devesh Palariya
            } else if (fareRes.elevationGain > 300) {
                // Steep Climbs legend
                RideDataProvider.mountainDrivers[0] // Rajesh Negi (Hairpin King)
            } else {
                RideDataProvider.mountainDrivers[2] // Manish Bhandari
            }

            _activeDriver.value = driver

            // Insert into local Room database to maintain complete tracking
            val booking = RideBooking(
                pickupName = pickup.name,
                dropoffName = dropoff.name,
                bikeType = bike.name,
                status = "Active",
                fare = fareRes.totalFare,
                distanceKm = fareRes.distanceKm,
                elevationChangeMeters = fareRes.elevationGain,
                driverName = driver.name,
                driverRating = driver.rating,
                driverPhone = driver.phoneNumber,
                driverImageUrl = driver.badge
            )
            val id = repository.insertBooking(booking)
            _activeRideId.value = id

            // Transition to Simulation layer
            _bookingState.value = BookingState.ACTIVE_SIMULATION
            _rideProgress.value = 0f
            runActiveRideSimulation(pickup, dropoff, fareRes)
        }
    }

    private fun runActiveRideSimulation(pickup: MountainLocation, dropoff: MountainLocation, fare: FareResult) {
        viewModelScope.launch {
            val totalSteps = 20
            val initialAlt = pickup.altitudeMeters
            val finalAlt = dropoff.altitudeMeters
            val climbRange = finalAlt - initialAlt

            for (i in 0..totalSteps) {
                val progress = i.toFloat() / totalSteps
                _rideProgress.value = progress

                // Calculate ongoing simulated altitude
                _rideCurrentAltitude.value = (initialAlt + progress * climbRange).roundToInt()

                // High fidelity update text simulating actual progress through curvy Himalayan trails
                _rideStatusText.value = when {
                    progress == 0f -> "Driver ${activeDriver.value?.name} has arrived with your certified helmet. Commencing climb from ${pickup.name}..."
                    progress < 0.25f -> {
                        if (climbRange > 150) "Navigating starting hairpin loops. Shifting to low gear for torque..."
                        else "Sailing down smoothly. Lean dynamically on the sweeping road bends."
                    }
                    progress in 0.25f..0.5f -> {
                        if (!pickup.isNainital) "Jungle canopy shade on safari routes. Spotting wild birds, scanning road sides."
                        else "Gaining elevation. Temperature dropping by ${((_rideCurrentAltitude.value - initialAlt) * 0.0065).roundTo(1)}°C. Splendid vistas!"
                    }
                    progress in 0.5f..0.75f -> {
                        if (climbRange > 300) "Successfully traversed the famous Kaladhungi hairpins. Elevation is ${_rideCurrentAltitude.value} meters!"
                        else "Cruising cleanly around the Naini ridge curves. Moderate wind gusts detected."
                    }
                    progress < 1.0f -> "Aproaching destination zone. Safely decelerating on terminal downhill slopes..."
                    else -> "Arrived at ${dropoff.name}! Enjoy Uttarakhand!"
                }

                delay(1200) // update simulation step every 1.2s (total simulated transit: ~24s)
            }

            // Once complete, update booking status in database to Completed
            _activeRideId.value?.let { id ->
                val bookings = allBookings.value
                val item = bookings.find { it.id == id }
                if (item != null) {
                    repository.updateBooking(item.copy(status = "Completed"))
                }
            }

            _bookingState.value = BookingState.COMPLETED
            delay(3000)
            _bookingState.value = BookingState.IDLE
            _activeDriver.value = null
            _activeRideId.value = null
        }
    }

    fun cancelActiveRide() {
        viewModelScope.launch {
            _activeRideId.value?.let { id ->
                val bookings = allBookings.value
                val item = bookings.find { it.id == id }
                if (item != null) {
                    repository.updateBooking(item.copy(status = "Cancelled"))
                }
            }
            _bookingState.value = BookingState.IDLE
            _activeDriver.value = null
            _activeRideId.value = null
            _rideProgress.value = 0f
        }
    }

    // AI Mount Guide Ask Flow
    fun askMountainGuide(question: String) {
        if (question.isBlank()) return
        _isAiLoading.value = true
        _aiResponse.value = ""

        viewModelScope.launch(Dispatchers.IO) {
            val systemInstructions = """
                You are "UKBikers AI Mountain Guide", an energetic, safety-focused, extremely helpful local travel advisor for Nainital and Ramnagar, Uttarakhand, India.
                Your purpose is to assist tourists and locals in selecting safe, efficient, and appropriate bike travel choices on steep, curvy Himalayan roads.
                Provide very practical local knowledge, safety warnings, and terrain insights.
                Specifically know about:
                - Nainital Lakes (Naini, Bhimtal, Sattal), Snow View Point, Dorothy Seat, Kaladhungi Road.
                - Ramnagar gateways (Dhikala, Bijrani), Corbett Tiger Reserve safari regulations, elephant crossings, Kosi river rapids.
                - Safety directives: Sounding horns on blind bends, lowering gears on descents, avoiding late night forested highways since Ramnagar roads are elephant corridors, fog precautions.
                Deliver output clearly, concisely (max 3 short paragraphs), and in a friendly, conversational helper tone. Don't use overly medical or dry jargon, be a native mountain buddy!
            """.trimIndent()

            val answer = GeminiClient.askGemini(question, systemInstructions)
            withContext(Dispatchers.Main) {
                _aiResponse.value = answer
                _isAiLoading.value = false
            }
        }
    }

    // Utility: clear db history
    fun clearDatabaseHistory() {
        viewModelScope.launch {
            repository.cancelAllActiveBookings()
            _bookingState.value = BookingState.IDLE
            _activeDriver.value = null
            _activeRideId.value = null
        }
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10.0 }
        return (this * multiplier).roundToInt() / multiplier
    }

    private fun Float.roundTo(decimals: Int): Float {
        var multiplier = 1.0f
        repeat(decimals) { multiplier *= 10.0f }
        return (this * multiplier).roundToInt() / multiplier
    }
}
