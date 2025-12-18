package com.example.pubmanager.domain.mapper

import com.example.pubmanager.data.model.Email
import com.example.pubmanager.ui.emails.EmailUi

fun Email.toUi(): EmailUi =
    EmailUi(
        id = this.id,
        email = this.email
    )

fun EmailUi.toEntity(): Email =
    Email(
        id = this.id,
        email = this.email
    )