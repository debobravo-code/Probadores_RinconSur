# Probadores RinconSur

**Integrantes:** Debora Bravo y Mikela Palma
**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105) – Evaluación Parcial 4, Duoc UC

## Descripción

Aplicación móvil para gestionar los probadores de una tienda de ropa: ver su estado, registrar su uso y reportar prendas olvidadas con fotografía. Es la continuación de nuestro proyecto anterior (Room + Compose), ahora conectado a un microservicio propio (Spring Boot + MySQL) con Retrofit.

## Funcionalidades

- **Inicio de sesión** con usuario del microservicio (clave encriptada con BCrypt).
- **Sedes:** crear, ver, editar y eliminar.
- **Probadores:** crear, ver, editar y eliminar; ocupar (1 a 4 prendas), liberar y marcar prenda olvidada.
- **Reportes de prenda olvidada:** crear, ver, editar y eliminar, con foto tomada con la cámara (CameraX) que se sube al microservicio.
- **Usuarios y tipos de usuario:** crear, ver, editar y eliminar; asignar sedes a cada usuario.
- **Historial y estadísticas** por sede.
- **API externa:** en el Inicio se muestra el clima actual de Santiago (Open-Meteo).

## Tecnologías

Kotlin, Jetpack Compose, MVVM, Room, Retrofit + Gson + OkHttp, CameraX, Coil. Pruebas: Kotest + MockK.
Backend: Spring Boot (Java 21), JPA, MySQL.

## Cómo se conectan Room, Retrofit y MySQL

```
Pantalla (Compose) → ViewModel → Repository ─┬→ Retrofit → Microservicio → MySQL   (guarda y consulta)
                                             └→ Room (copia local en el teléfono)   (lo que se muestra)
```

- **MySQL es la fuente de verdad.** Cada cambio (crear, editar, eliminar) se envía primero al microservicio.
- Si el microservicio responde bien, el Repository baja la lista actualizada y **reemplaza** la copia de Room en una sola transacción. Los ids de Room son los mismos de MySQL, por eso no se duplican registros.
- **Las pantallas leen siempre desde Room.** Si no hay internet se siguen viendo los últimos datos guardados y la app avisa que no pudo actualizar.
- Sin conexión al backend no se puede guardar ni editar. La copia local puede quedar desactualizada hasta la siguiente sincronización exitosa.
- El login siempre se valida en el microservicio.

## Organización del proyecto

Carpeta `app/src/main/java/com/example/probadores_rinconsur/`:

- `repository/models/`: modelos (entidades de Room y datos del JSON).
- `repository/data/`: Room (`AppDatabase` y los DAO).
- `Retrofit/`: interfaces API separadas por entidad, RetrofitClient, ClimaApiService y carpeta dto/ con los datos de las peticiones.
- `repository/remote/`: un Repository por recurso (`SedeRepository`, `ProbadorRepository`, `ReporteRepository`, `RegistroRepository`, `UserRepository`, `TipoUsuarioRepository`).
- `viewmodels/`: lógica y estado de cada pantalla.
- `view/ui/` y `view/utils/`: pantallas y componentes.
- `navegations/`: navegación entre pantallas.
- `RinconSurApp.kt`: crea la base de datos Room al abrir la app.

## Endpoints

### API externa (Open-Meteo, sin clave)

| Método | URL | Uso |
|---|---|---|
| GET | `https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.67&current=temperature_2m,weather_code&timezone=auto` | Clima en la pantalla de Inicio |

### Microservicio propio (`http://localhost:8080`)

