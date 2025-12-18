package com.example.pubmanager.domain.mapper

import com.example.pubmanager.data.model.Order
import com.example.pubmanager.ui.orders.OrderUi

fun Order.toUi(): OrderUi =
    OrderUi(
        id = id,
        eventId = eventId,
        familyId = familyId,
        productId = productId,
        quantity = quantity
    )

fun OrderUi.toEntity(): Order =
    Order(
        id = id,
        eventId = eventId,
        familyId = familyId,
        productId = productId,
        quantity = quantity
    )