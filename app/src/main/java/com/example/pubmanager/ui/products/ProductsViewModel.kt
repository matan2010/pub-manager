package com.example.pubmanager.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pubmanager.data.model.Product
import com.example.pubmanager.domain.mapper.toUi
import com.example.pubmanager.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<ProductUi>>(emptyList())
    val products: StateFlow<List<ProductUi>> = _products.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val dbProducts = repository.getAllProducts()
            _products.value = dbProducts.map { it.toUi() }
        }
    }

    fun addProduct(name: String, price: Double) {
        viewModelScope.launch {
            val product = Product(
                name = name,
                price = price
            )
            repository.insertProduct(product)
            loadProducts()
        }
    }

    fun updateProduct(id: Long, name: String, price: Double) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name,
                price = price
            )
            repository.updateProduct(product)
            loadProducts()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            val uiProduct = _products.value.firstOrNull { it.id == id } ?: return@launch

            val product = Product(
                id = uiProduct.id,
                name = uiProduct.name,
                price = uiProduct.price
            )

            repository.deleteProduct(product)
            loadProducts()
        }
    }
}
