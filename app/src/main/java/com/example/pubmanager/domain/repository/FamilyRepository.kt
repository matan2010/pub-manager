package com.example.pubmanager.domain.repository

import com.example.pubmanager.data.dao.FamilyDao
import com.example.pubmanager.data.model.Family

class FamilyRepository(
    private val familyDao: FamilyDao
) {

    suspend fun insertFamily(family: Family): Long {
        return familyDao.insertFamily(family)
    }

    suspend fun getAllFamilies(): List<Family> {
        return familyDao.getAllFamilies()
    }

    suspend fun deleteFamily(family: Family) {
        familyDao.deleteFamily(family)
    }

    suspend fun updateFamily(family: Family) {
        familyDao.updateFamily(family)
    }
}