| Recurso | Endpoints |
|---|---|
| Login | `POST /api/auth/login` |
| Sedes | `GET /api/sedes`, `GET /api/sedes/{id}`, `POST /api/sedes`, `PUT /api/sedes/{id}`, `DELETE /api/sedes/{id}` |
| Probadores | `GET /api/probadores`, `GET /api/probadores/{id}`, `GET /api/probadores/sede/{sedeId}`, `GET /api/probadores/sede/{sedeId}/estado/{estado}`, `POST /api/probadores`, `PUT /api/probadores/{id}`, `DELETE /api/probadores/{id}`, `PATCH /api/probadores/{id}/ocupar`, `PATCH /api/probadores/{id}/liberar`, `PATCH /api/probadores/{id}/estado` |
| Registros de ocupación | `GET /api/registros`, `GET /api/registros/{id}`, `GET /api/registros/sede/{sedeId}`, `GET /api/registros/sede/{sedeId}/probador/{numero}`, `GET /api/registros/sede/{sedeId}/prenda-olvidada`, `GET /api/registros/sede/{sedeId}/fechas`, `PUT /api/registros/{id}` |
| Reportes de prenda | `GET /api/reportes-prenda`, `GET /api/reportes-prenda/{id}`, `GET /api/reportes-prenda/probador/{probadorId}`, `GET /api/reportes-prenda/fechas`, `POST /api/reportes-prenda`, `PUT /api/reportes-prenda/{id}`, `DELETE /api/reportes-prenda/{id}` |
| Fotos | `POST /api/fotos` (multipart), `GET /api/fotos/{nombreArchivo}` |
| Usuarios | `GET /api/users`, `GET /api/users/{id}`, `POST /api/users`, `PUT /api/users/{id}`, `DELETE /api/users/{id}`, `GET /api/users/{id}/sedes`, `PUT /api/users/{id}/sedes` |
| Tipos de usuario | `GET /api/tipos-usuario`, `GET /api/tipos-usuario/{id}`, `POST /api/tipos-usuario`, `PUT /api/tipos-usuario/{id}`, `DELETE /api/tipos-usuario/{id}` |

## Cómo ejecutar

1. Abrir **Laragon** e iniciar MySQL.
2. Ejecutar el script `database/rinconsur_db.sql` del proyecto backend (no borra datos existentes). Usuarios de prueba: `admin@rinconsur.cl` y `maria@rinconsur.cl`.
3. Ejecutar el backend (ver su README). Queda en el puerto 8080.
4. Abrir este proyecto en Android Studio y esperar el Gradle Sync.
5. Con el emulador o celular conectado, ejecutar en una terminal: `adb reverse tcp:8080 tcp:8080` (la app usa `http://127.0.0.1:8080/`).
6. Presionar **Run**.
7. Para que un usuario de tipo Personal vea probadores, un administrador debe asignarle sedes en **Usuarios → editar** (María ya trae la sede 2 con el script).

**Otra dirección del backend:** se cambia solo en `RetrofitClient.kt` (`BASE_URL`). Emulador sin `adb reverse`: `http://10.0.2.2:8080/`. Celular por WiFi: `http://IP-DEL-PC:8080/`.

## Pruebas unitarias (Kotest + MockK, Guía 15)

Se ejecutaron exitosamente dos pruebas unitarias, una por capa, sin errores ni fallos:

| Prueba | Qué comprueba |
|---|---|
| `viewmodels/SedeViewModelTest` | Una sede con nombre inválido no se guarda y se muestra el mensaje de error. |
| `repository/SedeRepositoryTest` | Al refrescar, la lista que entrega el servidor se guarda en Room. |

- Android Studio: clic derecho en `app/src/test` → **Run Tests** (con **Run Tests with Coverage** se ve el porcentaje de cobertura).
- Terminal: `./gradlew testDebugUnitTest`

## APK firmada

La aplicacion fue compilada en la variante Release y firmada mediante Android Studio.

- APK: Probadores_RinconSur.apk
- Archivo de firma: Probadores_RinconSur.jks
- Alias: rinconsur
- Algoritmo: RSA de 2048 bits
- Esquema de firma: APK Signature Scheme v2
- Verificacion mediante apksigner: exitosa.

La APK y el archivo JKS se entregan por separado al docente.

La clave de firma y sus contrasenas no se publican en GitHub.

Para conectar la APK al backend local, iniciar el servidor, conectar el dispositivo por USB y ejecutar:

    adb reverse tcp:8080 tcp:8080

La firma digital fue verificada correctamente. La prueba de ejecucion de la APK Release queda pendiente.
