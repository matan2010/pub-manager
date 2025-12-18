package com.example.pubmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pubmanager.data.model.Product

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(product: Product): Long

    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<Product>

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: Long)

    @Delete
    suspend fun deleteProduct(product: Product)
}