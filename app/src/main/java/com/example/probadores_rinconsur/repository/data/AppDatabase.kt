package com.example.probadores_rinconsur.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.repository.models.User
import com.example.probadores_rinconsur.repository.models.UsuarioSede

/**
 * Convierte el enum EstadoProbador a texto
 * para que Room pueda almacenarlo.
 */
class Converters {

    @TypeConverter
    fun estadoATexto(e: EstadoProbador): String = e.name

    @TypeConverter
    fun textoAEstado(t: String): EstadoProbador =
        EstadoProbador.valueOf(t)
}

/**
 * Base de datos local (Room).
 *
 * Es una COPIA de lo que está en MySQL: la app siempre guarda primero en el
 * microservicio y después actualiza esta copia. Así los datos se pueden ver
 * sin internet y nunca se inventan datos locales distintos a los del servidor.
 */
@Database(
    entities = [
        User::class,
        TipoUsuario::class,
        Sede::class,
        Probador::class,
        RegistroOcupacion::class,
        ReportePrenda::class,
        UsuarioSede::class
    ],
    version = 6, // v6: Room pasa a ser una copia de MySQL (ids del servidor, sin claves foráneas locales)
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tipoUsuarioDao(): TipoUsuarioDao
    abstract fun sedeDao(): SedeDao
    abstract fun probadorDao(): ProbadorDao
    abstract fun registroDao(): RegistroDao
    abstract fun reporteDao(): ReporteDao
    abstract fun usuarioSedeDao(): UsuarioSedeDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rinconsur_database"
                )
                    // Los datos reales están en MySQL: si cambia la estructura,
                    // la copia local se vuelve a bajar del servidor sin perder nada.
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
