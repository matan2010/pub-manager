package com.example.pubmanager.ui.emails

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun EmailsRoute(
    viewModel: EmailsViewModel,
    onBackClick: () -> Unit
) {
    val emails by viewModel.emails.collectAsState()
    val context = LocalContext.current

    EmailsScreen(
        emails = emails,
        onBackClick = onBackClick,
        onSaveNewEmail = { address ->
            viewModel.addEmail(address)
        },
        onUpdateEmail = { id, address ->
            viewModel.updateEmail(id, address)
        },
        onDeleteEmailClick = { id ->
            viewModel.deleteEmail(id)
        },
        onSendEmailClick = {
            openEmailComposer(
                context = context,
                to = arrayOf("info@thepergola.pub"),
                subject = "New Order",
                body = "שלום,\nנשלחה הזמנה חדשה מהטאבלט."
            )
        }

    )
}


fun openEmailComposer(
    context: Context,
    to: Array<String>,
    subject: String,
    body: String
) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, to)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}