package io.github.jgingh7.smack.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import io.github.jgingh7.smack.utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    var isLoggedIn = false
    var userEmail = ""
    var authToken = ""

    // sends a json body request (email, password)
    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            // no json body sent back
            println(response)
            println("registeruser about to be trued")
            complete(true)
        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not register user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(registerRequest)
    }

    // sends a json body request (email, password)
    // gets back a json API response (user (the email), token)
    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {response ->
            //this is where we parse the json object
            println(response)

            try {
                userEmail = response.getString("user")
                authToken = response.getString("token")
                isLoggedIn = true
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXE:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {error ->
            //this is where we deal with our error
            Log.d("ERROR", "Could not login user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        Volley.newRequestQueue(context).add(loginRequest)
    }

    // sends a json header (token) and body request (name, email, avatarName, avatarColor)
    // gets back a json API response (email, name, avatarName, avatarColor, id)
    fun createUser(context: Context, name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        // the order should be the same as the json request
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)
        val requestBody = jsonBody.toString()

        val createRequest = object : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener {response ->

            try {
                // this order does not matter
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXE:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not add user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            // sends the token through the header
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $authToken")
                return headers
            }
        }

        Volley.newRequestQueue(context).add(createRequest)
    }

    // once login, get the Auth token and the user email
    // use that email to retrieve body information of user (avatarColor, avatarName, email, name)
    // no json request (because this is a GET request)
    // gets json API response (email, name, avatarName, avatarColor, id)
    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findUserRequest = object : JsonObjectRequest(Method.GET, "$URL_GET_USER$userEmail", null, Response.Listener {response ->

            try {
                UserDataService.name = response.getString("name")
                UserDataService.email = response.getString("email")
                UserDataService.avatarName = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                UserDataService.id = response.getString("_id")

                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)

                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXE:" + e.localizedMessage)
                complete(false)
            }

        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not find user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            // sends the token through the header
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer $authToken")
                return headers
            }
        }

        Volley.newRequestQueue(context).add(findUserRequest)
    }
}