package com.example.pubmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pubmanager.data.model.Order

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: Order): Long

    @Query("SELECT * FROM orders")
    suspend fun getAllOrders(): List<Order>

    @Update
    suspend fun updateOrder(order: Order)

    @Query("""
    SELECT * 
    FROM orders
    WHERE eventId = :eventId
    ORDER BY familyId ASC, productId ASC
""")
    suspend fun getOrdersForEvent(eventId: Long): List<Order>


    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("DELETE FROM orders WHERE eventId = :eventId AND familyId = :familyId")
    suspend fun deleteOrdersForFamilyInEvent(eventId: Long, familyId: Long)

    @Query("""
        SELECT *
        FROM orders
        WHERE eventId = :eventId AND familyId = :familyId
    """)
    suspend fun getOrdersForFamilyInEvent(eventId: Long, familyId: Long): List<Order>

}