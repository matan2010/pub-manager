package com.example.pubmanager.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.pubmanager.R
import androidx.compose.ui.AbsoluteAlignment

@Composable
fun HomeScreen(
    onEventsClick: () -> Unit = {},
    onFamiliesClick: () -> Unit = {},
    onProductsClick: () -> Unit = {},
    onEmailsClick: () -> Unit = {}
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.main_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(AbsoluteAlignment.CenterRight)
                .width(260.dp)
        ) {
            Button(
                onClick = onEventsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.events))
            }

            Button(
                onClick = onFamiliesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.families))
            }

            Button(
                onClick = onProductsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.products))
            }

            Button(
                onClick = onEmailsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.emails))
            }
        }
    }
}