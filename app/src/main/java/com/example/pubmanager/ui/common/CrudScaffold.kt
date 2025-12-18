package com.example.pubmanager.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> CrudScaffold(
    title: String,
    onBackClick: () -> Unit,

    items: List<T>,
    selectedId: Long?,
    onSelectId: (Long?) -> Unit,

    snackbarHostState: SnackbarHostState,

    sidePanel: @Composable ColumnScope.() -> Unit,

    tableHeader: @Composable RowScope.(hasSelection: Boolean) -> Unit,

    rowContent: @Composable (item: T, isSelected: Boolean, onClick: () -> Unit) -> Unit,

    deleteDialog: @Composable (() -> Unit)? = null,

    listState: LazyListState = rememberLazyListState()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .align(AbsoluteAlignment.TopRight)
                .padding(top = 8.dp)
        ) {
            Text(text = "חזור")
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                content = sidePanel
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasSelection = selectedId != null
                    tableHeader(hasSelection)
                }

                Divider()

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { item ->
                        val itemId = (item as? HasId)?.id ?: 0L
                        val isSelected = itemId == selectedId

                        rowContent(
                            item,
                            isSelected
                        ) {
                            onSelectId(
                                if (isSelected) null else itemId
                            )
                        }
                        Divider()
                    }
                }
            }
        }

        deleteDialog?.invoke()

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

interface HasId {
    val id: Long
}
