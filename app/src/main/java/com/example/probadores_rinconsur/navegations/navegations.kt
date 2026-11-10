
package com.example.probadores_rinconsur.navegations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.probadores_rinconsur.repository.models.esAdministrador
import com.example.probadores_rinconsur.view.ui.EstadisticasScreen
import com.example.probadores_rinconsur.view.ui.HistorialScreen
import com.example.probadores_rinconsur.view.ui.HomeScreen
import com.example.probadores_rinconsur.view.ui.LoginScreen
import com.example.probadores_rinconsur.view.ui.ProbadorFormScreen
import com.example.probadores_rinconsur.view.ui.ProbadorListScreen
import com.example.probadores_rinconsur.view.ui.ReporteFormScreen
import com.example.probadores_rinconsur.view.ui.ReporteListScreen
import com.example.probadores_rinconsur.view.ui.SedeScreen
import com.example.probadores_rinconsur.view.ui.UserFormScreen
import com.example.probadores_rinconsur.view.ui.UserListScreen
import com.example.probadores_rinconsur.view.utils.navEnterTransition
import com.example.probadores_rinconsur.view.utils.navExitTransition
import com.example.probadores_rinconsur.view.utils.navPopEnterTransition
import com.example.probadores_rinconsur.view.utils.navPopExitTransition
import com.example.probadores_rinconsur.viewmodels.EstadisticasViewModel
import com.example.probadores_rinconsur.viewmodels.HistorialViewModel
import com.example.probadores_rinconsur.viewmodels.LoginViewModel
import com.example.probadores_rinconsur.viewmodels.ProbadorViewModel
import com.example.probadores_rinconsur.viewmodels.ReporteViewModel
import com.example.probadores_rinconsur.viewmodels.SedeViewModel
import com.example.probadores_rinconsur.viewmodels.TipoUsuarioViewModel
import com.example.probadores_rinconsur.viewmodels.UserViewModel
import kotlinx.coroutines.launch

/**
 * Rutas de navegación de RinconSur.
 */
object Routes {

    const val LOGIN = "login"
    const val HOME = "home"
    const val PROBADORES = "probadores"
    const val PROBADOR_FORM = "probador_form?id={id}"
    const val REPORTES = "reportes"
    const val REPORTE_FORM = "reporte_form?id={id}&probadorId={probadorId}"
    const val HISTORIAL = "historial"
    const val USERS = "users"
    const val USER_FORM = "user_form?id={id}"
    const val SEDES = "sedes"
    const val ESTADISTICAS = "estadisticas"

    fun probadorForm(id: Int = 0) =
        "probador_form?id=$id"

    fun reporteForm(
        id: Int = 0,
        probadorId: Int = 0
    ) = "reporte_form?id=$id&probadorId=$probadorId"

    fun userForm(id: Int = 0) =
        "user_form?id=$id"
}

/**
 * Elementos del menú lateral.
 */
data class MenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val soloAdmin: Boolean = false
)

val menuItems = listOf(

    MenuItem(
        Routes.HOME,
        "Inicio",
        Icons.Default.Home
    ),

    MenuItem(
        Routes.PROBADORES,
        "Probadores",
        Icons.Default.Checkroom
    ),

    MenuItem(
        Routes.REPORTES,
        "Reportes de prendas",
        Icons.Default.Description
    ),

    MenuItem(
        Routes.HISTORIAL,
        "Historial",
        Icons.Default.History
    ),

    MenuItem(
        Routes.USERS,
        "Usuarios",
        Icons.Default.People,
        soloAdmin = true
    ),

    MenuItem(
        Routes.SEDES,
        "Sedes",
        Icons.Default.Storefront,
        soloAdmin = true
    ),

    MenuItem(
        Routes.ESTADISTICAS,
        "Estadísticas de prendas",
        Icons.Default.BarChart,
        soloAdmin = true
    )
)

/**
 * Título que aparece en la barra superior.
 */
private fun titleFor(route: String?): String =
    when (route) {

        Routes.HOME -> "Inicio"
        Routes.PROBADORES -> "Probadores"
        Routes.PROBADOR_FORM -> "Probador"
        Routes.REPORTES -> "Reportes de prendas"
        Routes.REPORTE_FORM -> "Reporte"
        Routes.HISTORIAL -> "Historial"
        Routes.USERS -> "Usuarios"
        Routes.USER_FORM -> "Usuario"
        Routes.SEDES -> "Sedes"
        Routes.ESTADISTICAS -> "Estadísticas de prendas"

        else -> ""
    }

