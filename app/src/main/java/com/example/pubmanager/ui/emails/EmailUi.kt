package com.example.pubmanager.ui.emails

import com.example.pubmanager.ui.common.HasId

data class EmailUi(
    override val id: Long,
    val email: String
) : HasId