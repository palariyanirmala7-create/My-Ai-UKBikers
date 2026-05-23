package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RideBooking
import kotlinx.coroutines.flow.Flow

@Dao
interface RideDao {
    @Query("SELECT * FROM ride_bookings ORDER BY bookedAt DESC")
    fun getAllBookings(): Flow<List<RideBooking>>

    @Query("SELECT * FROM ride_bookings WHERE status = 'Active' ORDER BY bookedAt DESC")
    fun getActiveBookings(): Flow<List<RideBooking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: RideBooking): Long

    @Update
    suspend fun updateBooking(booking: RideBooking)

    @Delete
    suspend fun deleteBooking(booking: RideBooking)

    @Query("UPDATE ride_bookings SET status = 'Cancelled' WHERE status = 'Active'")
    suspend fun cancelAllActiveBookings()
}
