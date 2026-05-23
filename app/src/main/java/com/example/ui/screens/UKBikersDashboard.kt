package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BikeType
import com.example.data.model.MountainLocation
import com.example.data.model.RideBooking
import com.example.data.model.MountainDriver
import com.example.data.model.RideDataProvider
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.LakeTealSecondary
import com.example.ui.theme.SafetySaffronAmber
import com.example.ui.theme.MutedSlateText
import com.example.ui.theme.ErrorSienna
import com.example.ui.viewmodel.BookingState
import com.example.ui.viewmodel.UKBikersViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UKBikersDashboard(
    viewModel: UKBikersViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Viewmodel States
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val pickup by viewModel.pickupLocation.collectAsStateWithLifecycle()
    val dropoff by viewModel.dropoffLocation.collectAsStateWithLifecycle()
    val selectedBike by viewModel.selectedBike.collectAsStateWithLifecycle()
    val fareRes by viewModel.fareResult.collectAsStateWithLifecycle()
    val bookingState by viewModel.bookingState.collectAsStateWithLifecycle()
    val driver by viewModel.activeDriver.collectAsStateWithLifecycle()
    val rideProgress by viewModel.rideProgress.collectAsStateWithLifecycle()
    val rideStatusText by viewModel.rideStatusText.collectAsStateWithLifecycle()
    val rideAltitude by viewModel.rideCurrentAltitude.collectAsStateWithLifecycle()
    val activeAlertIndex by viewModel.activeAlertIndex.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Book Taxi, 1 = AI Tour Guide, 2 = History
    var regionFilterIsNainital by remember { mutableStateOf(true) } // true: Nainital, false: Ramnagar

    // Selection Dialog States
    var showPickupDialog by remember { mutableStateOf(false) }
    var showDropoffDialog by remember { mutableStateOf(false) }

    // Chat query state
    var aiQueryText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                // Main Branded App Bar
                LargeTopAppBar(
                    title = {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TwoWheeler,
                                    contentDescription = "UKBikers Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "UKBikers",
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = " Uttarakhand Bike Taxi Services",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(onClick = {
                            viewModel.triggerBulletinsIndex((activeAlertIndex + 1) % RideDataProvider.environmentalBulletins.size)
                        }) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "Tour Helper Info")
                        }
                    }
                )

                // Environmental / Weather Bulletins Moving Ticker Banner
                val alertText = RideDataProvider.environmentalBulletins[activeAlertIndex]
                AnimatedContent(
                    targetState = alertText,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() with slideOutVertically { height -> -height } + fadeOut()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafetySaffronAmber.copy(alpha = 0.15f))
                        .clickable {
                            viewModel.triggerBulletinsIndex((activeAlertIndex + 1) % RideDataProvider.environmentalBulletins.size)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    label = "BulletinTicker"
                ) { text ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Alert Symbol",
                            tint = SafetySaffronAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Next bulletin",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // M3 Tab Navigation Bar
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Filled.TwoWheeler, contentDescription = "Booking") },
                    label = { Text("Book Taxi") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Filled.Explore, contentDescription = "AI Guide") },
                    label = { Text("Mount-Guide AI") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Ride History") },
                    label = { Text("My Trips") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main views rendering depending on selected active tab
            when (activeTab) {
                0 -> BookTaxiTab(
                    bookingState = bookingState,
                    pickup = pickup,
                    dropoff = dropoff,
                    selectedBike = selectedBike,
                    fareRes = fareRes,
                    driver = driver,
                    rideProgress = rideProgress,
                    rideStatusText = rideStatusText,
                    rideAltitude = rideAltitude,
                    regionFilterIsNainital = regionFilterIsNainital,
                    onRegionChanged = { regionFilterIsNainital = it },
                    onRequestPickupClicked = { showPickupDialog = true },
                    onRequestDropoffClicked = { showDropoffDialog = true },
                    onBikeSelected = { viewModel.selectBike(it) },
                    onBookClicked = { viewModel.requestBikerRide() },
                    onCancelClicked = { viewModel.cancelActiveRide() }
                )
                1 -> AICompanionTab(
                    queryText = aiQueryText,
                    onQueryChange = { aiQueryText = it },
                    response = aiResponse,
                    isLoading = isAiLoading,
                    onAskClicked = {
                        viewModel.askMountainGuide(aiQueryText)
                    },
                    onQuickQuerySelected = { query ->
                        aiQueryText = query
                        viewModel.askMountainGuide(query)
                    }
                )
                2 -> TripHistoryTab(
                    allBookings = allBookings,
                    onClearHistory = { viewModel.clearDatabaseHistory() }
                )
            }

            // Pickup Selection Dialog
            if (showPickupDialog) {
                LocationSelectionDialog(
                    title = "Choose Pickup Point",
                    isNainitalRegion = regionFilterIsNainital,
                    currentSelected = pickup,
                    onSelect = {
                        viewModel.setLocations(it, dropoff)
                        showPickupDialog = false
                    },
                    onDismiss = { showPickupDialog = false }
                )
            }

            // Dropoff Selection Dialog
            if (showDropoffDialog) {
                LocationSelectionDialog(
                    title = "Choose Destination Point",
                    isNainitalRegion = regionFilterIsNainital,
                    currentSelected = dropoff,
                    onSelect = {
                        viewModel.setLocations(pickup, it)
                        showDropoffDialog = false
                    },
                    onDismiss = { showDropoffDialog = false }
                )
            }
        }
    }
}

