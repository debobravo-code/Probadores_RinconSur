package com.example.probadores_rinconsur

import android.app.Application
import com.example.probadores_rinconsur.repository.data.AppDatabase

/**
 * Se ejecuta una sola vez al abrir la app.
 * Crea la base de datos Room para que los repositorios la usen.
 */
class RinconSurApp : Application() {

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
    }

    companion object {
        lateinit var database: AppDatabase
            private set
    }
}
