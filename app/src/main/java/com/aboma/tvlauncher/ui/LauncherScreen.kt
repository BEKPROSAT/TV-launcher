package com.aboma.tvlauncher.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.aboma.tvlauncher.LauncherViewModel
import com.aboma.tvlauncher.data.LauncherAppInfo
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class LauncherDestination {
    Home,
    AppDrawer,
    Settings,
}

@Composable
fun LauncherScreen(viewModel: LauncherViewModel) {
    val state = viewModel.state
    var destination by remember { mutableStateOf(LauncherDestination.Home) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF111927),
                        MaterialTheme.colorScheme.background,
                        Color(0xFF070A0F),
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 32.dp),
        ) {
            Header(
                title = when (destination) {
                    LauncherDestination.Home -> "TV Launcher"
                    LauncherDestination.AppDrawer -> "All Apps"
                    LauncherDestination.Settings -> "Settings"
                },
                subtitle = when (destination) {
                    LauncherDestination.Home -> "Pinned apps only. Open All Apps to add more."
                    LauncherDestination.AppDrawer -> "Enter opens. F adds or removes a Home tile."
                    LauncherDestination.Settings -> "Customization, accessibility, wallpaper, widgets, and remote pairing."
                },
                dexReady = state.dexReady,
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (state.isLoading) {
                LoadingState()
            } else {
                when (destination) {
                    LauncherDestination.Home -> HomeScreen(
                        pinnedApps = state.favorites,
                        onLaunch = viewModel::launch,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onOpenDrawer = { destination = LauncherDestination.AppDrawer },
                        onOpenSettings = { destination = LauncherDestination.Settings },
                    )
                    LauncherDestination.AppDrawer -> AppDrawerScreen(
                        apps = state.apps,
                        onLaunch = viewModel::launch,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onBackHome = { destination = LauncherDestination.Home },
                    )
                    LauncherDestination.Settings -> SettingsScreen(
                        onBackHome = { destination = LauncherDestination.Home },
                    )
                }
            }
        }

        SystemStatusBar(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 48.dp),
        )

        AnimatedVisibility(
            visible = state.message != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = state.message.orEmpty(),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun Header(
    title: String,
    subtitle: String,
    dexReady: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.width(260.dp))
    }
}

@Composable
private fun StatusPill(text: String, active: Boolean) {
    val color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SystemStatusBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    var now by remember { mutableStateOf(Date()) }
    var wifiStrength by remember { mutableStateOf(readWifiStrength(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            wifiStrength = readWifiStrength(context)
            delay(30_000)
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.26f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WifiGlyph(strength = wifiStrength)
        Text(
            text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(now),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun readWifiStrength(context: Context): Int {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val network = connectivity?.activeNetwork
    val capabilities = connectivity?.getNetworkCapabilities(network)
    if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) != true) return 0

    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    @Suppress("DEPRECATION")
    val rssi = wifiManager?.connectionInfo?.rssi ?: return 2

    return when {
        rssi >= -55 -> 4
        rssi >= -67 -> 3
        rssi >= -78 -> 2
        rssi >= -88 -> 1
        else -> 0
    }
}

@Composable
private fun WifiGlyph(strength: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.size(width = 24.dp, height = 18.dp),
    ) {
        repeat(4) { index ->
            val active = index < strength
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((6 + index * 4).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (active) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.22f),
                    ),
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Finding installed apps",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun HomeScreen(
    pinnedApps: List<LauncherAppInfo>,
    onLaunch: (LauncherAppInfo) -> Unit,
    onToggleFavorite: (LauncherAppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 184.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 48.dp),
    ) {
        if (pinnedApps.isEmpty()) {
            item {
                ActionTile(
                    title = "Add Apps",
                    subtitle = "Open the app drawer",
                    symbol = "+",
                    onClick = onOpenDrawer,
                )
            }
        }

        items(pinnedApps, key = { "${it.packageName}/${it.activityName}" }) { app ->
            AppTile(
                app = app,
                onLaunch = { onLaunch(app) },
                onToggleFavorite = { onToggleFavorite(app) },
            )
        }

        item {
            ActionTile(
                title = "See All Apps",
                subtitle = "Browse installed apps",
                symbol = "A",
                onClick = onOpenDrawer,
            )
        }

        item {
            ActionTile(
                title = "Settings",
                subtitle = "Wallpaper, widgets, access",
                symbol = "*",
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun AppDrawerScreen(
    apps: List<LauncherAppInfo>,
    onLaunch: (LauncherAppInfo) -> Unit,
    onToggleFavorite: (LauncherAppInfo) -> Unit,
    onBackHome: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBackHome) {
                Text(text = "Home")
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        AppRow(
            title = "Installed Apps",
            emptyText = "No launchable apps were found on this device.",
            apps = apps,
            onLaunch = onLaunch,
            onToggleFavorite = onToggleFavorite,
        )
    }
}

@Composable
private fun SettingsScreen(onBackHome: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp),
    ) {
        item {
            Button(onClick = onBackHome) {
                Text(text = "Home")
            }
        }
        items(
            listOf(
                "Wallpaper" to "Built-in and gallery backgrounds will live here.",
                "Widgets" to "Android widget hosting is planned through AppWidgetHost.",
                "Home Customization" to "Drag, resize, reset layout, and tile defaults.",
                "Accessibility" to "Text size, contrast, focus thickness, and reduce motion.",
                "Remote Pairing" to "PIN or QR pairing for the later companion app.",
                "About" to "Version, build type, and device diagnostics.",
            ),
        ) { section ->
            SettingsSection(title = section.first, body = section.second)
        }
    }
}

@Composable
private fun SettingsSection(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(22.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun AppRow(
    title: String,
    emptyText: String,
    apps: List<LauncherAppInfo>,
    onLaunch: (LauncherAppInfo) -> Unit,
    onToggleFavorite: (LauncherAppInfo) -> Unit,
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (apps.isEmpty()) {
            EmptyRow(text = emptyText)
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
            ) {
                items(apps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                    AppTile(
                        app = app,
                        onLaunch = { onLaunch(app) },
                        onToggleFavorite = { onToggleFavorite(app) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRow(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.68f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    symbol: String,
    onClick: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
        label = "actionTileScale",
    )

    Column(
        modifier = Modifier
            .width(184.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (focused) 1f else 0.72f))
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp &&
                    (event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.DirectionCenter)
                ) {
                    onClick()
                    true
                } else {
                    false
                }
            }
            .clickable(role = Role.Button, onClick = onClick)
            .focusable()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = symbol,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AppTile(
    app: LauncherAppInfo,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val iconBitmap = remember(app.packageName) {
        app.icon.toBitmap(width = 96, height = 96).asImageBitmap()
    }
    val scale by animateFloatAsState(
        targetValue = if (focused) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
        label = "appTileScale",
    )

    Column(
        modifier = Modifier
            .width(184.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (focused) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                },
            )
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
            )
            .onFocusChanged { focused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyUp) return@onKeyEvent false
                when (event.key) {
                    Key.Enter, Key.NumPadEnter, Key.DirectionCenter -> {
                        onLaunch()
                        true
                    }
                    Key.F -> {
                        onToggleFavorite()
                        true
                    }
                    else -> false
                }
            }
            .clickable(
                role = Role.Button,
                onClickLabel = "Open ${app.label}",
                onClick = onLaunch,
            )
            .focusable()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = iconBitmap,
            contentDescription = "${app.label} app icon",
            modifier = Modifier.size(72.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = app.label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (focused) "Enter open  F pin" else app.packageName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