// ==========================================
// RIDE BOOKING TAB SECTIONS
// ==========================================
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BookTaxiTab(
    bookingState: BookingState,
    pickup: MountainLocation?,
    dropoff: MountainLocation?,
    selectedBike: BikeType,
    fareRes: com.example.ui.viewmodel.FareResult?,
    driver: MountainDriver?,
    rideProgress: Float,
    rideStatusText: String,
    rideAltitude: Int,
    regionFilterIsNainital: Boolean,
    onRegionChanged: (Boolean) -> Unit,
    onRequestPickupClicked: () -> Unit,
    onRequestDropoffClicked: () -> Unit,
    onBikeSelected: (BikeType) -> Unit,
    onBookClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    AnimatedContent(
        targetState = bookingState,
        transitionSpec = {
            fadeIn() with fadeOut()
        },
        label = "BookingStateAnimation"
    ) { state ->
        when (state) {
            BookingState.IDLE, BookingState.CALCULATING -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    item {
                        // Title/Introduction Section
                        Text(
                            text = "Navigate Himalayan Inclines Safely",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Hail local mountain biker experts equipped with safety gear and terrain fluency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedSlateText
                        )
                    }

                    item {
                        // Area Sector Filter Choice
                        RegionSelector(
                            isNainitalSelected = regionFilterIsNainital,
                            onRegionSelected = onRegionChanged
                        )
                    }

                    item {
                        // Direct Destination Setup
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Pickup field
                                LocationTile(
                                    label = "Pickup Location in " + if (regionFilterIsNainital) "Nainital" else "Ramnagar",
                                    selectedLocation = pickup,
                                    onClick = onRequestPickupClicked,
                                    isPickup = true
                                )

                                Divider(
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                )

                                // Dropoff field
                                LocationTile(
                                    label = "Dropoff Destination in " + if (regionFilterIsNainital) "Nainital" else "Ramnagar",
                                    selectedLocation = dropoff,
                                    onClick = onRequestDropoffClicked,
                                    isPickup = false
                                )
                            }
                        }
                    }

                    item {
                        // Bike Selection Strip title
                        Text(
                            text = "Select Biking Fleet Suited For Grade",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Horizontal selector Scroll list for mountain bike fleets
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(RideDataProvider.bikeTypes) { bike ->
                                BikeTypeCard(
                                    bike = bike,
                                    isSelected = bike.id == selectedBike.id,
                                    onSelect = { onBikeSelected(bike) }
                                )
                            }
                        }
                    }

                    if (fareRes != null) {
                        item {
                            // High Fidelity Fare breakdown billing Card
                            FareEstimateSummary(
                                fareRes = fareRes,
                                bike = selectedBike
                            )
                        }

                        item {
                            // Primary Ride Hail request Button
                            Button(
                                onClick = onBookClicked,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("request_ride_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.TwoWheeler, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "BOOK RIDE - ₹${fareRes.totalFare}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Select different pickup & dropoff to fetch fare estimates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            BookingState.SEARCHING_DRIVER -> {
                // Interactive loading panel while matching driver in forest areas
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 6.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Default.ShareLocation,
                            contentDescription = null,
                            tint = SafetySaffronAmber,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        "Pinging Ride Network...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Searching for nearby mountain expert riders between ${pickup?.name ?: "origin"} and ${dropoff?.name ?: "dropoff"}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlateText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Local Precaution: Standard hill-climbs have blind turns. UKBikers check helmet lock and rider speed constraints automatically.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            BookingState.ACTIVE_SIMULATION, BookingState.COMPLETED -> {
                // Active Ride screen featuring Canvas graphics, real progress bars and altitudes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (state == BookingState.ACTIVE_SIMULATION) "Ride In Progress" else "Arrived Safely!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Curvy Scenic Mountain Road Canvas drawing
                    MountainRoadCanvas(progress = rideProgress)

                    // Realistic elevation & distance tracking Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Current Altitude", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                                Text("$rideAltitude m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("Himalayan slope", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Progress State", style = MaterialTheme.typography.labelSmall, color = MutedSlateText)
                                Text("${(rideProgress * 100).roundToInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SafetySaffronAmber)
                                Text("${fareRes?.distanceKm ?: 0.0} km total", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }

                    // Status text description
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state == BookingState.ACTIVE_SIMULATION) Color.Green else Color.Gray)
                            )
                            Text(
                                text = rideStatusText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Diver and safety card
                    if (driver != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Person,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = driver.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = SafetySaffronAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                "${driver.rating} • ${driver.experienceYears} Years Exp",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            driver.bikeNumber,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            driver.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = LakeTealSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Simulated call action button
                                    Button(
                                        onClick = { /* Simulated Call */ },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call Driver", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                    }

                                    // Pillion Helmet confirmed indicator
                                    Button(
                                        onClick = { },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Helmet Locked", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (state == BookingState.ACTIVE_SIMULATION) {
                        OutlinedButton(
                            onClick = onCancelClicked,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("cancel_ride_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorSienna),
                            border = BorderStroke(1.dp, ErrorSienna)
                        ) {
                            Text("CANCEL ACTIVE RIDE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Region filter segment picker selector
@Composable
fun RegionSelector(
    isNainitalSelected: Boolean,
    onRegionSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isNainitalSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onRegionSelected(true) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Nainital Hills 🏔️",
                fontWeight = FontWeight.Bold,
                color = if (isNainitalSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (!isNainitalSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onRegionSelected(false) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ramnagar Safari 🐅",
                fontWeight = FontWeight.Bold,
                color = if (!isNainitalSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

// Location Selector Tiles styling
@Composable
fun LocationTile(
    label: String,
    selectedLocation: MountainLocation?,
    onClick: () -> Unit,
    isPickup: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = if (isPickup) Icons.Filled.MyLocation else Icons.Filled.LocationOn,
            contentDescription = null,
            tint = if (isPickup) LakeTealSecondary else SafetySaffronAmber,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlateText
            )
            Text(
                text = selectedLocation?.name ?: "Tap to choose location...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (selectedLocation != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selectedLocation != null) {
                Text(
                    text = "${selectedLocation.altitudeMeters}m altitude • ${selectedLocation.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            Icons.Default.UnfoldMore,
            contentDescription = "Expand list",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

// Horizontal card template for selecting bikes
@Composable
fun BikeTypeCard(
    bike: BikeType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (bike.id) {
                        "cruiser" -> Icons.Filled.TwoWheeler
                        "electric" -> Icons.Filled.ElectricBike
                        else -> Icons.Filled.DirectionsBike
                    },
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )

                // High torque steep label markers
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SafetySaffronAmber.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        bike.maxClimbGrade,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                bike.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                bike.description,
                style = MaterialTheme.typography.labelSmall,
                color = MutedSlateText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp,
                modifier = Modifier.height(26.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Min: ₹${bike.baseFare.roundToInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    "₹${bike.perKmRate}/km",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

// Surcharge and altitude multiplier summary display card
@Composable
fun FareEstimateSummary(
    fareRes: com.example.ui.viewmodel.FareResult,
    bike: BikeType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Fare Breakdown & Transit Specs",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Altitude Change Surcharge", style = MaterialTheme.typography.bodySmall, color = MutedSlateText)
                Text(
                    if (fareRes.elevationGain > 0) "+₹${fareRes.altitudeSurcharge} (${fareRes.elevationGain}m up)"
                    else "₹0.00 (Flattish/Downhill ride)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (fareRes.elevationGain > 0) SafetySaffronAmber else Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Base Distance Fare (${fareRes.distanceKm} km)", style = MaterialTheme.typography.bodySmall, color = MutedSlateText)
                Text("₹${fareRes.distanceFare}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            if (fareRes.hazardMultiplier > 1.0) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Steep Climb Gear Multiplier", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                    Text("x${fareRes.hazardMultiplier}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }

            Divider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.3f))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Fare (Incl. Tax)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Est Transit Time: ~${fareRes.estMinutes} mins", style = MaterialTheme.typography.labelSmall, color = LakeTealSecondary, fontWeight = FontWeight.Bold)
                }
                Text(
                    "₹${fareRes.totalFare}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Vector mountain drawing inside active ride simulation
@Composable
fun MountainRoadCanvas(progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = ForestGreenDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw beautiful hill backdrop silhouette gradient
            val hillPath1 = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * 0.4f)
                quadraticTo(w * 0.3f, h * 0.2f, w * 0.6f, h * 0.5f)
                quadraticTo(w * 0.8f, h * 0.6f, w, h * 0.3f)
                lineTo(w, h)
                close()
            }
            drawPath(
                path = hillPath1,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF042E25), Color(0xFF0C1312))
                )
            )

            // 2. Winding dynamic curvy mountain road path
            val roadPath = Path().apply {
                moveTo(w * 0.1f, h * 0.85f)
                cubicTo(
                    w * 0.4f, h * 0.95f, // hairpin bend bottom
                    w * 0.2f, h * 0.45f, // sharp loop middle left
                    w * 0.5f, h * 0.5f   // midpoint flat
                )
                cubicTo(
                    w * 0.8f, h * 0.55f, // loop right
                    w * 0.6f, h * 0.15f, // top incline
                    w * 0.9f, h * 0.2f   // final peak
                )
            }

            // Draw thick road base black backing
            drawPath(
                path = roadPath,
                color = Color(0xFF2E3E3B),
                style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw center safety dash-dotted lane separator lines on hill pass
            drawPath(
                path = roadPath,
                color = SafetySaffronAmber,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 15f), 0f)
                )
            )

            // 3. Mark Start/End Viewpoint icons
            drawCircle(color = LakeTealSecondary, radius = 9f, center = Offset(w * 0.1f, h * 0.85f))
            drawCircle(color = SafetySaffronAmber, radius = 9f, center = Offset(w * 0.9f, h * 0.2f))

            // 4. Calculate dynamic bike nodes coordinates sliding on Bezier curves according to progress Float
            // We parameterize the Bezier calculations to cleanly slide node from (w*0.1, h*0.85) to (w*0.9, h*0.2)
            val bikeX: Float
            val bikeY: Float

            if (progress <= 0.5f) {
                // First cubic segment
                val t = progress / 0.5f
                val p0 = Offset(w * 0.1f, h * 0.85f)
                val p1 = Offset(w * 0.4f, h * 0.95f)
                val p2 = Offset(w * 0.2f, h * 0.45f)
                val p3 = Offset(w * 0.5f, h * 0.5f)

                bikeX = (1-t)*(1-t)*(1-t)*p0.x + 3*(1-t)*(1-t)*t*p1.x + 3*(1-t)*t*t*p2.x + t*t*t*p3.x
                bikeY = (1-t)*(1-t)*(1-t)*p0.y + 3*(1-t)*(1-t)*t*p1.y + 3*(1-t)*t*t*p2.y + t*t*t*p3.y
            } else {
                // Second cubic segment
                val t = (progress - 0.5f) / 0.5f
                val p0 = Offset(w * 0.5f, h * 0.5f)
                val p1 = Offset(w * 0.8f, h * 0.55f)
                val p2 = Offset(w * 0.6f, h * 0.15f)
                val p3 = Offset(w * 0.9f, h * 0.2f)

                bikeX = (1-t)*(1-t)*(1-t)*p0.x + 3*(1-t)*(1-t)*t*p1.x + 3*(1-t)*t*t*p2.x + t*t*t*p3.x
                bikeY = (1-t)*(1-t)*(1-t)*p0.y + 3*(1-t)*(1-t)*t*p1.y + 3*(1-t)*t*t*p2.y + t*t*t*p3.y
            }

            // Draw a pulsing beacon representing the active motorcycle traveling Uttarakhand passes
            drawCircle(
                color = Color.White,
                radius = 18f,
                center = Offset(bikeX, bikeY)
            )

            drawCircle(
                color = SafetySaffronAmber,
                radius = 11f,
                center = Offset(bikeX, bikeY)
            )
        }
    }
}

// ==========================================
// AI COMPANION TOUR GUIDE SECTIONS
// ==========================================
@Composable
fun AICompanionTab(
    queryText: String,
    onQueryChange: (String) -> Unit,
    response: String,
    isLoading: Boolean,
    onAskClicked: () -> Unit,
    onQuickQuerySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "UKBikers Mount-Guide AI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            "Ask our real-time AI assistant for road conditions, wildlife safari cautions, steep pass shortcuts, or weather status in Uttarakhand Nainital and Ramnagar.",
            style = MaterialTheme.typography.bodySmall,
            color = MutedSlateText
        )

        // Predefined rapid queries suggestions
        Text("Common safety queries:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "Is Kaladhungi road safe in rain?",
                "Saddle safety on 20% inclines?",
                "Avoid elephants near Dhikala?",
                "Best sunset points Nainital?"
            )
            suggestions.forEach { s ->
                SuggestionChip(
                    onClick = { onQuickQuerySelected(s) },
                    label = { Text(s, fontSize = 11.sp) }
                )
            }
        }

        // Search Ask console
        OutlinedTextField(
            value = queryText,
            onValueChange = onQueryChange,
            placeholder = { Text("Ask about Nainital/Ramnagar biking routes...", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_query_input"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3,
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onAskClicked, enabled = queryText.isNotBlank()) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "Submit",
                            tint = if (queryText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        )

        if (response.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_response_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            "Local Mountain AI Insights",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = response,
                        style = androidx.compose.ui.text.TextStyle(
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Quick warning notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = LakeTealSecondary, modifier = Modifier.size(16.dp))
                Text(
                    text = "AI Guide insights are helpful advisories. Never ride beyond personal safety comfort levels.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// FlowRow alternative helper since experimental flows might require exact SDK config
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Basic Layout fallback using double Column/Row wrapping to guarantee compilation across any compose versions
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val xGap = 8.dp.roundToPx()
        val yGap = 8.dp.roundToPx()

        var currentX = 0
        var currentY = 0
        var rowHeight = 0

        val placeables = measurables.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val positions = mutableListOf<Offset>()

        placeables.forEach { placeable ->
            if (currentX + placeable.width > constraints.maxWidth) {
                currentX = 0
                currentY += rowHeight + yGap
                rowHeight = 0
            }
            positions.add(Offset(currentX.toFloat(), currentY.toFloat()))
            currentX += placeable.width + xGap
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        layout(
            width = constraints.maxWidth,
            height = if (positions.isEmpty()) 0 else (currentY + rowHeight)
        ) {
            placeables.forEachIndexed { index, placeable ->
                val pos = positions[index]
                placeable.placeRelative(pos.x.roundToInt(), pos.y.roundToInt())
            }
        }
    }
}

// ==========================================
// TRIP BOOKINGS PERSISTENCE HISTORY TAB
// ==========================================
@Composable
fun TripHistoryTab(
    allBookings: List<RideBooking>,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Mountain Rides Ledger",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (allBookings.isNotEmpty()) {
                TextButton(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorSienna)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (allBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.SentimentVeryDissatisfied,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No trips recorded yet",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Book your first UKBikers mountain taxicab to view your history checklist here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlateText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(allBookings) { booking ->
                    HistoryItemCard(booking = booking)
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(booking: RideBooking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.bikeType,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                // Colored status indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (booking.status) {
                                "Completed" -> Color.Green.copy(alpha = 0.15f)
                                "Cancelled" -> Color.Red.copy(alpha = 0.15f)
                                else -> SafetySaffronAmber.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = booking.status,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (booking.status) {
                            "Completed" -> ForestGreenPrimary
                            "Cancelled" -> ErrorSienna
                            else -> Color(0xFFC46A00)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pickup dropoff points
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = LakeTealSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.pickupName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SafetySaffronAmber, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.dropoffName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Driver: ${booking.driverName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Rating: ★ ${booking.driverRating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${booking.fare}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${booking.distanceKm} km • " + if (booking.elevationChangeMeters > 0) "+${booking.elevationChangeMeters}m" else "Flat",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// ==========================================
// LOCATION POPUP DIALOGS SELECTORS
// ==========================================
@Composable
fun LocationSelectionDialog(
    title: String,
    isNainitalRegion: Boolean,
    currentSelected: MountainLocation?,
    onSelect: (MountainLocation) -> Unit,
    onDismiss: () -> Unit
) {
    // Filter coordinates with matching region choice
    val filteredLocations = RideDataProvider.locations.filter { it.isNainital == isNainitalRegion }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 350.dp)
            ) {
                Text(
                    text = "Showing points tailored perfectly for " + (if (isNainitalRegion) "Nainital elevations" else "Ramnagar Reserve gate rules") + ":",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(filteredLocations) { loc ->
                        val isPicked = loc.name == currentSelected?.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(loc) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPicked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = loc.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${loc.altitudeMeters}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LakeTealSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = loc.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
