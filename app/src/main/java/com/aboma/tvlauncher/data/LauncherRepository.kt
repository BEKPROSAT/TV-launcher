package com.aboma.tvlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class LauncherRepository(private val context: Context) {
    private val packageManager = context.packageManager

    fun loadLaunchableApps(): List<LauncherAppInfo> {
        val queryIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                queryIntent,
                PackageManager.ResolveInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(queryIntent, 0)
        }

        return activities
            .mapNotNull { resolveInfo ->
                val activity = resolveInfo.activityInfo ?: return@mapNotNull null
                if (activity.packageName == context.packageName) return@mapNotNull null
                LauncherAppInfo(
                    label = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = activity.packageName,
                    activityName = activity.name,
                    icon = resolveInfo.loadIcon(packageManager),
                )
            }
            .distinctBy { "${it.packageName}/${it.activityName}" }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: LauncherAppInfo): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(app.packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }
}
