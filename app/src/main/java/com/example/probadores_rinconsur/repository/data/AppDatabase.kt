package com.example.probadores_rinconsur.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.probadores_rinconsur.repository.models.EstadoProbador
import com.example.probadores_rinconsur.repository.models.Probador
import com.example.probadores_rinconsur.repository.models.RegistroOcupacion
import com.example.probadores_rinconsur.repository.models.ReportePrenda
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.TipoUsuario
import com.example.probadores_rinconsur.repository.models.User
import com.example.probadores_rinconsur.repository.models.UsuarioSede

/** Le enseña a Room a guardar el enum EstadoProbador como texto. */
class Converters {

    @TypeConverter
    fun estadoATexto(e: EstadoProbador): String = e.name

    @TypeConverter
    fun textoAEstado(t: String): EstadoProbador =
        EstadoProbador.valueOf(t)
}

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
    version = 4, // v4: relación entre usuarios y sedes asignadas
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
                    // Proyecto académico: al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private val seedCallback = object : Callback() {

            // Se ejecuta UNA vez, cuando se crea la BD:
            // sedes, probadores y datos de ejemplo.
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                sembrarSedesYProbadores(db)
            }

            // Se ejecuta en cada apertura con INSERT OR IGNORE:
            // los tipos de usuario y las cuentas iniciales siempre existen.
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

                db.execSQL(
                    "INSERT OR IGNORE INTO tipo_usuarios " +
                            "(id, tipo_usuario) VALUES " +
                            "(1, 'Administrador'), " +
                            "(2, 'Personal')"
                )

                db.execSQL(
                    "INSERT OR IGNORE INTO users " +
                            "(name, email, password, tipoUsuarioId) VALUES " +
                            "('Ana Castillo', 'admin@rinconsur.cl', 'admin123', 1), " +
                            "('María Pérez', 'maria@rinconsur.cl', 'personal123', 2)"
                )
            }
        }

        /** Datos FICTICIOS: 3 sedes de 6 probadores. */
        private fun sembrarSedesYProbadores(
            db: SupportSQLiteDatabase
        ) {
            val ahora = System.currentTimeMillis()
            val min = 60_000L

            db.execSQL(
                "INSERT INTO sedes (id, nombre, tiempoAlerta) VALUES " +
                        "(1, 'Santiago Norte', 15), " +
                        "(2, 'Santiago Centro', 15), " +
                        "(3, 'Santiago Oriente', 15)"
            )

            val filas = (1..3).flatMap { sede ->
                (1..6).map { n ->

                    val id = (sede - 1) * 6 + n

                    val (estado, inicio, prendas) =
                        if (sede != 1) {
                            Triple(
                                "LIBRE",
                                "NULL",
                                0
                            )
                        } else {
                            when (n) {
                                2 -> Triple(
                                    "OCUPADO",
                                    "${ahora - 8 * min}",
                                    3
                                )

                                3 -> Triple(
                                    "OCUPADO",
                                    "${ahora - 22 * min}",
                                    2
                                )

                                5 -> Triple(
                                    "PRENDA_OLVIDADA",
                                    "NULL",
                                    0
                                )

                                6 -> Triple(
                                    "FUERA_DE_SERVICIO",
                                    "NULL",
                                    0
                                )

                                else -> Triple(
                                    "LIBRE",
                                    "NULL",
                                    0
                                )
                            }
                        }

                    "($id, $n, $sede, '$estado', $inicio, $prendas)"
                }
            }

            db.execSQL(
                "INSERT INTO probadores " +
                        "(id, numero, sedeId, estado, " +
                        "inicioOcupacion, cantidadPrendas) VALUES " +
                        filas.joinToString(", ")
            )

            val usos = listOf(
                listOf(1, 1, 300, 17, 0, 3),
                listOf(1, 2, 240, 18, 0, 2),
                listOf(1, 5, 120, 15, 1, 4),
                listOf(1, 4, 200, 12, 0, 1),
                listOf(1, 1, 180, 20, 0, 4),
                listOf(1, 3, 150, 9, 0, 2),
                listOf(2, 1, 280, 14, 0, 3),
                listOf(2, 2, 210, 16, 0, 4),
                listOf(2, 3, 90, 11, 0, 2),
                listOf(3, 1, 260, 13, 0, 1),
                listOf(3, 4, 170, 19, 0, 3)
            ).joinToString(", ") { u ->

                "(${u[0]}, ${u[1]}, " +
                        "${ahora - u[2] * min}, " +
                        "${ahora - (u[2] - u[3]) * min}, " +
                        "${u[4]}, ${u[5]})"
            }

            db.execSQL(
                "INSERT INTO registros " +
                        "(sedeId, numeroProbador, inicioMillis, " +
                        "finMillis, prendaOlvidada, cantidadPrendas) " +
                        "VALUES $usos"
            )

            db.execSQL(
                "INSERT INTO reportes_prenda " +
                        "(probadorId, descripcion, fotoUri, fechaMillis) " +
                        "VALUES " +
                        "(5, 'Polera negra encontrada dentro del probador.', " +
                        "NULL, ${ahora - 105 * min})"
            )
        }
    }
}