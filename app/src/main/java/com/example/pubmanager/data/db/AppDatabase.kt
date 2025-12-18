package com.example.pubmanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pubmanager.data.dao.EmailDao
import com.example.pubmanager.data.dao.EventDao
import com.example.pubmanager.data.dao.FamilyDao
import com.example.pubmanager.data.dao.OrderDao
import com.example.pubmanager.data.dao.ProductDao
import com.example.pubmanager.data.model.Email
import com.example.pubmanager.data.model.Event
import com.example.pubmanager.data.model.Family
import com.example.pubmanager.data.model.Order
import com.example.pubmanager.data.model.Product

@Database(
    entities = [Event::class, Family::class, Product::class, Order::class, Email::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun productDao(): ProductDao
    abstract fun emailDao(): EmailDao
    abstract fun familyDao(): FamilyDao
    abstract fun orderDao(): OrderDao
}
