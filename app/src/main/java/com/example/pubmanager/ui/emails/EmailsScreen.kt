package com.example.pubmanager.ui.emails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pubmanager.R
import com.example.pubmanager.ui.common.CrudScaffold
import kotlinx.coroutines.launch

@Composable
fun EmailsScreen(
    emails: List<EmailUi>,
    onBackClick: () -> Unit,
    onSaveNewEmail: (String) -> Unit,
    onUpdateEmail: (Long, String) -> Unit,
    onDeleteEmailClick: (Long) -> Unit,
    onSendEmailClick: () -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var newEmail by remember { mutableStateOf("") }

    var selectedEmailId by remember { mutableStateOf<Long?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    var editingEmailId by remember { mutableStateOf<Long?>(null) }

    var emailError by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteEmail by remember { mutableStateOf<EmailUi?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val txtTitle = stringResource(R.string.emails_title)
    val txtAddEmail = stringResource(R.string.add_email)
    val txtEmailUpdate = stringResource(R.string.email_update)
    val txtEmailAddress = stringResource(R.string.email_address)
    val txtUpdate = stringResource(R.string.update)
    val txtSave = stringResource(R.string.save)
    val txtCancel = stringResource(R.string.cancel)
    val txtDelete = stringResource(R.string.delete)
    val txtDeleteEmail = stringResource(R.string.delete_email)
    val txtDeleteEmailQuestion =
        stringResource(R.string.are_you_sure_you_want_to_delete_the_email)
    val txtYes = stringResource(R.string.yes)
    val txtNo = stringResource(R.string.no)

    val msgNoEmailForUpdate = stringResource(R.string.no_email_selected_for_update)
    val msgNoEmailForDelete = stringResource(R.string.no_email_selected_for_delete)
    val msgEmailAdded = stringResource(R.string.email_added_successfully)
    val msgEmailUpdated = stringResource(R.string.email_updated_successfully)
    val msgEmailDeleted = stringResource(R.string.email_deleted_successfully)
    val msgInvalidEmail = stringResource(R.string.invalid_email_address)
    val msgEmailRequired = stringResource(R.string.valid_email_must_be_filled_in)

    CrudScaffold(
        title = txtTitle,
        onBackClick = onBackClick,
        items = emails,
        selectedId = selectedEmailId,
        onSelectId = { selectedEmailId = it },
        snackbarHostState = snackbarHostState,

        sidePanel = {
            if (!isAdding) {
                Button(
                    onClick = {
                        isEditMode = false
                        editingEmailId = null
                        newEmail = ""
                        emailError = null
                        isAdding = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = txtAddEmail)
                }
            } else {
                Text(
                    text = if (isEditMode) txtEmailUpdate else txtAddEmail,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newEmail,
                    onValueChange = {
                        newEmail = it
                        emailError = null
                    },
                    label = { Text(txtEmailAddress) },
                    isError = emailError != null,
                    supportingText = {
                        if (emailError != null) {
                            Text(
                                text = emailError!!,
                                color = Color.Red
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val trimmed = newEmail.trim()

                            if (trimmed.isBlank()) {
                                emailError = msgEmailRequired
                                return@Button
                            }
                            if (!trimmed.contains("@") || !trimmed.contains(".")) {
                                emailError = msgInvalidEmail
                                return@Button
                            }

                            if (isEditMode && editingEmailId != null) {
                                onUpdateEmail(
                                    editingEmailId!!,
                                    trimmed
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEmailUpdated,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onSaveNewEmail(trimmed)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEmailAdded,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            newEmail = ""
                            isAdding = false
                            isEditMode = false
                            editingEmailId = null
                            emailError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isEditMode) txtUpdate else txtSave
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            newEmail = ""
                            isAdding = false
                            isEditMode = false
                            editingEmailId = null
                            emailError = null
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
                text = txtEmailAddress,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(2f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val id = selectedEmailId
                        val emailToEdit = emails.firstOrNull { it.id == id }

                        if (emailToEdit == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoEmailForUpdate,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        isAdding = true
                        isEditMode = true
                        editingEmailId = emailToEdit.id
                        newEmail = emailToEdit.email
                        emailError = null
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
                        val id = selectedEmailId
                        val emailToDelete = emails.firstOrNull { it.id == id }

                        if (emailToDelete == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoEmailForDelete,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        pendingDeleteEmail = emailToDelete
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

        rowContent = { email: EmailUi, isSelected, onClick ->
            val background = if (isSelected) Color(0xFFFFE0B2) else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background)
                    .clickable { onClick() }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = email.email,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },

        deleteDialog = {
            if (showDeleteDialog && pendingDeleteEmail != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(text = txtDeleteEmail)
                    },
                    text = {
                        Text(
                            text = txtDeleteEmailQuestion +
                                    " \"${pendingDeleteEmail!!.email}\"?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = pendingDeleteEmail!!.id
                                onDeleteEmailClick(id)

                                showDeleteDialog = false
                                pendingDeleteEmail = null
                                selectedEmailId = null

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgEmailDeleted,
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
                                pendingDeleteEmail = null
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

