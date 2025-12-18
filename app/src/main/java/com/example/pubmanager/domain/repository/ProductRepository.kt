package com.example.pubmanager.domain.repository

import com.example.pubmanager.data.dao.ProductDao
import com.example.pubmanager.data.model.Product

class ProductRepository(
    private val productDao: ProductDao
) {

    suspend fun insertProduct(product: Product): Long {
        return productDao.insertProduct(product)
    }

    suspend fun getAllProducts(): List<Product> {
        return productDao.getAllProducts()
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product)
    }
}