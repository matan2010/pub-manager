package com.example.pubmanager.ui.main

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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
                    }
                )
            }

            composable("events") {
                val viewModel: EventsViewModel = hiltViewModel()
                EventsRoute(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onOpenOrder = { eventId -> navController.navigate("order/$eventId") }
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

            composable("order/{eventId}") { backStackEntry ->
                val eventId = backStackEntry.arguments
                    ?.getString("eventId")
                    ?.toLong() ?: return@composable

                val viewModel: OrdersViewModel = hiltViewModel()

                val ordersVm: OrdersViewModel = hiltViewModel()
                val familiesVm: FamiliesViewModel = hiltViewModel()
                val productsVm: ProductsViewModel = hiltViewModel()

                val families by familiesVm.families.collectAsState()
                val products by productsVm.products.collectAsState()

                OrdersRoute(
                    viewModel = ordersVm,
                    eventId = eventId,
                    families = families,
                    products = products,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}