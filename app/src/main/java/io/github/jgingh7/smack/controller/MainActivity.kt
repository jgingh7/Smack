package io.github.jgingh7.smack.controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.jgingh7.smack.R
import io.github.jgingh7.smack.adapters.ChannelListAdapter
import io.github.jgingh7.smack.model.Channel
import io.github.jgingh7.smack.services.AuthService
import io.github.jgingh7.smack.services.MessageService
import io.github.jgingh7.smack.services.UserDataService
import io.github.jgingh7.smack.utilities.BROADCAST_USER_DATA_CHANGE
import io.github.jgingh7.smack.utilities.SOCKET_URL
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    var selectedChannel: Channel? = null // if not logged in, no selectedChannel

    //listview version
//    lateinit var channelAdapter: ArrayAdapter<Channel>
//
//    private fun setupAdapters() {
//        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
//        channel_list.adapter = channelAdapter
//    }

    lateinit var channelAdapter: ChannelListAdapter

    private fun setupAdapters() {
//        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
//        channel_list.adapter = channelAdapter
        channelAdapter = ChannelListAdapter(this, MessageService.channels) { channel ->
            selectedChannel = channel
            drawer_layout.closeDrawer(GravityCompat.START) // close the drawer
            updateWithChannel()
        }
        channel_list.adapter = channelAdapter

        val layoutManager = LinearLayoutManager(this)
        channel_list.layoutManager = layoutManager
    }

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        socket.connect()
        // listening for a specific event called "channelCreated",
        // and if detected, use listener called onNewChannel
        socket.on("channelCreated", onNewChannel) // "channelCreated" in the API code
        // if put on onResume() socket connect happens twice when a channel is made

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
            R.id.nav_gallery,
            R.id.nav_slideshow
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        setupAdapters()

        // for ListView
//        channel_list.setOnItemClickListener { _, _, i, _ ->
//            selectedChannel = MessageService.channels[i]
//            drawer_layout.closeDrawer(GravityCompat.START)
//            updateWithChannel()
//        }

        // check to see if logged in
        // if logged in find user
        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }
    }

    override fun onResume() {
        // broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGE)) // finding the specific broadcast

        super.onResume()
    }

    override fun onDestroy() {
        socket.disconnect()

        // you should unregister the broadcast receiver when leaving this activity
        // unregistering broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)

        super.onDestroy()
    }

    // creating broadcast receiver
    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            //update nav header UI
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable",
                    packageName) // UserDataService.(image String)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginBtnNavHeader.text = "Logout"

                MessageService.getChannels { complete ->
                    if (complete) {
                        if (MessageService.channels.count() > 0) {
                            selectedChannel = MessageService.channels[0] // have the first channel as selectedChannel by default
                            channelAdapter.notifyDataSetChanged() // telling adapter to reload the recycle view because the data set changed
                            updateWithChannel()
                        }
                    }
                }
            }
        }
    }

    // a called function whenever we click on (select) a new channel from the list of channels
    fun updateWithChannel() {
        mainChannelName.text = "${selectedChannel?.toString()}"
        // download messages for channel
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun loginBtnNavClicked(view: View) {

        if (App.prefs.isLoggedIn) { // when logged in, click this button to logout
            UserDataService.logout()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginBtnNavHeader.text = "Login"

        } else { // when not logged in, click this button to login
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    fun addChannelClicked(view: View) {
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)

            builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ -> // when clicked "add"
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()

                    // create channel with the channel name and description
                    socket.emit("newChannel", channelName, channelDesc) // the API is listening to "newChannel" // the order here matters because that is how the API is set up to listen
                }
                .setNegativeButton("Cancel") { _, _ ->
                    // Cancel and close the dialog
                }
                .show()
        }
    }

    // emitter listener (listens to the emissions of the API)
    // when we receive a new channel
    private val onNewChannel = Emitter.Listener { args -> // the call back is on a worker thread
        runOnUiThread { // so runOnUiThread to execute on UI thread
            val channelName = args[0] as String // args is of Any type, so need to cast it to String object
            val channelDescription = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDescription, channelId)
            MessageService.channels.add(newChannel)
            channelAdapter.notifyDataSetChanged() // immediately after the addition of channel, it pops up on the recycle view
        }
    }

    fun sendMsgBtnClicked(view: View) {
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}
