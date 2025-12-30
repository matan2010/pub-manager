package com.example.pubmanager.ui.emails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Email
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.EmailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailsViewModel @Inject constructor(
    private val repository: EmailRepository
) : ViewModel() {

    private val _emails = MutableStateFlow<List<EmailUi>>(emptyList())
    val emails: StateFlow<List<EmailUi>> = _emails.asStateFlow()

    init {
        loadEmails()
    }

    fun loadEmails() {
        viewModelScope.launch {
            val dbEmails = repository.getAllEmails()
            _emails.value = dbEmails.map { it.toUi() }
        }
    }

    fun addEmail(address: String) {
        viewModelScope.launch {
            repository.insertEmail(Email(email = address))
            loadEmails()
        }
    }

    fun updateEmail(id: Long, address: String) {
        viewModelScope.launch {
            repository.updateEmail(Email(id = id, email = address))
            loadEmails()
        }
    }

    fun deleteEmail(id: Long) {
        viewModelScope.launch {
            val uiEmail = _emails.value.firstOrNull { it.id == id } ?: return@launch
            repository.deleteEmail(Email(id = uiEmail.id, email = uiEmail.email))
            loadEmails()
        }
    }
}