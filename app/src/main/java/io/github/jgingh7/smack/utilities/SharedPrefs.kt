package io.github.jgingh7.smack.utilities

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {

    val PREFS_FILENAME = "prefs"
    // reference to the shared preferences
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0) // 0 means content private // almost always 0

    // save AuthService's value (isLoggedIn, userEmail, authToken) to the device

    // keys
    val IS_LOGGED_IN = "isLoggedIn"
    val AUTH_TOKEN = "authToken"
    val USER_EMAIL = "userEmail"

    // values to save the AuthService's value
    // need custom getters and setters, because it gets and saves to shared preferences
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false) // if we haven't saved something to this variable IS_LOGGED_IN, then default it to false
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply() // set the value of IS_LOGGED_IN as "value"

    var authToken: String?
        get() = prefs.getString(AUTH_TOKEN, "")
        set(value) = prefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String?
        get() = prefs.getString(USER_EMAIL, "")
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    // creating a new queue request for all the web request (Volley.newRequestQueue(context).add(findUserRequest)) is not a good practice
    // so, add a queue here
    val requestQueue = Volley.newRequestQueue(context)

}