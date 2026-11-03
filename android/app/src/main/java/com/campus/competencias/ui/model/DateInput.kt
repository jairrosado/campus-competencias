/**
 * Archivo: android/app/src/main/java/com/campus/competencias/ui/model/DateInput.kt
 * Propósito: Conversión entre las fechas ISO del backend y el texto local que se
 * escribe en los formularios (yyyy-MM-dd HH:mm).
 *
 * Historias de usuario:
 * - HU-07 · Creación y consulta de torneos
 * - HU-08 · Reglamento deportivo y apertura de inscripciones
 * - HU-09 · Configuración de hackatones
 * - AN-04 · Eventos y catálogos en Android
 */
package com.campus.competencias.ui.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val inputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

/** Texto legible para editar una fecha ISO que ya viene del backend. */
fun isoToInput(value: String?): String {
    if (value.isNullOrBlank()) return ""
    return runCatching {
        OffsetDateTime.parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(inputFormatter)
    }.recoverCatching {
        Instant.parse(value)
            .atZone(ZoneId.systemDefault())
            .format(inputFormatter)
    }.getOrDefault("")
}

/**
 * Convierte el texto local a ISO 8601 con zona horaria, que es el formato que
 * exige el backend (por ejemplo 2026-11-20T08:00:00-05:00).
 */
fun inputToIso(value: String): String? = runCatching {
    LocalDateTime.parse(value.trim(), inputFormatter)
        .atZone(ZoneId.systemDefault())
        .toOffsetDateTime()
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}.getOrNull()

fun inputDate(daysFromNow: Int, hour: Int = 8): String =
    LocalDateTime.now()
        .plusDays(daysFromNow.toLong())
        .withHour(hour)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .format(inputFormatter)

fun isValidInputDate(value: String): Boolean =
    runCatching { LocalDateTime.parse(value.trim(), inputFormatter) }.isSuccess

fun compareInputDates(first: String, second: String): Int =
    LocalDateTime.parse(first.trim(), inputFormatter)
        .compareTo(LocalDateTime.parse(second.trim(), inputFormatter))
