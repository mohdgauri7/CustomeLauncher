package com.mohdgauri.customelauncher.data

sealed class ScreenRoutes(val route: String) {

    data object HomeScreen : ScreenRoutes("HomeScreen")

}