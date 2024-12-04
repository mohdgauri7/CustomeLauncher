package com.mohdgauri.customelauncher.viewmodels

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohdgauri.customelauncher.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var originalList: List<AppInfo> = emptyList()
    var homeData by mutableStateOf(HomeUIStates())
        private set

    fun getInstalledApps(context: Context) {
        homeData = homeData.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            val packageManager = context.packageManager
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps = packages.mapNotNull { app ->
                if (packageManager.getLaunchIntentForPackage(app.packageName) != null) {
                    val appName = packageManager.getApplicationLabel(app).toString()
                    val drawable = packageManager.getApplicationIcon(app.packageName)
                    val iconBitmap = drawableToBitmap(drawable)
                    AppInfo(name = appName, packageName = app.packageName, icon = iconBitmap)
                } else {
                    null
                }
            }
            originalList = apps
            homeData = homeData.copy(isLoading = false, installedApps = apps)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun searchUpdate(query: String){
        if(query.isEmpty()){
            homeData = homeData.copy(isLoading = false, installedApps = originalList)
        }else{
            val filteredApps = originalList.filter { app ->
                app.packageName.contains(query, ignoreCase = true) || app.name.contains(query, ignoreCase = true)
            }
            homeData = homeData.copy(isLoading = false, installedApps = filteredApps)
        }
    }



    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                // App not found, show an error message or handle accordingly
                Toast.makeText(context, "App not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to launch app", Toast.LENGTH_SHORT).show()
        }
    }
    fun uninstallApp(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        // This flag allows the uninstall intent to be handled by the system
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            context.startActivity(intent)
            getInstalledApps(context)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, "App Uninstalled", Toast.LENGTH_SHORT).show()
        }
    }

}


data class HomeUIStates(
    var isLoading: Boolean = false,
    var installedApps: List<AppInfo> = emptyList()
)