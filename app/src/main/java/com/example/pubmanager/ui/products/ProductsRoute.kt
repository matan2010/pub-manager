package com.example.pubmanager.ui.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ProductsRoute(
    viewModel: ProductsViewModel,
    onBackClick: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    ProductsScreen(
        products = products,
        onBackClick = onBackClick,
        onSaveNewProduct = { name, price ->
            viewModel.addProduct(name, price)
        },
        onUpdateProduct = { id, name, price ->
            viewModel.updateProduct(id, name, price)
        },
        onDeleteProductClick = { id ->
            viewModel.deleteProduct(id)
        }
    )
}
