# 📱 robadores RinconSur

## Descripción del proyecto

**Probadores RinconSur** es una aplicación móvil desarrollada para facilitar la gestión y organización de los probadores de una tienda de ropa.

Su objetivo es permitir que el personal pueda consultar el estado de los probadores, registrar su uso y reportar prendas olvidadas. También cuenta con funciones de administración para mantener organizada la información de usuarios, sedes y probadores.

## Funcionalidades principales

- **Inicio de sesión:** permite ingresar a la aplicación con una cuenta de usuario.
- **Gestión de sedes:** permite trabajar con las distintas sucursales de RinconSur.
- **Control de probadores:** permite registrar y consultar información sobre los probadores.
- **Registro de ocupación:** mantiene información sobre el uso de los probadores.
- **Reporte de prendas olvidadas:** permite registrar prendas encontradas e incorporar una fotografía.
- **Administración de usuarios:** permite gestionar los usuarios de la aplicación.
- **Historial y estadísticas:** permite consultar registros e información relacionada con el funcionamiento de los probadores.

## Herramientas utilizadas

El proyecto fue desarrollado en **Android Studio**, utilizando:

- **Kotlin:** lenguaje de programación de la aplicación.
- **Jetpack Compose:** para crear las pantallas.
- **Room:** para almacenar la información en una base de datos local.
- **MVVM:** para organizar el código y separar las pantallas de la lógica de la aplicación.

## Organización del proyecto

El código está organizado en diferentes carpetas para facilitar su desarrollo:

- **models:** contiene los datos que utiliza la aplicación.
- **data:** permite guardar y consultar la información.
- **viewmodels:** gestiona la información y las acciones de cada pantalla.
- **view/ui:** contiene las pantallas que visualiza el usuario.
- **navegations:** permite desplazarse entre las distintas pantallas.

## Ejecución del proyecto

Para ejecutar la aplicación:

1. Abrir el proyecto en Android Studio.
2. Esperar que se carguen las dependencias.
3. Seleccionar un emulador Android.
4. Presionar **Run** para iniciar la aplicación.

## Objetivo final

Desarrollar una aplicación sencilla y funcional que ayude al personal de RinconSur a organizar los probadores, registrar información y facilitar las tareas diarias dentro de las distintas sedes.
