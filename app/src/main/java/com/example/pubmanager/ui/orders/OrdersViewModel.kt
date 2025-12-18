package com.example.pubmanager.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Order
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.EmailRepository
import com.example.pubmanager.domain.repository.OrderRepository
import com.example.pubmanager.ui.emails.EmailUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val emailRepository: EmailRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderUi>>(emptyList())
    val orders: StateFlow<List<OrderUi>> = _orders.asStateFlow()

    private val _emails = MutableStateFlow<List<EmailUi>>(emptyList())
    val emails: StateFlow<List<EmailUi>> = _emails.asStateFlow()

    fun loadOrders(eventId: Long) {
        viewModelScope.launch {
            val dbOrders = repository.getOrdersForEvent(eventId)
            _orders.value = dbOrders.map { it.toUi() }
        }
    }

    fun loadEmails() {
        viewModelScope.launch {
            val dbEmails = emailRepository.getAllEmails()
            _emails.value = dbEmails.map { it.toUi() }
        }
    }

    fun addToFamilyOrder(eventId: Long, familyId: Long, quantitiesToAdd: Map<Long, Int>) {
        viewModelScope.launch {
            val existing = repository
                .getOrdersForFamilyInEvent(eventId, familyId)
                .associate { it.productId to it.quantity }

            val merged: Map<Long, Int> =
                (existing.keys + quantitiesToAdd.keys).associateWith { productId ->
                    val oldQty = existing[productId] ?: 0
                    val addQty = quantitiesToAdd[productId] ?: 0
                    (oldQty + addQty).coerceAtLeast(0)
                }

            repository.deleteOrdersForFamilyInEvent(eventId, familyId)

            merged
                .filter { (_, qty) -> qty > 0 }
                .forEach { (productId, qty) ->
                    repository.insertOrder(
                        Order(
                            id = 0L,
                            eventId = eventId,
                            familyId = familyId,
                            productId = productId,
                            quantity = qty
                        )
                    )
                }

            loadOrders(eventId)
        }
    }

    fun setFamilyOrder(eventId: Long, familyId: Long, quantitiesSet: Map<Long, Int>) {
        viewModelScope.launch {
            repository.deleteOrdersForFamilyInEvent(eventId, familyId)

            quantitiesSet
                .filter { (_, qty) -> qty > 0 }
                .forEach { (productId, qty) ->
                    repository.insertOrder(
                        Order(
                            id = 0L,
                            eventId = eventId,
                            familyId = familyId,
                            productId = productId,
                            quantity = qty
                        )
                    )
                }

            loadOrders(eventId)
        }
    }

    fun deleteFamilyOrder(eventId: Long, familyId: Long) {
        viewModelScope.launch {
            repository.deleteOrdersForFamilyInEvent(eventId, familyId)
            loadOrders(eventId)
        }
    }
}
