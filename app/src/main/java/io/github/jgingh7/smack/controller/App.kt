package io.github.jgingh7.smack.controller

import android.app.Application
import io.github.jgingh7.smack.utilities.SharedPrefs

// Application class : This is where you have the global base context.
// It is initialized before even any of the main activities of onCreate().
// The very first thing that is created when app is launched.
class App :Application() {

    companion object {
        lateinit var prefs: SharedPrefs
    }

    // initialize shared preferences
    override fun onCreate() {
        prefs = SharedPrefs(applicationContext) // context for the entire application
        super.onCreate()
    }
}