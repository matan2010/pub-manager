package com.example.pubmanager.ui.families

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pubmanager.R
import com.example.pubmanager.ui.common.CrudScaffold
import kotlinx.coroutines.launch

@Composable
fun FamiliesScreen(
    families: List<FamilyUi>,
    onBackClick: () -> Unit,
    onSaveNewFamily: (String, String) -> Unit,
    onUpdateFamily: (Long, String, String) -> Unit,
    onDeleteFamilyClick: (Long) -> Unit
) {
    var isAdding by remember { mutableStateOf(false) }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }

    var selectedFamilyId by remember { mutableStateOf<Long?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    var editingFamilyId by remember { mutableStateOf<Long?>(null) }

    var nameError by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingDeleteFamily by remember { mutableStateOf<FamilyUi?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val txtTitle = stringResource(R.string.families_title)
    val txtAddFamily = stringResource(R.string.add_family)
    val txtFamilyUpdate = stringResource(R.string.family_update)
    val txtFirstName = stringResource(R.string.first_name)
    val txtLastName = stringResource(R.string.last_name)
    val txtUpdate = stringResource(R.string.update)
    val txtSave = stringResource(R.string.save)
    val txtCancel = stringResource(R.string.cancel)
    val txtDelete = stringResource(R.string.delete)
    val txtDeleteFamily = stringResource(R.string.delete_family)
    val txtDeleteFamilyQuestion =
        stringResource(R.string.are_you_sure_you_want_to_delete_the_family)
    val txtYes = stringResource(R.string.yes)
    val txtNo = stringResource(R.string.no)

    val msgNoFamilyForUpdate = stringResource(R.string.no_family_selected_for_update)
    val msgNoFamilyForDelete = stringResource(R.string.no_family_selected_for_delete)
    val msgFamilyAdded = stringResource(R.string.family_added_successfully)
    val msgFamilyUpdated = stringResource(R.string.family_updated_successfully)
    val msgFamilyDeleted = stringResource(R.string.family_deleted_successfully)
    val msgNamesRequired = stringResource(R.string.valid_first_and_last_name_must_be_filled_in)

    CrudScaffold(
        title = txtTitle,
        onBackClick = onBackClick,
        items = families,
        selectedId = selectedFamilyId,
        onSelectId = { selectedFamilyId = it },
        snackbarHostState = snackbarHostState,

        sidePanel = {
            if (!isAdding) {
                Button(
                    onClick = {
                        isEditMode = false
                        editingFamilyId = null
                        newFirstName = ""
                        newLastName = ""
                        nameError = null
                        isAdding = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = txtAddFamily)
                }
            } else {
                Text(
                    text = if (isEditMode) txtFamilyUpdate else txtAddFamily,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newFirstName,
                    onValueChange = {
                        newFirstName = it
                        nameError = null
                    },
                    label = { Text(txtFirstName) },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newLastName,
                    onValueChange = {
                        newLastName = it
                        nameError = null
                    },
                    label = { Text(txtLastName) },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default
                )

                if (nameError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nameError!!,
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
                            val first = newFirstName.trim()
                            val last = newLastName.trim()

                            if (first.isBlank() || last.isBlank()) {
                                nameError = msgNamesRequired
                                return@Button
                            }

                            if (isEditMode && editingFamilyId != null) {
                                onUpdateFamily(
                                    editingFamilyId!!,
                                    first,
                                    last
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgFamilyUpdated,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } else {
                                onSaveNewFamily(first, last)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgFamilyAdded,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }

                            newFirstName = ""
                            newLastName = ""
                            isAdding = false
                            isEditMode = false
                            editingFamilyId = null
                            nameError = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isEditMode) txtUpdate else txtSave)
                    }

                    OutlinedButton(
                        onClick = {
                            newFirstName = ""
                            newLastName = ""
                            isAdding = false
                            isEditMode = false
                            editingFamilyId = null
                            nameError = null
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
                text = txtFirstName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = txtLastName,
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
                        val id = selectedFamilyId
                        val familyToEdit = families.firstOrNull { it.id == id }

                        if (familyToEdit == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoFamilyForUpdate,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        isAdding = true
                        isEditMode = true
                        editingFamilyId = familyToEdit.id
                        newFirstName = familyToEdit.firstName
                        newLastName = familyToEdit.lastName
                        nameError = null
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
                        val id = selectedFamilyId
                        val familyToDelete = families.firstOrNull { it.id == id }

                        if (familyToDelete == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = msgNoFamilyForDelete,
                                    duration = SnackbarDuration.Short
                                )
                            }
                            return@Button
                        }

                        pendingDeleteFamily = familyToDelete
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

        rowContent = { family: FamilyUi, isSelected, onClick ->
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
                    text = family.firstName,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = family.lastName,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        },

        deleteDialog = {
            if (showDeleteDialog && pendingDeleteFamily != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = {
                        Text(text = txtDeleteFamily)
                    },
                    text = {
                        Text(
                            text = txtDeleteFamilyQuestion +
                                    " \"${pendingDeleteFamily!!.firstName} ${pendingDeleteFamily!!.lastName}\"?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val id = pendingDeleteFamily!!.id
                                onDeleteFamilyClick(id)

                                showDeleteDialog = false
                                pendingDeleteFamily = null
                                selectedFamilyId = null

                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = msgFamilyDeleted,
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
                                pendingDeleteFamily = null
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
