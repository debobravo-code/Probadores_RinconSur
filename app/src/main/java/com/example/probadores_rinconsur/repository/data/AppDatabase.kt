package com.example.probadores_rinconsur.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        TipoUsuario::class,
        Sede::class,
        Probador::class,
        RegistroOcupacion::class,
        ReportePrenda::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tipoUsuarioDao(): TipoUsuarioDao
    abstract fun sedeDao(): SedeDao
    abstract fun probadorDao(): ProbadorDao
    abstract fun registroDao(): RegistroDao
    abstract fun reporteDao(): ReporteDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rincon_sur_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.tipoUsuarioDao().insert(
                            TipoUsuario(
                                id = TipoUsuario.ID_ADMINISTRADOR,
                                tipoUsuario = "Administrador"
                            )
                        )
                    }
                }
            }
        }
    }
}