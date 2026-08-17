package com.uvaustralia.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Schedule and trigger the worker; the worker itself exits early if no
        // widget instances are on the home screen, so there is no wasted work.
        UvWidgetWorker.schedule(context)
        UvWidgetWorker.runNow(context)
    }
}
