package com.example.projectjarvis.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.apply

class AutomationAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutomationAccessibilityService? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        this.serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Optional: log foreground app for debugging
        val packageName = event?.packageName?.toString()
        if (packageName != null) {
            Log.d("JarvisAccessibility", "Foreground app: $packageName")
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    // Function to close app if it is foreground
    fun closeAppIfForeground(targetPackage: String): Boolean {
        val currentPackage = rootInActiveWindow?.packageName?.toString()
        return if (currentPackage == targetPackage) {
            performGlobalAction(GLOBAL_ACTION_BACK) // Minimizes app
            true
        } else {
            false
        }
    }
}