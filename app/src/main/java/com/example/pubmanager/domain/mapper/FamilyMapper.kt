package com.example.pubmanager.domain.mapper

import com.example.pubmanager.data.model.Family
import com.example.pubmanager.ui.families.FamilyUi

fun Family.toUi(): FamilyUi =
    FamilyUi(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName
    )

fun FamilyUi.toEntity(): Family =
    Family(
        id = this.id,
        firstName = this.firstName,
        lastName = this.lastName
    )