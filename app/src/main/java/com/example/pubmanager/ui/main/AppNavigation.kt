package com.example.pubmanager.ui.main

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pubmanager.ui.emails.EmailsRoute
import com.example.pubmanager.ui.emails.EmailsViewModel
import com.example.pubmanager.ui.events.EventsRoute
import com.example.pubmanager.ui.events.EventsViewModel
import com.example.pubmanager.ui.families.FamiliesRoute
import com.example.pubmanager.ui.families.FamiliesViewModel
import com.example.pubmanager.ui.orders.OrdersRoute
import com.example.pubmanager.ui.orders.OrdersViewModel
import com.example.pubmanager.ui.products.ProductsRoute
import com.example.pubmanager.ui.products.ProductsViewModel
import com.example.pubmanager.ui.update.UpdateScreen
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.pubmanager.ui.common.rememberIsOnline
import com.example.pubmanager.ui.update.UpdateViewModel

private val EVENT_DATE_FORMATTER =
    DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val updateVm: UpdateViewModel = viewModel()
    val updateState by updateVm.state.collectAsState()

    val isOnline by rememberIsOnline()

    LaunchedEffect(Unit) {
        if (isOnline) updateVm.checkOnly()
    }

    Surface(color = MaterialTheme.colorScheme.background) {

        NavHost(
            navController = navController,
            startDestination = "home"
        ) {

            composable("home") {
                HomeScreen(
                    onEventsClick = {
                        navController.navigate("events")
                    },
                    onFamiliesClick = {
                        navController.navigate("families")
                    },
                    onProductsClick = {
                        navController.navigate("products")
                    },
                    onEmailsClick = {
                        navController.navigate("emails")
                    },
                    onUpdateClick = {
                        navController.navigate("update")
                                    },
                    updateEnabled = updateState.updateAvailable,
                    updateBusy = updateState.isBusy,
                    isOnline = isOnline
                )
            }

            composable("events") {
                val viewModel: EventsViewModel = hiltViewModel()
                EventsRoute(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onOpenOrder = { eventId, eventName, eventDate ->
                        val nameEnc = Uri.encode(eventName)
                        val dateEnc = Uri.encode(eventDate.format(EVENT_DATE_FORMATTER))
                        navController.navigate("order/$eventId?name=$nameEnc&date=$dateEnc")
                    }

                )
            }

            composable("families") {
                val viewModel: FamiliesViewModel = hiltViewModel()
                FamiliesRoute(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("products") {
                val viewModel: ProductsViewModel = hiltViewModel()

                ProductsRoute(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("emails") {
                val viewModel: EmailsViewModel = hiltViewModel()
                EmailsRoute(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("order/{eventId}?name={name}&date={date}") { backStackEntry ->
                val eventId = backStackEntry.arguments
                    ?.getString("eventId")
                    ?.toLong() ?: return@composable

                val eventName = backStackEntry.arguments
                    ?.getString("name")
                    ?.let { Uri.decode(it) }
                    ?: "אירוע"

                val eventDateTextRaw = backStackEntry.arguments
                    ?.getString("date")
                    ?.let { Uri.decode(it) }
                    ?: ""

                val ordersVm: OrdersViewModel = hiltViewModel()
                val familiesVm: FamiliesViewModel = hiltViewModel()
                val productsVm: ProductsViewModel = hiltViewModel()

                val families by familiesVm.families.collectAsState()
                val products by productsVm.products.collectAsState()

                OrdersRoute(
                    viewModel = ordersVm,
                    eventId = eventId,
                    eventName = eventName,
                    eventDateText = eventDateTextRaw,
                    families = families,
                    products = products,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("update") {
                UpdateScreen(vm = updateVm, onClose = { navController.popBackStack() })
            }
        }
    }
}