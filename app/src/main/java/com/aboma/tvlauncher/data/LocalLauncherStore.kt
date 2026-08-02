package com.aboma.tvlauncher.data

import android.content.Context

class LocalLauncherStore(context: Context) {
    private val preferences = context.getSharedPreferences("launcher_store", Context.MODE_PRIVATE)

    fun favoritePackages(): List<String> = preferences.getString(KEY_FAVORITES, "")
        .orEmpty()
        .split("|")
        .filter { it.isNotBlank() }

    fun recentPackages(): List<String> = preferences.getString(KEY_RECENTS, "")
        .orEmpty()
        .split("|")
        .filter { it.isNotBlank() }

    fun isFavorite(packageName: String): Boolean = packageName in favoritePackages()

    fun toggleFavorite(packageName: String) {
        val next = favoritePackages().toMutableList()
        if (packageName in next) {
            next.remove(packageName)
        } else {
            next.add(0, packageName)
        }
        preferences.edit().putString(KEY_FAVORITES, next.distinct().joinToString("|")).apply()
    }

    fun recordRecent(packageName: String) {
        val next = mutableListOf(packageName)
        next.addAll(recentPackages().filterNot { it == packageName })
        preferences.edit().putString(KEY_RECENTS, next.take(12).joinToString("|")).apply()
    }

    private companion object {
        const val KEY_FAVORITES = "favorites"
        const val KEY_RECENTS = "recents"
    }
}
