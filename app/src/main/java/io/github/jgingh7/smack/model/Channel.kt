package io.github.jgingh7.smack.model

class Channel (val name: String, val description: String, val id: String) {
    // showing the name of the channel
    override fun toString(): String {
        return "#$name"
    }
}