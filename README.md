# TV Launcher

Samsung DeX-focused Android launcher for turning a Galaxy S20+ into a TV streaming launcher.

## Current Milestone

- Kotlin + Jetpack Compose Android app
- Designed for landscape DeX/TV use
- Home screen shows pinned apps only
- Dedicated **See All Apps** tile opens the App Drawer
- Visible **Settings** tile on the Home screen
- Persistent top-right status bar with time and Wi-Fi signal estimate
- Large focusable app tiles
- Keyboard, D-pad, and mouse-friendly navigation
- Installed app discovery through `PackageManager`
- Home pinning with `F` key support
- Debug APK build through GitHub Actions

## No Android Studio Workflow

1. Push this repository to GitHub.
2. Open the repository's **Actions** tab.
3. Run or wait for **Build Debug APK**.
4. Open the completed workflow run.
5. Download the `tv-launcher-debug-apk` artifact.
6. Extract it and sideload `app-debug.apk` onto the Samsung phone.

## Sideloading

On the phone:

1. Copy `app-debug.apk` to the device.
2. Open the APK from Files or your browser downloads.
3. If prompted, enable **Install unknown apps** for that source.
4. Install **TV Launcher**.
5. Open it once, then optionally set it as the default Home app.

## TV Controls

- Arrow keys or D-pad: move focus
- Enter or center/select: open focused app
- Mouse click: open app
- `F`: pin or unpin focused app from the App Drawer or focused app tile

## Planned Next

- DeX connect/disconnect handling
- Drag-and-drop Home screen layout
- Tile resizing with persisted positions and sizes
- Full Settings implementation
- Wallpaper picker
- Android widget hosting
- Accessibility controls including high contrast and reduce motion
- Custom web tiles
- Best-effort landscape launch behavior for third-party apps
- Phone-as-remote companion app
- Optional kiosk/screen-pinning mode
