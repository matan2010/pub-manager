package com.example.pubmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pubmanager.data.model.Email

@Dao
interface EmailDao {
    @Insert
    suspend fun insertEmail(email: Email): Long

    @Query("SELECT * FROM emails")
    suspend fun getAllEmails(): List<Email>

    @Update
    suspend fun updateEmail(email: Email)

    @Query("DELETE FROM emails WHERE id = :emailId")
    suspend fun deleteEmailById(emailId: Long)

    @Delete
    suspend fun deleteEmail(email: Email)
}