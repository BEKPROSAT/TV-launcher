package com.aboma.tvlauncher.data

import android.graphics.drawable.Drawable

data class LauncherAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable,
)
