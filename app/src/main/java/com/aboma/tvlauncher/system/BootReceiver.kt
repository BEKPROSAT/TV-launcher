package com.aboma.tvlauncher.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aboma.tvlauncher.MainActivity

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Samsung DeX behavior varies by One UI version. Boot launch is conservative here:
        // it opens the launcher after boot, while future DeX-specific receivers can refine this.
        val launchIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    }
}
