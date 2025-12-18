package com.example.pubmanager.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pubmanager.data.model.Family

@Dao
interface FamilyDao {
    @Insert
    suspend fun insertFamily(family: Family): Long

    @Query("""
    SELECT * FROM families
    ORDER BY lastName ASC, firstName ASC
""")
    suspend fun getAllFamilies(): List<Family>

    @Update
    suspend fun updateFamily(family: Family)

    @Query("DELETE FROM families WHERE id = :familyId")
    suspend fun deleteFamilyById(familyId: Long)

    @Delete
    suspend fun deleteFamily(family: Family)
}