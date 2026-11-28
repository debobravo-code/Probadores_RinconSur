package com.example.probadores_rinconsur.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.probadores_rinconsur.repository.remote.ClimaApiService
import com.example.probadores_rinconsur.repository.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClimaUiState(
    val cargando: Boolean = false,
    val temperatura: Double? = null,
    val descripcion: String = "",
    val error: String? = null
)

/**
 * API EXTERNA (Open-Meteo): clima actual de Santiago para la pantalla de inicio.
 * Es independiente del microservicio propio: si falla, el resto de la app sigue funcionando.
 */
class ClimaViewModel(
    private val api: ClimaApiService = RetrofitClient.climaApiService
) : ViewModel() {

    companion object {
        // Santiago de Chile
        const val LATITUD = -33.45
        const val LONGITUD = -70.67

        /** Traduce el código de clima de Open-Meteo (WMO) a un texto simple. */
        fun descripcionDelCodigo(codigo: Int): String = when (codigo) {
            0 -> "Despejado"
            1, 2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Niebla"
            in 51..57 -> "Llovizna"
            in 61..67 -> "Lluvia"
            in 71..77 -> "Nieve"
            in 80..82 -> "Chubascos"
            in 95..99 -> "Tormenta"
            else -> "Sin información"
        }
    }

    private val _estado = MutableStateFlow(ClimaUiState())
    val estado: StateFlow<ClimaUiState> = _estado.asStateFlow()

    init {
        cargarClima()
    }

    fun cargarClima() {
        if (_estado.value.cargando) return

        viewModelScope.launch {
            _estado.value = ClimaUiState(cargando = true)

            try {
                val respuesta = api.obtenerClima(
                    latitud = LATITUD,
                    longitud = LONGITUD,
                    actual = "temperature_2m,weather_code",
                    zonaHoraria = "America/Santiago"
                )

                val clima = respuesta.body()?.current

                _estado.value = if (respuesta.isSuccessful && clima != null) {
                    ClimaUiState(
                        temperatura = clima.temperatura,
                        descripcion = descripcionDelCodigo(clima.codigoClima)
                    )
                } else {
                    ClimaUiState(error = "No se pudo obtener el clima")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ClimaUiState(error = "No se pudo conectar con el servicio del clima")
            }
        }
    }
}
