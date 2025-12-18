package com.example.pubmanager.domain.mapper

import com.example.pubmanager.data.model.Event
import com.example.pubmanager.ui.events.EventUi

fun Event.toUi(): EventUi =
    EventUi(
        id = this.id,
        name = this.name,
        date = this.date
    )

fun EventUi.toEntity(): Event =
    Event(
        id = this.id,
        name = this.name,
        date = this.date
    )