package io.github.jgingh7.smack.controller

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.github.jgingh7.smack.R
import io.github.jgingh7.smack.services.AuthService
import io.github.jgingh7.smack.utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE // invisible Spinner
    }

    fun generateUserAvatar(view: View) {
        val random = Random()
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        if (color == 0) {
            userAvatar = "light$avatar"
        } else {
            userAvatar = "dark$avatar"
        }
        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        createAvatarImageView.setImageResource(resourceId)
    }

    fun generateColorClicked(view: View) {
        val random = Random()
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserClicked(view: View) {
        enableSpinner(true)

        val userName = createUserNameTxt.text.toString()
        val email = createEmailTxt.text.toString()
        val password = createPasswordTxt.text.toString()

        if (userName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) { //make sure non of the three are empty
            var currState = ""

            AuthService.registerUser(email, password) { registerSuccess ->
                currState = "Register user"
                if (registerSuccess) {
                    AuthService.loginUser(email, password) { loginSuccess ->
                        currState = "Login user"
                        if (loginSuccess) {
                            AuthService.createUser(userName, email, userAvatar, avatarColor) { createSuccess ->
                                currState = "Create user"
                                if (createSuccess) {
                                    //local broadcast when create user is successful
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                    enableSpinner(false)
                                    finish() //dismiss an activity
                                } else {
                                    errorToast(currState)
                                }
                            }
                        } else {
                            errorToast(currState)
                        }
                    }
                } else {
                    errorToast(currState)
                }
            }
        } else {
            Toast.makeText(this, "Make sure user name, email, and password are filled in.", Toast.LENGTH_LONG).show()
            enableSpinner(false)
        }
    }

    // if register, login, or create was unsuccessful
    fun errorToast(currState: String) {
        Toast.makeText(this, "$currState went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    // disabling others when spinner is on
    fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        createUserNameTxt.isEnabled = !enable
        createEmailTxt.isEnabled = !enable
        createPasswordTxt.isEnabled = !enable
        createUserBtn.isEnabled = !enable
        createAvatarImageView.isEnabled = !enable
        backgroundColorBtn.isEnabled = !enable
    }

}
