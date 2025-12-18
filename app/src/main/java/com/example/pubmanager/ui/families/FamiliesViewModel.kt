package com.example.pubmanager.ui.families

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Family
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.FamilyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FamiliesViewModel @Inject constructor(
    private val repository: FamilyRepository
) : ViewModel() {
    private val _families = MutableStateFlow<List<FamilyUi>>(emptyList())
    val families: StateFlow<List<FamilyUi>> = _families.asStateFlow()

    init {
        loadFamilies()
    }

    fun loadFamilies() {
        viewModelScope.launch {
            val dbFamilies = repository.getAllFamilies()
            _families.value = dbFamilies.map { it.toUi() }
        }
    }

    fun addFamily(firstName: String, lastName: String) {
        viewModelScope.launch {
            val family = Family(
                firstName = firstName,
                lastName = lastName
            )
            repository.insertFamily(family)
            loadFamilies()
        }
    }

    fun updateFamily(id: Long, firstName: String, lastName: String) {
        viewModelScope.launch {
            val family = Family(
                id = id,
                firstName = firstName,
                lastName = lastName
            )
            repository.updateFamily(family)
            loadFamilies()
        }
    }

    fun deleteFamily(id: Long) {
        viewModelScope.launch {
            val uiFamily = _families.value.firstOrNull { it.id == id } ?: return@launch
            val family = Family(
                id = uiFamily.id,
                firstName = uiFamily.firstName,
                lastName = uiFamily.lastName
            )
            repository.deleteFamily(family)
            loadFamilies()
        }
    }
}