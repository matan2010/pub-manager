package com.example.pubmanager.ui.orders

import com.example.pubmanager.ui.common.HasId

data class OrderUi(
    override val id: Long,
    val eventId: Long,
    val familyId: Long,
    val productId: Long,
    val quantity: Int
) : HasId