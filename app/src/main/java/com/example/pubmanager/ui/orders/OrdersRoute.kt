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
    eventName: String,
    eventDateText: String,
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
        eventId = eventId,
        eventName = eventName,
        eventDateText = eventDateText,

        families = families,
        products = products,
        orders = orders,

        emails = emails,

        onBackClick = onBackClick,

        onSaveNewOrder = { familyId, quantitiesByProduct ->
            viewModel.addToFamilyOrder(eventId, familyId, quantitiesByProduct)
        },

        onUpdateOrder = { familyId, quantitiesByProduct ->
            viewModel.setFamilyOrder(eventId, familyId, quantitiesByProduct)
        },

        onDeleteOrder = { familyId ->
            viewModel.deleteFamilyOrder(eventId, familyId)
        },

        onSendSelectedEmails = { toEmails, excelBytes, attachmentName, subject, text,onResult ->
            viewModel.sendEmailToSelected(
                toEmails = toEmails,
                excelBytes = excelBytes,
                attachmentName = attachmentName,
                subject = subject,
                text = text
            ) { msg ->
                onResult(msg)
            }
        }
    )
}


