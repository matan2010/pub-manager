package com.example.pubmanager.ui.events

import com.example.pubmanager.ui.common.HasId
import java.time.LocalDate

data class EventUi (
    override val id: Long,
    val name: String,
    val date: LocalDate
) : HasId
