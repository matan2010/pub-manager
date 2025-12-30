package com.example.pubmanager.ui.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pubmanager.R
import com.example.pubmanager.ui.common.CrudScaffold
import com.example.pubmanager.ui.common.HasId
import com.example.pubmanager.ui.common.isOnline
import com.example.pubmanager.ui.emails.EmailUi
import com.example.pubmanager.ui.families.FamilyUi
import com.example.pubmanager.ui.orders.OrderUi
import com.example.pubmanager.ui.products.ProductUi
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import com.example.pubmanager.drive.DriveUploader
import java.io.File as JavaFile

private data class OrderRowUi(
    override val id: Long,
    val familyId: Long,
    val familyFullName: String,
    val quantitiesByProduct: Map<Long, Int>,
    val total: Double
) : HasId

@Composable
fun OrdersScreen(
    eventId: Long,
    eventName: String,
    eventDateText: String,
    families: List<FamilyUi>,
    products: List<ProductUi>,
    orders: List<OrderUi>,
    emails: List<EmailUi> = emptyList(),
    onBackClick: () -> Unit,
    onSaveNewOrder: (familyId: Long, quantitiesToAdd: Map<Long, Int>) -> Unit,
    onUpdateOrder: (familyId: Long, quantitiesSet: Map<Long, Int>) -> Unit,
    onDeleteOrder: (familyId: Long) -> Unit,
    onSendClick: () -> Unit = {},
    onItemsClick: () -> Unit = {},
    onSendSelectedEmails: (
        toEmails: List<String>,
        excelBytes: ByteArray,
        attachmentName: String,
        subject: String,
        text: String,
        onResult: (String) -> Unit
    ) -> Unit = { _, _, _, _, _, _-> }
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val leftTableListState = rememberLazyListState()

    val txtTitle = stringResource(R.string.orders_title)
    val txtAdd = stringResource(R.string.add_order)
    val txtUpdateOrder = stringResource(R.string.order_update)
    val txtSearchFamily = stringResource(R.string.search_family)
    val txtItems = stringResource(R.string.items)
    val txtFamily = stringResource(R.string.family)
    val txtTotal = stringResource(R.string.total)
    val txtUpdate = stringResource(R.string.update)
    val txtDelete = stringResource(R.string.delete)
    val txtSave = stringResource(R.string.save)
    val txtCancel = stringResource(R.string.cancel)
    val txtSend = stringResource(R.string.send)

    val txtDeleteOrder = stringResource(R.string.delete_order)
    val txtDeleteOrderQuestion = stringResource(R.string.are_you_sure_you_want_to_delete_the_order)
    val txtYes = stringResource(R.string.yes)
    val txtNo = stringResource(R.string.no)

    val msgNoOrderForUpdate = stringResource(R.string.no_order_selected_for_update)
    val msgNoOrderForDelete = stringResource(R.string.no_order_selected_for_delete)
    val msgNoFamilySelected = stringResource(R.string.no_family_selected)
    val msgPickAtLeastOneItem = stringResource(R.string.pick_at_least_one_item)
    val msgDeleted = stringResource(R.string.order_deleted)
    val txtUpdateOrderSuccessfully = stringResource(R.string.order_update_successfully)
    val txtEmailsTitle = stringResource(R.string.emails_title)
    val txtNoEmails = stringResource(R.string.no_emails)
    val txtClose = stringResource(R.string.close)

    val txtNoItems = stringResource(R.string.no_items)
    val txtGrandTotalLabel = stringResource(R.string.grand_total_label)

    val msgSubjectEmail = stringResource(R.string.subject_email)
    val msgTextEmail = stringResource(R.string.text_email)

    val msgNoInternetConnection = stringResource(R.string.no_internet_connection)
    val msgNoValidEmailsWereSelected = stringResource(R.string.no_valid_emails_were_selected)

    val msgUploadedToDrive = stringResource(R.string.uploaded_to_drive)
    val msgSending = stringResource(R.string.sending)

    val familyById = remember(families) { families.associateBy { it.id } }
    val productById = remember(products) { products.associateBy { it.id } }

    val productsScrollState = rememberScrollState()

    val folderId = "14JuWiFFXZA0GZf6MbLAEiO0yn3zI-2pb"
    val productColWidth = 92.dp

    val totalWeight = 0.8f
    val totalStartPadding = 50.dp
    val totalToButtonsGap = 22.dp

    val buttonsAreaWeight = 1.35f
    val productsAreaWeight = 2.0f
    val headerBtnPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)

    val orderRows = remember(eventId, families, products, orders, context, familyById, productById) {
        val ordersForEvent = orders.filter { it.eventId == eventId }
        val grouped = ordersForEvent.groupBy { it.familyId }

        grouped.map { (familyId, familyOrders) ->
            val fam = familyById[familyId]
            val fullName =
                if (fam != null) "${fam.firstName} ${fam.lastName}"
                else context.getString(R.string.family_unknown_format, familyId)

            val quantities = products.associate { p ->
                val qty = familyOrders.firstOrNull { it.productId == p.id }?.quantity ?: 0
                p.id to qty
            }
            val total = quantities.entries.sumOf { (pid, qty) ->
                val price = productById[pid]?.price ?: 0.0
                price * qty
            }
            OrderRowUi(
                id = familyId,
                familyId = familyId,
                familyFullName = fullName,
                quantitiesByProduct = quantities,
                total = total
            )
        }.sortedWith(
            compareBy<OrderRowUi> { row ->
                val fam = familyById[row.familyId]
                fam?.lastName?.lowercase(Locale.getDefault()) ?: ""
            }.thenBy { row ->
                val fam = familyById[row.familyId]
                fam?.firstName?.lowercase(Locale.getDefault()) ?: ""
            }
        )
    }

    val grandTotal = remember(orderRows) {
        BigDecimal.valueOf(orderRows.sumOf { it.total })
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
    }

    var selectedFamilyId by remember { mutableStateOf<Long?>(null) }

    var isAdding by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    var familySearch by remember { mutableStateOf("") }
    var pickedFamilyId by remember { mutableStateOf<Long?>(null) }

    val qtyState = remember { mutableStateMapOf<Long, Int>() }

    fun initQuantitiesAllZero() {
        qtyState.clear()
        products.forEach { p -> qtyState[p.id] = 0 }
    }

    fun resetForm() {
        isAdding = false
        isEditMode = false
        pickedFamilyId = null
        familySearch = ""
        qtyState.clear()
        focusManager.clearFocus()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteFamilyId by remember { mutableStateOf<Long?>(null) }

    val filteredFamilies = remember(families, familySearch) {
        val q = familySearch.trim().lowercase(Locale.getDefault())
        if (q.isBlank()) {
            families
        } else {
            val tokens = q.split(Regex("\\s+")).filter { it.isNotBlank() }

            families.filter { f ->
                val first = f.firstName.lowercase(Locale.getDefault())
                val last = f.lastName.lowercase(Locale.getDefault())
                val full = "$first $last"
                val fullRev = "$last $first"

                if (tokens.size == 1) {
                    val t = tokens[0]
                    first.startsWith(t) || last.startsWith(t) || full.startsWith(t) || fullRev.startsWith(t)
                } else {
                    tokens.all { tok -> full.contains(tok) || fullRev.contains(tok) }
                }
            }
        }
    }

    val bringMap = remember { mutableStateMapOf<Long, BringIntoViewRequester>() }

    LaunchedEffect(selectedFamilyId, orderRows) {
        val id = selectedFamilyId ?: return@LaunchedEffect

        val index = orderRows.indexOfFirst { it.familyId == id }
        if (index >= 0) {
            leftTableListState.animateScrollToItem(index)
        } else {
            bringMap[id]?.bringIntoView()
        }

        productsScrollState.animateScrollTo(0)
    }

    var showEmailsDialog by remember { mutableStateOf(false) }
    var showProductsDialog by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }

    val selectedEmailMap = remember { mutableStateMapOf<Long, Boolean>() }

    fun resetEmailSelection() {
        selectedEmailMap.clear()
        emails.forEach { e ->
            selectedEmailMap[e.id] = false
        }
    }

    val selectedEmailsCount = selectedEmailMap.values.count { it }
    val canSendEmails = selectedEmailsCount >= 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
    ) {
        CrudScaffold(
            title = "$eventName      $eventDateText          |       $txtTitle       |                $txtGrandTotalLabel $grandTotal",
            onBackClick = onBackClick,
            items = orderRows,
            selectedId = selectedFamilyId,
            onSelectId = { selectedFamilyId = it },
            snackbarHostState = snackbarHostState,
            listState = leftTableListState,

            sidePanel = {
                if (!isAdding) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isAdding = true
                                isEditMode = false
                                pickedFamilyId = null
                                familySearch = ""
                                initQuantitiesAllZero()
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = txtAdd,
                                maxLines = 2,
                                softWrap = true,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                resetEmailSelection()
                                showEmailsDialog = true
                                focusManager.clearFocus()
                                onSendClick()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Text(
                                text = txtSend,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                showProductsDialog = true
                                focusManager.clearFocus()
                                onItemsClick()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E))
                        ) {
                            Text(
                                text = txtItems,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (isEditMode) txtUpdateOrder else txtAdd,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = familySearch,
                        onValueChange = { familySearch = it },
                        label = { Text(txtSearchFamily) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color(0xFFF5F5F5))
                            .padding(6.dp)
                    ) {
                        items(filteredFamilies) { fam ->
                            val isPicked = pickedFamilyId == fam.id
                            val bg = if (isPicked) Color(0xFFFFE0B2) else Color.Transparent

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                                    .clickable {
                                        pickedFamilyId = fam.id
                                        selectedFamilyId = fam.id
                                        focusManager.clearFocus()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${fam.firstName} ${fam.lastName}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = txtItems,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .background(Color(0xFFF5F5F5))
                            .padding(6.dp)
                    ) {
                        items(products) { p ->
                            val qty = qtyState[p.id] ?: 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.name,
                                    modifier = Modifier.weight(1.5f),
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = p.price.toString(),
                                    modifier = Modifier.weight(0.7f),
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.weight(1.3f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            val current = qtyState[p.id] ?: 0
                                            if (current > 0) qtyState[p.id] = current - 1
                                        },
                                        modifier = Modifier.size(40.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.minus),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Text(
                                        text = qty.toString(),
                                        modifier = Modifier.width(36.dp),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Button(
                                        onClick = {
                                            val current = qtyState[p.id] ?: 0
                                            qtyState[p.id] = current + 1
                                        },
                                        modifier = Modifier.size(40.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.plus),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val effectiveFamilyId =
                                    if (isEditMode) selectedFamilyId else pickedFamilyId

                                if (effectiveFamilyId == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msgNoFamilySelected,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                val quantitiesAllProducts =
                                    products.associate { p -> p.id to (qtyState[p.id] ?: 0) }

                                val anyPositive = quantitiesAllProducts.values.any { it > 0 }
                                if (!anyPositive) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msgPickAtLeastOneItem,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                if (isEditMode) {
                                    onUpdateOrder(effectiveFamilyId, quantitiesAllProducts)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = txtUpdateOrderSuccessfully,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                } else {
                                    onSaveNewOrder(effectiveFamilyId, quantitiesAllProducts)
                                }

                                resetForm()
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(txtSave, maxLines = 1) }

                        OutlinedButton(
                            onClick = { resetForm() },
                            modifier = Modifier.weight(1f)
                        ) { Text(txtCancel, maxLines = 1) }
                    }
                }
            },

            tableHeader = { hasSelection ->
                Text(
                    text = txtFamily,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1.2f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier
                        .weight(productsAreaWeight)
                        .horizontalScroll(productsScrollState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    products.forEach { p ->
                        Box(modifier = Modifier.width(productColWidth)) {
                            Text(
                                text = p.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text(
                    text = txtTotal,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(totalWeight)
                        .padding(start = totalStartPadding),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.width(totalToButtonsGap))

                Column(
                    modifier = Modifier.weight(buttonsAreaWeight),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val famId = selectedFamilyId
                                val row = orderRows.firstOrNull { it.familyId == famId }

                                if (row == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msgNoOrderForUpdate,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }

                                isAdding = true
                                isEditMode = true

                                pickedFamilyId = row.familyId
                                familySearch = row.familyFullName

                                qtyState.clear()
                                products.forEach { p -> qtyState[p.id] = row.quantitiesByProduct[p.id] ?: 0 }

                                focusManager.clearFocus()
                            },
                            enabled = hasSelection,
                            modifier = Modifier.weight(1f),
                            contentPadding = headerBtnPadding,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726))
                        ) {
                            Text(
                                text = txtUpdate,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                val famId = selectedFamilyId
                                if (famId == null) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msgNoOrderForDelete,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@Button
                                }
                                pendingDeleteFamilyId = famId
                                showDeleteDialog = true
                                focusManager.clearFocus()
                            },
                            enabled = hasSelection,
                            modifier = Modifier.weight(1f),
                            contentPadding = headerBtnPadding,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            Text(
                                text = txtDelete,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },

            rowContent = { row: OrderRowUi, isSelected, onClick ->
                val background = if (isSelected) Color(0xFFFFE0B2) else Color.Transparent

                val requester = remember(row.familyId) { BringIntoViewRequester() }
                DisposableEffect(row.familyId) {
                    bringMap[row.familyId] = requester
                    onDispose { bringMap.remove(row.familyId) }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(requester)
                        .background(background)
                        .clickable {
                            onClick()
                            focusManager.clearFocus()
                        }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.familyFullName,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1.2f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier
                            .weight(productsAreaWeight)
                            .horizontalScroll(productsScrollState),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        products.forEach { p ->
                            val q = row.quantitiesByProduct[p.id] ?: 0
                            Box(modifier = Modifier.width(productColWidth)) {
                                Text(
                                    text = q.toString(),
                                    fontSize = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Text(
                        text = row.total.toString(),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .weight(totalWeight)
                            .padding(start = totalStartPadding),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.width(totalToButtonsGap))
                    Spacer(modifier = Modifier.weight(buttonsAreaWeight))
                }
            },

            deleteDialog = {
                if (showDeleteDialog && pendingDeleteFamilyId != null) {
                    val famId = pendingDeleteFamilyId!!
                    val famName =
                        orderRows.firstOrNull { it.familyId == famId }?.familyFullName ?: ""

                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text(txtDeleteOrder) },
                        text = {
                            Text(
                                stringResource(
                                    R.string.delete_order_question_with_name,
                                    txtDeleteOrderQuestion,
                                    famName
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onDeleteOrder(famId)
                                    showDeleteDialog = false
                                    pendingDeleteFamilyId = null
                                    selectedFamilyId = null
                                    resetForm()

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = msgDeleted,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            ) { Text(txtYes) }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    pendingDeleteFamilyId = null
                                }
                            ) { Text(txtNo) }
                        }
                    )
                }
            }
        )

        if (showEmailsDialog) {
            AlertDialog(
                onDismissRequest = { showEmailsDialog = false },
                title = { Text(txtEmailsTitle) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        if (emails.isEmpty()) {
                            Text(txtNoEmails)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(emails) { e ->
                                    val checked = selectedEmailMap[e.id] ?: false

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedEmailMap[e.id] = !checked }
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { v -> selectedEmailMap[e.id] = v }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = e.email,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val sendColor = if (canSendEmails) Color(0xFF43A047) else Color(0xFF9E9E9E)

                    Button(
                        onClick = {
                            if (isSending) return@Button

                            isSending = true

                            if (!isOnline(context)) {
                                isSending = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgNoInternetConnection,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val toEmails = emails
                                .filter { selectedEmailMap[it.id] == true }
                                .map { it.email.trim() }
                                .filter { it.isNotBlank() }
                                .distinct()

                            if (toEmails.isEmpty()) {
                                isSending = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgNoValidEmailsWereSelected,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                                return@Button
                            }

                            val attachmentName = buildExcelFileName(eventName, eventDateText)
                            val excelBytes = createOrdersXlsxBytes(
                                eventId = eventId,
                                eventName = eventName,
                                eventDateText = eventDateText,
                                products = products,
                                orderRows = orderRows,
                                grandTotal = grandTotal,
                                sentToEmails = toEmails
                            )

                            val xlsxFile = JavaFile(context.cacheDir, attachmentName)
                            xlsxFile.writeBytes(excelBytes)

                            scope.launch {
                                try {
                                    val driveId = DriveUploader.uploadFile(
                                        context = context,
                                        localFile = xlsxFile,
                                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        driveFileName = attachmentName,
                                        folderId = folderId
                                    )

                                    snackbarHostState.showSnackbar(
                                        message = msgUploadedToDrive,
                                        duration = SnackbarDuration.Short
                                    )

                                    showEmailsDialog = false
                                    onSendSelectedEmails(
                                        toEmails,
                                        excelBytes,
                                        attachmentName,
                                        msgSubjectEmail,
                                        msgTextEmail
                                    ) { msg ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(message = msg,
                                                actionLabel = txtClose,
                                                duration = SnackbarDuration.Long)
                                        }
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = "Send failed: ${e.message ?: "unknown error"}",
                                        duration = SnackbarDuration.Long
                                    )
                                } finally {
                                    isSending = false
                                }
                            }
                        },
                        enabled = canSendEmails && !isSending,
                        colors = ButtonDefaults.buttonColors(containerColor = sendColor)
                    ) {
                        Text(if (isSending) msgSending else txtSend)
                    }
                },
                dismissButton = {
                    Button(onClick = { showEmailsDialog = false }) { Text(txtCancel) }
                }
            )
        }

        if (showProductsDialog) {
            AlertDialog(
                onDismissRequest = { showProductsDialog = false },
                title = { Text(txtItems) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    ) {
                        if (products.isEmpty()) {
                            Text(txtNoItems)
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(products) { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = p.name,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = p.price.toString(),
                                            modifier = Modifier.width(90.dp),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showProductsDialog = false }) { Text(txtClose) }
                }
            )
        }
    }
}

private fun round1(d: Double): Double =
    BigDecimal.valueOf(d).setScale(1, RoundingMode.HALF_UP).toDouble()



/* =========================================================================================
   Excel helpers
   ========================================================================================= */

private fun buildExcelFileName(eventName: String, eventDateText: String): String {
    val safeName = sanitizeFileName(eventName).ifBlank { "אירוע" }
    val safeDate = sanitizeFileName(eventDateText).ifBlank { "" }
    val base = if (safeDate.isNotBlank()) "${safeName}_$safeDate" else safeName
    return "$base.xlsx"
}

private fun sanitizeFileName(s: String): String {
    val trimmed = s.trim()
    if (trimmed.isBlank()) return ""
    return trimmed
        .replace(Regex("""[\\/:*?"<>|]"""), "-")
        .replace(Regex("""\s+"""), "_")
        .take(80)
}

private fun sanitizeSheetName(name: String): String {
    val cleaned = name.trim()
        .replace(Regex("""[:\\/?*\[\]]"""), " ")
        .replace(Regex("""\s+"""), " ")
        .trim()
    return cleaned.take(31)
}

private fun colToLetters(col1Based: Int): String {
    var n = col1Based
    val sb = StringBuilder()
    while (n > 0) {
        val rem = (n - 1) % 26
        sb.append(('A'.code + rem).toChar())
        n = (n - 1) / 26
    }
    return sb.reverse().toString()
}

private fun xmlEscape(s: String): String {
    return s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}

private fun formatDoubleForXml(d: Double): String {
    return String.format(Locale.US, "%.2f", d)
}

private fun createOrdersXlsxBytes(
    eventId: Long,
    eventName: String,
    eventDateText: String,
    products: List<ProductUi>,
    orderRows: List<OrderRowUi>,
    grandTotal: Double,
    sentToEmails: List<String>
): ByteArray {
    val baos = ByteArrayOutputStream()
    ZipOutputStream(baos).use { zip ->

        fun put(path: String, content: String) {
            zip.putNextEntry(ZipEntry(path))
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        val sheetXml = buildSheetXml(
            eventId = eventId,
            eventName = eventName,
            eventDateText = eventDateText,
            products = products,
            orderRows = orderRows,
            grandTotal = grandTotal,
            sentToEmails = sentToEmails
        )

        val stylesXml = buildStylesXml()

        put(
            "[Content_Types].xml",
            """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
              <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
              <Default Extension="xml" ContentType="application/xml"/>
              <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
              <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
              <!-- ✅ חדש -->
              <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
            </Types>
            """.trimIndent()
        )

        put(
            "_rels/.rels",
            """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                Target="xl/workbook.xml"/>
            </Relationships>
            """.trimIndent()
        )

        val sheetNameSafe = sanitizeSheetName(eventName).ifBlank { "אירוע" }

        put(
            "xl/workbook.xml",
            """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                      xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <sheets>
                <sheet name="$sheetNameSafe" sheetId="1" r:id="rId1"/>
              </sheets>
            </workbook>
            """.trimIndent()
        )

        put(
            "xl/_rels/workbook.xml.rels",
            """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                Target="worksheets/sheet1.xml"/>
              <!-- ✅ חדש -->
              <Relationship Id="rId2"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
                Target="styles.xml"/>
            </Relationships>
            """.trimIndent()
        )

        put("xl/styles.xml", stylesXml)

        put("xl/worksheets/sheet1.xml", sheetXml)
    }

    return baos.toByteArray()
}

private fun buildStylesXml(): String {
    return """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <fonts count="2">
            <font/>
            <font><b/></font>
          </fonts>

          <fills count="6">
            <fill><patternFill patternType="none"/></fill>
            <fill><patternFill patternType="gray125"/></fill>

            <!-- 2: תכלת -->
            <fill><patternFill patternType="solid"><fgColor rgb="FFCCFFFF"/></patternFill></fill>
            <!-- 3: ירקרק -->
            <fill><patternFill patternType="solid"><fgColor rgb="FFCCFFCC"/></patternFill></fill>
            <!-- 4: צהוב -->
            <fill><patternFill patternType="solid"><fgColor rgb="FFFFFF99"/></patternFill></fill>
            <!-- 5: כתום -->
            <fill><patternFill patternType="solid"><fgColor rgb="FFFFCC99"/></patternFill></fill>
          </fills>

          <borders count="1">
            <border/>
          </borders>

          <cellXfs count="10">
            <xf fontId="0" fillId="0" borderId="0"/>                                  <!-- 0 רגיל -->
            <xf fontId="1" fillId="0" borderId="0" applyFont="1"/>                    <!-- 1 Bold -->

            <xf fontId="1" fillId="2" borderId="0" applyFont="1" applyFill="1"/>      <!-- 2 Bold + תכלת -->
            <xf fontId="1" fillId="3" borderId="0" applyFont="1" applyFill="1"/>      <!-- 3 Bold + ירקרק -->
            <xf fontId="1" fillId="4" borderId="0" applyFont="1" applyFill="1"/>      <!-- 4 Bold + צהוב -->
            <xf fontId="1" fillId="5" borderId="0" applyFont="1" applyFill="1"/>      <!-- 5 Bold + כתום -->

            <xf fontId="0" fillId="2" borderId="0" applyFill="1"/>                    <!-- 6 תכלת -->
            <xf fontId="0" fillId="3" borderId="0" applyFill="1"/>                    <!-- 7 ירקרק -->
            <xf fontId="0" fillId="4" borderId="0" applyFill="1"/>                    <!-- 8 צהוב -->
            <xf fontId="0" fillId="5" borderId="0" applyFill="1"/>                    <!-- 9 כתום -->
          </cellXfs>
        </styleSheet>
    """.trimIndent()
}

private fun buildSheetXml(
    eventId: Long,
    eventName: String,
    eventDateText: String,
    products: List<ProductUi>,
    orderRows: List<OrderRowUi>,
    grandTotal: Double,
    sentToEmails: List<String>
): String {
    val headers = buildList {
        add("משפחה")
        products.forEach { add(it.name) }
        add("סה״כ")
    }

    val sideStartCol = headers.size + 2

    val itemsStartCol = sideStartCol + 2

    val rowsXml = StringBuilder()

    fun cellRef(colIndex1Based: Int, rowIndex1Based: Int): String =
        "${colToLetters(colIndex1Based)}$rowIndex1Based"

    fun inlineStrCell(r: String, value: String, style: Int = 0): String {
        val safe = xmlEscape(value)
        return """<c r="$r" s="$style" t="inlineStr"><is><t>$safe</t></is></c>"""
    }

    fun numberCell(r: String, value: String, style: Int = 0): String {
        return """<c r="$r" s="$style"><v>$value</v></c>"""
    }

    fun appendSideCells(cells: StringBuilder, rowIdx: Int) {
        val sideIndex = rowIdx - 2

        val email = sentToEmails.getOrNull(sideIndex)
        if (!email.isNullOrBlank()) {
            cells.append(inlineStrCell(cellRef(sideStartCol, rowIdx), email, 6))
        }

        val p = products.getOrNull(sideIndex)
        if (p != null) {
            cells.append(inlineStrCell(cellRef(itemsStartCol, rowIdx), p.name, 6))
            cells.append(numberCell(cellRef(itemsStartCol + 1, rowIdx), formatDoubleForXml(p.price), 6))
        }
    }

    var rowIdx = 1

    run {
        val cells = StringBuilder()
        cells.append(inlineStrCell(cellRef(1, rowIdx), "אירוע", 2))
        cells.append(inlineStrCell(cellRef(2, rowIdx), "תאריך", 2))

        cells.append(inlineStrCell(cellRef(sideStartCol, rowIdx), "מיילים", 2))
        cells.append(inlineStrCell(cellRef(itemsStartCol, rowIdx), "פריטים", 2))
        cells.append(inlineStrCell(cellRef(itemsStartCol + 1, rowIdx), "מחירים", 2))

        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    run {
        val cells = StringBuilder()
        cells.append(inlineStrCell(cellRef(1, rowIdx), eventName, 6))
        cells.append(inlineStrCell(cellRef(2, rowIdx), eventDateText, 6))

        appendSideCells(cells, rowIdx)

        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    run {
        val cells = StringBuilder()
        appendSideCells(cells, rowIdx)
        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    run {
        val cells = StringBuilder()

        headers.forEachIndexed { i, h ->
            val col = i + 1
            val style = when (col) {
                1 -> 3
                headers.size -> 4
                else -> 2
            }
            cells.append(inlineStrCell(cellRef(col, rowIdx), h, style))
        }

        appendSideCells(cells, rowIdx)
        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    orderRows.forEach { row ->
        val cells = StringBuilder()

        cells.append(inlineStrCell(cellRef(1, rowIdx), row.familyFullName, 7))

        products.forEachIndexed { i, p ->
            val qty = row.quantitiesByProduct[p.id] ?: 0
            cells.append(numberCell(cellRef(2 + i, rowIdx), qty.toString(), 6))
        }

        cells.append(numberCell(cellRef(headers.size, rowIdx), formatDoubleForXml(row.total), 8))

        appendSideCells(cells, rowIdx)

        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    run {
        val cells = StringBuilder()

        cells.append(inlineStrCell(cellRef(1, rowIdx), "סה״כ כללי", 5))

        if (headers.size > 2) {
            for (c in 2 until headers.size) {
                cells.append(inlineStrCell(cellRef(c, rowIdx), "", 9))
            }
        }

        cells.append(numberCell(cellRef(headers.size, rowIdx), formatDoubleForXml(grandTotal), 5))

        appendSideCells(cells, rowIdx)

        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    val neededSideRows = maxOf(sentToEmails.size, products.size) + 1
    while (rowIdx <= neededSideRows) {
        val cells = StringBuilder()
        appendSideCells(cells, rowIdx)
        rowsXml.append("""<row r="$rowIdx">$cells</row>""")
        rowIdx++
    }

    return """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <sheetViews>
            <sheetView rightToLeft="1"/>
          </sheetViews>

          <sheetData>
            $rowsXml
          </sheetData>
        </worksheet>
    """.trimIndent()
}