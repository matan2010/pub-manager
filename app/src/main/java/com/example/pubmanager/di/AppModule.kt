package com.example.pubmanager.di

import android.content.Context
import androidx.room.Room
import com.example.pubmanager.data.dao.EmailDao
import com.example.pubmanager.data.dao.EventDao
import com.example.pubmanager.data.dao.FamilyDao
import com.example.pubmanager.data.dao.OrderDao
import com.example.pubmanager.data.dao.ProductDao
import com.example.pubmanager.data.db.AppDatabase
import com.example.pubmanager.domain.repository.EmailRepository
import com.example.pubmanager.domain.repository.EventRepository
import com.example.pubmanager.domain.repository.FamilyRepository
import com.example.pubmanager.domain.repository.OrderRepository
import com.example.pubmanager.domain.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pubmanager.db"
        ).build()
    }

    @Provides
    fun provideProductDao(
        db: AppDatabase
    ): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao
    ): ProductRepository = ProductRepository(productDao)

    @Provides
    fun provideEmailDao(
        db: AppDatabase
    ): EmailDao = db.emailDao()

    @Provides
    @Singleton
    fun provideEmailRepository(
        emailDao: EmailDao
    ): EmailRepository = EmailRepository(emailDao)

    @Provides
    fun provideFamilyDao(
        db: AppDatabase
    ): FamilyDao = db.familyDao()

    @Provides
    @Singleton
    fun provideFamilyRepository(
        familyDao: FamilyDao
    ): FamilyRepository = FamilyRepository(familyDao)

    @Provides
    fun provideEventDao(db: AppDatabase): EventDao = db.eventDao()

    @Provides
    fun provideEventRepository(
        eventDao: EventDao
    ): EventRepository = EventRepository(eventDao)

    @Provides
    fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    @Singleton
    fun provideOrderRepository(orderDao: OrderDao): OrderRepository =
        OrderRepository(orderDao)
}