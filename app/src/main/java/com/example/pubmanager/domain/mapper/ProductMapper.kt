package com.example.pubmanager.domain.mapper

import com.example.pubmanager.data.model.Product
import com.example.pubmanager.ui.products.ProductUi

fun Product.toUi(): ProductUi =
    ProductUi(
        id = this.id,
        name = this.name,
        price = this.price
    )

fun ProductUi.toEntity(): Product =
    Product(
        id = this.id,
        name = this.name,
        price = this.price
    )