/**
 * Navegación principal de la aplicación.
 *
 * Identifica al usuario conectado y su rol.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    reporteViewModel: ReporteViewModel,
    historialViewModel: HistorialViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    sedeViewModel: SedeViewModel,
    estadisticasViewModel: EstadisticasViewModel
) {

    val navController = rememberNavController()

    val drawerState =
        rememberDrawerState(DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    // Usuario autenticado
    val loginState by
    loginViewModel.uiState.collectAsStateWithLifecycle()

    // Identificar al administrador
    val esAdmin =
        loginState.loggedUser?.esAdministrador() == true

    // Identificador del usuario conectado
    val currentUserId =
        loginState.loggedUser?.id

    val isLogin =
        currentRoute == null ||
                currentRoute == Routes.LOGIN

    val isTopLevel =
        menuItems.any {
            it.route == currentRoute
        }

    fun navigateTopLevel(route: String) {

        // Impedir navegación desde el menú
        // hacia opciones exclusivas del administrador.
        val opcion = menuItems.find {
            it.route == route
        }

        if (opcion?.soloAdmin == true && !esAdmin) {
            return
        }

        navController.navigate(route) {

            popUpTo(Routes.HOME) {
                saveState = false
            }

            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,

        drawerContent = {

            ModalDrawerSheet {

                Spacer(
                    Modifier.height(16.dp)
                )

                Text(
                    text = loginState.loggedUser?.name ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(
                        horizontal = 24.dp
                    )
                )

                Text(
                    text = loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 4.dp
                    )
                )

                HorizontalDivider(
                    Modifier.padding(vertical = 8.dp)
                )

                // El personal no ve las opciones
                // exclusivas del administrador.
                menuItems
                    .filter {
                        esAdmin || !it.soloAdmin
                    }
                    .forEach { item ->

                        NavigationDrawerItem(
                            label = {
                                Text(item.label)
                            },

                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = null
                                )
                            },

                            selected =
                                currentRoute == item.route,

                            onClick = {

                                scope.launch {
                                    drawerState.close()
                                }

                                navigateTopLevel(item.route)
                            },

                            modifier = Modifier.padding(
                                NavigationDrawerItemDefaults.ItemPadding
                            )
                        )
                    }

                HorizontalDivider(
                    Modifier.padding(vertical = 8.dp)
                )

                NavigationDrawerItem(
                    label = {
                        Text("Cerrar sesión")
                    },

                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null
                        )
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        loginViewModel.logout()

                        navController.navigate(Routes.LOGIN) {

                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    },

                    modifier = Modifier.padding(
                        NavigationDrawerItemDefaults.ItemPadding
                    )
                )
            }
        }
    ) {

        Scaffold(
            topBar = {

                if (!isLogin) {

                    TopAppBar(
                        title = {
                            Text(titleFor(currentRoute))
                        },

                        navigationIcon = {

                            if (isTopLevel) {

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Menu,
                                        contentDescription = "Menú"
                                    )
                                }

                            } else {

                                IconButton(
                                    onClick = {
                                        navController.popBackStack()
                                    }
                                ) {

                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            }
                        },

                        colors =
                            TopAppBarDefaults.topAppBarColors(
                                containerColor =
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                    )
                }
            }
        ) { innerPadding ->

            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),

                loginViewModel = loginViewModel,
                probadorViewModel = probadorViewModel,
                reporteViewModel = reporteViewModel,
                historialViewModel = historialViewModel,
                userViewModel = userViewModel,
                tipoUsuarioViewModel = tipoUsuarioViewModel,
                sedeViewModel = sedeViewModel,
                estadisticasViewModel = estadisticasViewModel,

                currentUserId = currentUserId,
                currentUserName =
                    loginState.loggedUser?.name ?: "",

                esAdmin = esAdmin,
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

/**
 * Contiene las pantallas y rutas de la aplicación.
 */
