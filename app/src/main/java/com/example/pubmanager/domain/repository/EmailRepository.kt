package com.example.pubmanager.domain.repository

import com.example.pubmanager.data.dao.EmailDao
import com.example.pubmanager.data.model.Email

class EmailRepository(
    private val emailDao: EmailDao
) {
    suspend fun insertEmail(email: Email): Long {
        return emailDao.insertEmail(email)
    }

    suspend fun getAllEmails(): List<Email> {
        return emailDao.getAllEmails()
    }

    suspend fun deleteEmail(email: Email) {
        emailDao.deleteEmail(email)
    }

    suspend fun updateEmail(email: Email) {
        emailDao.updateEmail(email)
    }
}