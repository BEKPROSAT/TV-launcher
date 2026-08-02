package com.aboma.tvlauncher

import android.app.Application
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aboma.tvlauncher.data.LauncherAppInfo
import com.aboma.tvlauncher.data.LauncherRepository
import com.aboma.tvlauncher.data.LocalLauncherStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LauncherUiState(
    val apps: List<LauncherAppInfo> = emptyList(),
    val favorites: List<LauncherAppInfo> = emptyList(),
    val recents: List<LauncherAppInfo> = emptyList(),
    val isLoading: Boolean = true,
    val dexReady: Boolean = false,
    val message: String? = null,
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LauncherRepository(application)
    private val store = LocalLauncherStore(application)

    var state by mutableStateOf(LauncherUiState())
        private set

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val config = getApplication<Application>().resources.configuration
            val apps = withContext(Dispatchers.IO) { repository.loadLaunchableApps() }
            state = state.copy(
                apps = apps,
                favorites = store.favoritePackages().mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } },
                recents = store.recentPackages().mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } },
                isLoading = false,
                dexReady = isDexLike(config),
                message = null,
            )
        }
    }

    fun launch(app: LauncherAppInfo) {
        if (repository.launch(app)) {
            store.recordRecent(app.packageName)
            refreshRowsFromCurrentApps()
        } else {
            Toast.makeText(getApplication(), "Could not open ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleFavorite(app: LauncherAppInfo) {
        store.toggleFavorite(app.packageName)
        refreshRowsFromCurrentApps(message = if (store.isFavorite(app.packageName)) {
            "${app.label} pinned"
        } else {
            "${app.label} unpinned"
        })
    }

    private fun refreshRowsFromCurrentApps(message: String? = null) {
        val apps = state.apps
        state = state.copy(
            favorites = store.favoritePackages().mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } },
            recents = store.recentPackages().mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } },
            message = message,
        )
    }

    private fun isDexLike(configuration: Configuration): Boolean {
        val uiType = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        val wideLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE &&
            configuration.screenWidthDp >= 900
        return uiType == Configuration.UI_MODE_TYPE_DESK ||
            uiType == Configuration.UI_MODE_TYPE_TELEVISION ||
            wideLandscape
    }
}
