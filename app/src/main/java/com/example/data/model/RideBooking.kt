package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ride_bookings")
data class RideBooking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pickupName: String,
    val dropoffName: String,
    val bikeType: String,
    val status: String, // Active, Completed, Cancelled
    val fare: Double,
    val distanceKm: Double,
    val elevationChangeMeters: Int,
    val bookedAt: Long = System.currentTimeMillis(),
    val driverName: String,
    val driverRating: Float,
    val driverPhone: String,
    val driverImageUrl: String = ""
)
