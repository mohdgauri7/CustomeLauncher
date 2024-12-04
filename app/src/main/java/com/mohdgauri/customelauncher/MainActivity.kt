package com.mohdgauri.customelauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.mohdgauri.customelauncher.data.ScreenRoutes
import com.mohdgauri.customelauncher.screens.HomeScreen
import com.mohdgauri.customelauncher.ui.theme.CustomeLauncherTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var appUpdateManager: AppUpdateManager
    private val DAYS_ASK_FOR_FLEXIBLE = 30
    private var currentUpdateType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkForInAppUpdate()
        setContent {
            CustomeLauncherTheme {
                AppNavigation()
            }
        }
    }


    val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            appUpdateManager.completeUpdate()
        }
    }

    private fun checkForInAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        // Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                (appUpdateInfo.updatePriority() >= 4 ||
                        (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_ASK_FOR_FLEXIBLE) &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                startUpdate(appUpdateInfo, AppUpdateType.IMMEDIATE)
            } else if (
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                startUpdate(appUpdateInfo, AppUpdateType.FLEXIBLE)
            }
        }
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                // handle callback
                if (result.resultCode != RESULT_OK) {
                    if (currentUpdateType == AppUpdateType.IMMEDIATE) {
                        finish()
                    }
                }
            }
    }
    fun startUpdate(appUpdateInfo: AppUpdateInfo, appUpdateType: Int) {
        currentUpdateType = appUpdateType
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // an activity result launcher registered via registerForActivityResult
            activityResultLauncher,
            // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
            // flexible updates.
            AppUpdateOptions.newBuilder(appUpdateType).build()
        )
    }

    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            // If the update is downloaded but not installed,
            // notify the user to complete the update.
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                appUpdateManager.completeUpdate()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appUpdateManager.unregisterListener(listener)
    }
}


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ScreenRoutes.HomeScreen.route) {
        composable(route = ScreenRoutes.HomeScreen.route) {
            HomeScreen()
        }
    }
}

