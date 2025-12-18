package com.example.pubmanager.ui.families

import com.example.pubmanager.ui.common.HasId

data class FamilyUi(
    override val id: Long,
    val firstName: String,
    val lastName: String
) : HasId