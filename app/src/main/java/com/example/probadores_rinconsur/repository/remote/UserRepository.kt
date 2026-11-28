package com.example.probadores_rinconsur.repository.remote

import com.example.probadores_rinconsur.repository.data.UserDao
import com.example.probadores_rinconsur.repository.data.UsuarioSedeDao
import com.example.probadores_rinconsur.repository.models.Sede
import com.example.probadores_rinconsur.repository.models.User
import com.example.probadores_rinconsur.Retrofit.UserApiService
import com.example.probadores_rinconsur.Retrofit.RetrofitClient
import com.example.probadores_rinconsur.Retrofit.AuthApiService
import com.example.probadores_rinconsur.Retrofit.dto.LoginRequest
import com.example.probadores_rinconsur.Retrofit.dto.UserResponse
import com.example.probadores_rinconsur.RinconSurApp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

/** El servidor nunca devuelve la contraseña, por eso queda vacía. */
private fun UserResponse.aUser() = User(
    id = id,
    name = name,
    email = email,
    password = "",
    tipoUsuarioId = tipoUsuarioId
)

/**
 * Usuarios y sus sedes asignadas: MySQL por Retrofit + copia local en Room.
 * El login siempre se valida en el servidor (la clave está encriptada con BCrypt).
 */
class UserRepository(
    private val api: UserApiService = RetrofitClient.userApiService,
    private val dao: UserDao = RinconSurApp.database.userDao(),
    private val usuarioSedeDao: UsuarioSedeDao = RinconSurApp.database.usuarioSedeDao(),
    private val authApi: AuthApiService = RetrofitClient.authApiService
) {

    val users: Flow<List<User>> = dao.getAll()

    suspend fun login(request: LoginRequest): Response<UserResponse> = authApi.login(request)

    suspend fun refrescar(): Boolean {
        return try {
            val respuesta = api.listarUsuarios()
            if (respuesta.isSuccessful) {
                dao.reemplazarTodo(respuesta.body().orEmpty().map { it.aUser() })
                true
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /** Pide el usuario al servidor; si no hay conexión usa la copia de Room. */
    suspend fun obtener(id: Int): User? {
        return try {
            val respuesta = api.obtenerUsuario(id)
            if (respuesta.isSuccessful) respuesta.body()?.aUser() else null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            dao.getById(id)
        }
    }

    suspend fun crear(user: User): Response<UserResponse> =
        api.crearUsuario(user).also { if (it.isSuccessful) refrescar() }

    suspend fun actualizar(id: Int, user: User): Response<UserResponse> =
        api.actualizarUsuario(id, user).also { if (it.isSuccessful) refrescar() }

    suspend fun eliminar(id: Int): Response<Unit> =
        api.eliminarUsuario(id).also { if (it.isSuccessful) refrescar() }

    // ---------------- Sedes asignadas a un usuario ----------------

    /** Sedes asignadas al usuario, leídas desde Room. */
    fun sedesDeUsuario(usuarioId: Int): Flow<List<Sede>> =
        usuarioSedeDao.getSedesPorUsuario(usuarioId)

    /** Baja las sedes asignadas desde MySQL y actualiza Room. false si no hay conexión. */
    suspend fun refrescarSedesDeUsuario(usuarioId: Int): Boolean {
        return try {
            val respuesta = api.listarSedesDeUsuario(usuarioId)
            if (respuesta.isSuccessful) {
                usuarioSedeDao.reemplazarSedes(usuarioId, respuesta.body().orEmpty().map { it.id })
                true
            } else {
                false
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /** Guarda las sedes del usuario en MySQL y, si salió bien, también en Room. */
    suspend fun asignarSedes(usuarioId: Int, sedesIds: List<Int>): Response<List<Sede>> =
        api.asignarSedes(usuarioId, sedesIds).also {
            if (it.isSuccessful) usuarioSedeDao.reemplazarSedes(usuarioId, sedesIds)
        }
}
