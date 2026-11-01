package com.example.probadores_rinconsur.view.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Ayudas para mostrar horas, fechas y minutos. */
object Formato {
    private val zona: ZoneId get() = ZoneId.systemDefault()

    fun minutosEntre(inicio: Long, fin: Long): Int = ((fin - inicio) / 60_000L).toInt().coerceAtLeast(0)

    fun hora(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zona).format(DateTimeFormatter.ofPattern("HH:mm"))

    fun fecha(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(zona).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}