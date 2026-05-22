package com.example.tienda_app.util

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

object AccessibilityHelper {

    /**
     * Checks if native TalkBack or any other touch exploration service is enabled on the system.
     */
    fun isSystemTalkBackEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        return am?.isTouchExplorationEnabled == true
    }

    /**
     * Announces a message to the user. Uses view-based accessibility announcements which are
     * spoken out by TalkBack if active, and also triggers if custom audio assistant is active.
     */
    fun announce(view: View, message: String) {
        val context = view.context
        val isSystemTalkBack = isSystemTalkBackEnabled(context)
        val isCustomAudioEnabled = SettingsManager.getInstance(context).audioAssistant

        if (isSystemTalkBack || isCustomAudioEnabled) {
            // Standard accessibility announcement
            view.announceForAccessibility(message)
            
            // If TalkBack isn't running but our custom helper is, send an AccessibilityEvent
            // to make sure accessibility services (if any are active in different modes) read it,
            // or perform a fallback view announcement.
            if (!isSystemTalkBack && isCustomAudioEnabled) {
                val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT).apply {
                    className = view.javaClass.name
                    packageName = context.packageName
                    text.add(message)
                }
                val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                if (am != null && am.isEnabled) {
                    am.sendAccessibilityEvent(event)
                }
            }
        }
    }
}
