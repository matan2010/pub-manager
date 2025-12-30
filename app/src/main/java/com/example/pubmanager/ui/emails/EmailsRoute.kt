package com.example.pubmanager.ui.emails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun EmailsRoute(
    viewModel: EmailsViewModel,
    onBackClick: () -> Unit
) {
    val emails by viewModel.emails.collectAsState()

    EmailsScreen(
        emails = emails,
        onBackClick = onBackClick,
        onSaveNewEmail = { viewModel.addEmail(it) },
        onUpdateEmail = { id, addr -> viewModel.updateEmail(id, addr) },
        onDeleteEmailClick = { viewModel.deleteEmail(it) },
    )
}