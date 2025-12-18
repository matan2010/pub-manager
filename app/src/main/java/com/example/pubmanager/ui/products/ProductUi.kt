package com.example.pubmanager.ui.products

import com.example.pubmanager.ui.common.HasId

data class ProductUi (
    override val id: Long,
    val name: String,
    val price: Double
) : HasId