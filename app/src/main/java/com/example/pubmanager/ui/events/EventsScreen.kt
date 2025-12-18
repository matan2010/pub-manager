package com.example.pubmanager.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pubmanager.R
import com.example.pubmanager.ui.common.CrudScaffold
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventsScreen(
    events: List<EventUi>,
    onBackClick: () -> Unit,
    onSaveNewEvent: (String, LocalDate) -> Unit,
    onUpdateEvent: (Long, String, LocalDate) -> Unit,
    onDeleteEventClick: (Long) -> Unit,
    onOpenOrder: (Long) -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    var newDate by remember { mutableStateOf<LocalDate?>(null) }

    var newDateText by remember { mutableStateOf("") }

    var selectedEventId by remember { mutableStateOf<Long?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    var editingEventId by remember { mutableStateOf<Long?>(null) }

    var formError by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteEvent by remember { mutableStateOf<EventUi?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
    }

    val txtTitle = stringResource(R.string.events_title)
    val txtAddEvent = stringResource(R.string.add_event)
    val txtEventUpdate = stringResource(R.string.event_update)
    val txtEventName = stringResource(R.string.event_name)
    val txtEventDate = stringResource(R.string.event_date) // מומלץ: "תאריך (DD-MM-YYYY)"
    val txtUpdate = stringResource(R.string.update)
    val txtSave = stringResource(R.string.save)
    val txtCancel = stringResource(R.string.cancel)
    val txtDelete = stringResource(R.string.delete)
    val txtDeleteEvent = stringResource(R.string.delete_event)
    val txtDeleteEventQuestion =
        stringResource(R.string.are_you_sure_you_want_to_delete_the_event)
    val txtYes = stringResource(R.string.yes)
    val txtNo = stringResource(R.string.no)

    val msgNoEventForUpdate = stringResource(R.string.no_event_selected_for_update)
    val msgNoEventForDelete = stringResource(R.string.no_event_selected_for_delete)
    val msgEventAdded = stringResource(R.string.event_added_successfully)
    val msgEventUpdated = stringResource(R.string.event_updated_successfully)
    val msgEventDeleted = stringResource(R.string.event_deleted_successfully)
    val msgEventFormInvalid = stringResource(R.string.valid_event_name_and_date_required)

    CrudScaffold(
        title = txtTitle,
        onBackClick = onBackClick,
        items = events,
        selectedId = selectedEventId,
        onSelectId = { selectedEventId = it },
        snackbarHostState = snackbarHostState,

        sidePanel = {
            if (!isAdding) {
                Button(
                    onClick = {
                        isEditMode = false
                        editingEventId = null
                        newName = ""
                        newDate = null
                        newDateText = ""
                        formError = null
                        isAdding = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = txtAddEvent)
                }
            } else {
                Text(
                    text = if (isEditMode) txtEventUpdate else txtAddEvent,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        formError = null
                    },
                    label = { Text(txtEventName) },
                    isError = formError != null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newDateText,
                    onValueChange = { /* readOnly */ },
                    label = { Text(txtEventDate) },
                    readOnly = true,
                    isError = formError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showDatePicker = true
                            formError = null
                        },
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker = true
                            formError = null
                        }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Pick date"
                            )
                        }
                    }
                )

                if (showDatePicker) {
                    val pickerState = rememberDatePickerState()

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val millis = pickerState.selectedDateMillis
                                if (millis != null) {
                                    val pickedDate = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()

                                    newDate = pickedDate
                                    newDateText = pickedDate.format(dateFormatter) // DD-MM-YYYY
                                    formError = null
                                }
                                showDatePicker = false
                            }) { Text(txtSave) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text(txtCancel) }
                        }
                    ) {
                        DatePicker(state = pickerState)
                    }
                }

                if (formError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val name = newName.trim()
                            val date = newDate

                            if (name.isBlank() || date == null) {
                                formError = msgEventFormInvalid
                                return@Button
                            }

                            if (isEditMode && editingEventId != null) {
                                onUpdateEvent(editingEventId!!, name, date)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEventUpdated,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onSaveNewEvent(name, date)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEventAdded,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            newName = ""
                            newDate = null
                            newDateText = ""
                            isAdding = false
                            isEditMode = false
                            editingEventId = null
                            formError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isEditMode) txtUpdate else txtSave)
                    }

                    OutlinedButton(
                        onClick = {
                            newName = ""
                            newDate = null
                            newDateText = ""
                            isAdding = false
                            isEditMode = false
                            editingEventId = null
                            formError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = txtCancel)
                    }
                }
            }
        },

        tableHeader = { hasSelection ->
            Text(
                text = txtEventName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = txtEventDate,
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
                        val id = selectedEventId
                        val eventToEdit = events.firstOrNull { it.id == id }

                        if (eventToEdit == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoEventForUpdate,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        isAdding = true
                        isEditMode = true
                        editingEventId = eventToEdit.id
                        newName = eventToEdit.name

                        newDate = eventToEdit.date
                        newDateText = eventToEdit.date.format(dateFormatter)

                        formError = null
                    },
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726)
                    )
                ) {
                    Text(text = txtUpdate)
                }

                Button(
                    onClick = {
                        val id = selectedEventId
                        val eventToDelete = events.firstOrNull { it.id == id }

                        if (eventToDelete == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoEventForDelete,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        pendingDeleteEvent = eventToDelete
                        showDeleteDialog = true
                    },
                    enabled = hasSelection,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text(text = txtDelete)
                }
            }
        },

        rowContent = { event: EventUi, isSelected, onClick ->
            val background = if (isSelected) Color(0xFFFFE0B2) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .pointerInput(event.id) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onDoubleTap = { onOpenOrder(event.id) }
                        )
                    }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = event.date.format(dateFormatter),
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.weight(1f))
            }
        },

        deleteDialog = {
            if (showDeleteDialog && pendingDeleteEvent != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = txtDeleteEvent) },
                    text = {
                        Text(
                            text = txtDeleteEventQuestion +
                                    " \"${pendingDeleteEvent!!.name}\"?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = pendingDeleteEvent!!.id
                                onDeleteEventClick(id)

                                showDeleteDialog = false
                                pendingDeleteEvent = null
                                selectedEventId = null

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEventDeleted,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        ) {
                            Text(text = txtYes)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                pendingDeleteEvent = null
                            }
                        ) {
                            Text(text = txtNo)
                        }
                    }
                )
            }
        }
    )
}


