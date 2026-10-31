package com.example.probadores_rinconsur.viewmodels

/**
 * Reglas de validación compartidas por los ViewModels.
 */
object Validaciones {

    private const val LETRA = "[A-Za-zÁÉÍÓÚáéíóúÜüÑñ]"

    /**
     * Solo letras mayúsculas y minúsculas (incluye ñ, Ñ y vocales con tilde o diéresis),
     * con UN espacio entre palabras. Ej. válidos: "Juan Pérez", "Jefe Ventas", "Administrador".
     * Inválidos: " Juan", "Juan ", "Juan  Pérez", "Juan3", "Jefe-Ventas".
     */
    private val SOLO_LETRAS_Y_ESPACIOS = Regex("^$LETRA+( $LETRA+)*$")

    fun esSoloLetras(valor: String): Boolean = SOLO_LETRAS_Y_ESPACIOS.matches(valor)
}