@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    reporteViewModel: ReporteViewModel,
    historialViewModel: HistorialViewModel,
    userViewModel: UserViewModel,
    tipoUsuarioViewModel: TipoUsuarioViewModel,
    sedeViewModel: SedeViewModel,
    estadisticasViewModel: EstadisticasViewModel,
    currentUserId: Int?,
    currentUserName: String,
    esAdmin: Boolean,
    onNavigateTopLevel: (String) -> Unit
) {

    val idArg = listOf(
        navArgument("id") {
            type = NavType.IntType
            defaultValue = 0
        }
    )

    val reporteArgs = listOf(

        navArgument("id") {
            type = NavType.IntType
            defaultValue = 0
        },

        navArgument("probadorId") {
            type = NavType.IntType
            defaultValue = 0
        }
    )

    // Animaciones de navegación existentes
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,

        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {

        // INICIO DE SESIÓN
        composable(Routes.LOGIN) {

            LoginScreen(loginViewModel) {

                navController.navigate(Routes.HOME) {

                    popUpTo(Routes.LOGIN) {
                        inclusive = true
                    }
                }
            }
        }

        // INICIO
        composable(Routes.HOME) {

            HomeScreen(
                userName = currentUserName,
                esAdmin = esAdmin,
                onNavigate = onNavigateTopLevel
            )
        }

        // LISTADO DE PROBADORES
        composable(Routes.PROBADORES) {

            ProbadorListScreen(
                viewModel = probadorViewModel,
                userViewModel = userViewModel,
                currentUserId = currentUserId,
                esAdmin = esAdmin,

                onAdd = {
                    navController.navigate(
                        Routes.probadorForm()
                    )
                },

                onEdit = { id ->
                    navController.navigate(
                        Routes.probadorForm(id)
                    )
                },

                onReportar = { id ->
                    navController.navigate(
                        Routes.reporteForm(probadorId = id)
                    )
                }
            )
        }

        // FORMULARIO DE PROBADORES
        composable(
            Routes.PROBADOR_FORM,
            arguments = idArg
        ) { entry ->

            ProbadorFormScreen(
                viewModel = probadorViewModel,

                probadorId =
                    entry.arguments?.getInt("id") ?: 0,

                onDone = {
                    navController.popBackStack()
                }
            )
        }

        // LISTADO DE REPORTES
        composable(Routes.REPORTES) {

            ReporteListScreen(
                viewModel = reporteViewModel,

                onAdd = {
                    navController.navigate(
                        Routes.reporteForm()
                    )
                },

                onEdit = { id ->
                    navController.navigate(
                        Routes.reporteForm(id)
                    )
                }
            )
        }

        // FORMULARIO DE REPORTES
        composable(
            Routes.REPORTE_FORM,
            arguments = reporteArgs
        ) { entry ->

            ReporteFormScreen(
                viewModel = reporteViewModel,

                reporteId =
                    entry.arguments?.getInt("id") ?: 0,

                probadorIdInicial =
                    entry.arguments?.getInt("probadorId") ?: 0,

                onDone = {
                    navController.popBackStack()
                }
            )
        }

        // HISTORIAL
        // Próximo paso: conectar currentUserId
        // y esAdmin para filtrar las sedes.
        composable(Routes.HISTORIAL) {

            HistorialScreen(
                viewModel = historialViewModel
            )
        }

        // USUARIOS: SOLO ADMINISTRADOR
        composable(Routes.USERS) {

            if (esAdmin) {

                UserListScreen(
                    viewModel = userViewModel,
                    tipoUsuarioViewModel = tipoUsuarioViewModel,
                    currentUserId = currentUserId,

                    onAdd = {
                        navController.navigate(
                            Routes.userForm()
                        )
                    },

                    onEdit = { id ->
                        navController.navigate(
                            Routes.userForm(id)
                        )
                    }
                )

            } else {
                AccesoRestringido()
            }
        }

        // FORMULARIO DE USUARIOS: SOLO ADMIN
        composable(
            Routes.USER_FORM,
            arguments = idArg
        ) { entry ->

            if (esAdmin) {

                UserFormScreen(
                    viewModel = userViewModel,
                    tipoUsuarioViewModel = tipoUsuarioViewModel,
                    sedeViewModel = sedeViewModel,

                    userId =
                        entry.arguments?.getInt("id") ?: 0,

                    onDone = {
                        navController.popBackStack()
                    }
                )

            } else {
                AccesoRestringido()
            }
        }

        // SEDES: SOLO ADMINISTRADOR
        composable(Routes.SEDES) {

            if (esAdmin) {

                SedeScreen(
                    sedeViewModel
                )

            } else {
                AccesoRestringido()
            }
        }

        // ESTADÍSTICAS: SOLO ADMINISTRADOR
        composable(Routes.ESTADISTICAS) {

            if (esAdmin) {

                EstadisticasScreen(
                    estadisticasViewModel
                )

            } else {
                AccesoRestringido()
            }
        }
    }
}

/**
 * Mensaje para pantallas exclusivas
 * del administrador.
 */
@Composable
private fun AccesoRestringido() {

    Text(
        text = "Solo el Administrador puede acceder a esta pantalla.",
        modifier = Modifier.padding(16.dp)
    )
}
