package com.example.probadores_rinconsur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.probadores_rinconsur.navegations.AppNavigation
import com.example.probadores_rinconsur.view.theme.AppTheme
import com.example.probadores_rinconsur.viewmodels.EstadisticasViewModel
import com.example.probadores_rinconsur.viewmodels.HistorialViewModel
import com.example.probadores_rinconsur.viewmodels.LoginViewModel
import com.example.probadores_rinconsur.viewmodels.ProbadorViewModel
import com.example.probadores_rinconsur.viewmodels.ReporteViewModel
import com.example.probadores_rinconsur.viewmodels.SedeViewModel
import com.example.probadores_rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.probadores_rinconsur.viewmodels.UserViewModel

class MainActivity : ComponentActivity() {

    // ViewModels (capa VM de MVVM) con alcance de la Activity
    private val loginViewModel: LoginViewModel by viewModels()
    private val probadorViewModel: ProbadorViewModel by viewModels()
    private val reporteViewModel: ReporteViewModel by viewModels()
    private val historialViewModel: HistorialViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val tipoUsuarioViewModel: TipoUsuarioViewModel by viewModels()
    private val sedeViewModel: SedeViewModel by viewModels()
    private val estadisticasViewModel: EstadisticasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation(
                    loginViewModel, probadorViewModel, reporteViewModel, historialViewModel,
                    userViewModel, tipoUsuarioViewModel, sedeViewModel, estadisticasViewModel
                )
            }
        }
    }
}