package io.github.jgingh7.smack.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jgingh7.smack.R
import io.github.jgingh7.smack.model.Channel

class ChannelListAdapter(private val context: Context, private val channels: ArrayList<Channel>, private val itemClick: (Channel) -> Unit) : RecyclerView.Adapter<ChannelListAdapter.Holder>() {
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val channelName = itemView.findViewById<TextView>(R.id.channelName)

        fun bindCategory(channel: Channel, context: Context) {
            channelName?.text = channel.toString()
            itemView.setOnClickListener { itemClick(channel) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.channel_list, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return channels.count()
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bindCategory(channels[position], context)
    }
}