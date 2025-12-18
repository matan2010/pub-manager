package com.example.pubmanager.ui.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pubmanager.ui.families.FamilyUi
import com.example.pubmanager.ui.products.ProductUi

@Composable
fun OrdersRoute(
    viewModel: OrdersViewModel,
    eventId: Long,
    families: List<FamilyUi>,
    products: List<ProductUi>,
    onBackClick: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val emails by viewModel.emails.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadOrders(eventId)
        viewModel.loadEmails()
    }

    OrdersScreen(
        orders = orders,
        eventId = eventId,
        families = families,
        products = products,

        emails = emails,
        onSendEmails = { selectedEmails ->

        },

        onBackClick = onBackClick,

        onSaveNewOrder = { familyId, quantitiesByProduct ->
            viewModel.addToFamilyOrder(eventId, familyId, quantitiesByProduct)
        },

        onUpdateOrder = { familyId, quantitiesByProduct ->
            viewModel.setFamilyOrder(eventId, familyId, quantitiesByProduct)
        },

        onDeleteOrder = { familyId ->
            viewModel.deleteFamilyOrder(eventId, familyId)
        }
    )
}
