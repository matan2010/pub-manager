package com.example.pubmanager.ui.update

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun UpdateScreen(vm: UpdateViewModel, onClose: () -> Unit) {
    val state by vm.state.collectAsState()

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Update") },
        text = { Text(state.message) },
        confirmButton = {
            Button(
                onClick = { vm.checkAndUpdate() },
                enabled = !state.isBusy
            ) {
                Text(if (state.isBusy) "Working..." else "Check / Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onClose) { Text("Close") }
        }
    )
}
