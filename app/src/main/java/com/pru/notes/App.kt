package com.pru.notes

import android.app.Application
import com.pru.notes.help.ApplicationLifecycleHandler

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val handler = ApplicationLifecycleHandler()
        registerActivityLifecycleCallbacks(handler)
        registerComponentCallbacks(handler)
    }
}