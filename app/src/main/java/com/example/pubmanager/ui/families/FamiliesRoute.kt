package com.example.pubmanager.ui.families

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun FamiliesRoute(
    viewModel: FamiliesViewModel,
    onBackClick: () -> Unit
) {
    val families by viewModel.families.collectAsState()

    FamiliesScreen(
        families = families,
        onBackClick = onBackClick,
        onSaveNewFamily = { firstName, lastName ->
            viewModel.addFamily(firstName, lastName)
        },
        onUpdateFamily = { id, firstName, lastName ->
            viewModel.updateFamily(id, firstName, lastName)
        },
        onDeleteFamilyClick = { id ->
            viewModel.deleteFamily(id)
        }
    )
}