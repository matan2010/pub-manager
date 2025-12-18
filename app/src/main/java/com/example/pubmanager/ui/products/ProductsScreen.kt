package com.example.pubmanager.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pubmanager.R
import kotlinx.coroutines.launch
import com.example.pubmanager.ui.common.CrudScaffold
import kotlinx.coroutines.launch

@Composable
fun ProductsScreen(
    products: List<ProductUi>,
    onBackClick: () -> Unit,
    onSaveNewProduct: (String, Double) -> Unit,
    onUpdateProduct: (Long, String, Double) -> Unit,
    onDeleteProductClick: (Long) -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }

    var selectedProductId by remember { mutableStateOf<Long?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    var editingProductId by remember { mutableStateOf<Long?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteProduct by remember { mutableStateOf<ProductUi?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val msgValidNamePrice = stringResource(R.string.valid_name_and_price_must_be_filled_in)
    val msgProductUpdated = stringResource(R.string.the_product_was_successfully_updated)
    val msgProductAdded = stringResource(R.string.product_added_successfully)
    val msgNoProductForUpdate = stringResource(R.string.no_product_selected_for_update)
    val msgNoProductForDelete = stringResource(R.string.no_product_selected_for_delete)
    val msgProductDeleted = stringResource(R.string.product_deleted_successfully)

    CrudScaffold(
        title = stringResource(R.string.products),
        onBackClick = onBackClick,
        items = products,
        selectedId = selectedProductId,
        onSelectId = { selectedProductId = it },
        snackbarHostState = snackbarHostState,

        sidePanel = {
            if (!isAdding) {
                Button(
                    onClick = {
                        isEditMode = false
                        editingProductId = null
                        newName = ""
                        newPrice = ""
                        isAdding = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.add_product))
                }
            } else {
                Text(
                    text = if (isEditMode)
                        stringResource(R.string.product_update)
                    else
                        stringResource(R.string.add_product),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val priceDouble =
                                newPrice.replace(',', '.').toDoubleOrNull()

                            if (newName.isBlank() || priceDouble == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgValidNamePrice,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            if (isEditMode && editingProductId != null) {
                                onUpdateProduct(
                                    editingProductId!!,
                                    newName,
                                    priceDouble
                                )

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgProductUpdated,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onSaveNewProduct(newName, priceDouble)

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgProductAdded,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            newName = ""
                            newPrice = ""
                            isAdding = false
                            isEditMode = false
                            editingProductId = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isEditMode)
                                stringResource(R.string.update)
                            else
                                stringResource(R.string.save)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            newName = ""
                            newPrice = ""
                            isAdding = false
                            isEditMode = false
                            editingProductId = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            }
        },

        tableHeader = { hasSelection ->
            Text(
                text = stringResource(R.string.name),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(2f)
            )
            Text(
                text = stringResource(R.string.price),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val id = selectedProductId
                        val productToEdit =
                            products.firstOrNull { it.id == id }

                        if (productToEdit == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoProductForUpdate,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        isAdding = true
                        isEditMode = true
                        editingProductId = productToEdit.id
                        newName = productToEdit.name
                        newPrice = productToEdit.price.toString()
                    },
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726)
                    )
                ) {
                    Text(text = stringResource(R.string.update))
                }

                Button(
                    onClick = {
                        val id = selectedProductId
                        val productToDelete =
                            products.firstOrNull { it.id == id }

                        if (productToDelete == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoProductForDelete,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        pendingDeleteProduct = productToDelete
                        showDeleteDialog = true
                    },
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        },

        rowContent = { product: ProductUi, isSelected, onClick ->
            val backgroundColor =
                if (isSelected) Color(0xFFFFE0B2) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .clickable { onClick() }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(2f)
                )

                Text(
                    text = product.price.toString(),
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        },

        deleteDialog = {
            if (showDeleteDialog && pendingDeleteProduct != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(text = stringResource(R.string.delete_product))
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.are_you_sure_you_want_to_delete_the_product) +
                                    " \"${pendingDeleteProduct!!.name}\"?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = pendingDeleteProduct!!.id
                                onDeleteProductClick(id)

                                showDeleteDialog = false
                                pendingDeleteProduct = null
                                selectedProductId = null

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgProductDeleted,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Text(stringResource(R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                pendingDeleteProduct = null
                            }
                        ) {
                            Text(stringResource(R.string.no))
                        }
                    }
                )
            }
        }
    )
}
