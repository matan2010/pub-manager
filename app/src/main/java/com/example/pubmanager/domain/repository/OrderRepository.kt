package com.example.pubmanager.domain.repository

import com.example.pubmanager.data.dao.OrderDao
import com.example.pubmanager.data.model.Event
import com.example.pubmanager.data.model.Order

class OrderRepository(
    private val orderDao: OrderDao
) {

    suspend fun addOrder(
        eventId: Long,
        familyId: Long,
        productId: Long,
        quantity: Int
    ): Long {
        val order = Order(
            id = 0L,
            eventId = eventId,
            familyId = familyId,
            productId = productId,
            quantity = quantity
        )
        return orderDao.insertOrder(order)
    }

    suspend fun insertOrder(order: Order): Long {
        return orderDao.insertOrder(order)
    }

    suspend fun getOrdersForEvent(eventId: Long): List<Order> {
        return orderDao.getOrdersForEvent(eventId)
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }

    suspend fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order)
    }

    suspend fun getOrdersForFamilyInEvent(eventId: Long, familyId: Long): List<Order> =
        orderDao.getOrdersForFamilyInEvent(eventId, familyId)

    suspend fun deleteOrdersForFamilyInEvent(eventId: Long, familyId: Long) {
        orderDao.deleteOrdersForFamilyInEvent(eventId, familyId)
    }

}