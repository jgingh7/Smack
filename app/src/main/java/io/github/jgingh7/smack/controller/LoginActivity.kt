package io.github.jgingh7.smack.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import io.github.jgingh7.smack.R
import io.github.jgingh7.smack.services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginLoginBtnClicked(view: View) {
        enableSpinner(true)
        val email = loginEmailTxt.text.toString()
        val password = loginPasswordTxt.text.toString()

        var currState = ""

        if (email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.loginUser(email, password) { loginSuccess ->
                currState = "Login user"
                if (loginSuccess) {
                    AuthService.findUserByEmail(this) { findSuccess ->
                        currState = "Find user"
                        if (findSuccess) {
                            enableSpinner(false)
                            finish()
                        }
                        else {
                            errorToast(currState)
                        }
                    }
                } else {
                    errorToast(currState)
                }
            }
        } else {
            Toast.makeText(this, "Please fill in both eamil and password", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }

    fun loginUserBtnClicked(view: View) {
        val createUserIntent = Intent(this, CreateUserActivity::class.java)
        startActivity(createUserIntent)
        finish() // finish LoginActivity
                 // if not stays on activity_login layout after createUser Activity is finished
    }

    // if login was unsuccessful
    fun errorToast(currState: String) {
        Toast.makeText(this, "$currState went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    // disabling others when spinner is on
    fun enableSpinner(enable: Boolean) {
        if (enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginEmailTxt.isEnabled = !enable
        loginPasswordTxt.isEnabled = !enable
        loginLoginBtn.isEnabled = !enable
        loginCreateUserBtn.isEnabled = !enable
    }
}
