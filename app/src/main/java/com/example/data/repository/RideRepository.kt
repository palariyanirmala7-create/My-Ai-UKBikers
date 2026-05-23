package com.example.data.repository

import com.example.data.local.RideDao
import com.example.data.model.RideBooking
import kotlinx.coroutines.flow.Flow

class RideRepository(private val rideDao: RideDao) {
    val allBookings: Flow<List<RideBooking>> = rideDao.getAllBookings()
    val activeBookings: Flow<List<RideBooking>> = rideDao.getActiveBookings()

    suspend fun insertBooking(booking: RideBooking): Long {
        return rideDao.insertBooking(booking)
    }

    suspend fun updateBooking(booking: RideBooking) {
        rideDao.updateBooking(booking)
    }

    suspend fun cancelAllActiveBookings() {
        rideDao.cancelAllActiveBookings()
    }
}
