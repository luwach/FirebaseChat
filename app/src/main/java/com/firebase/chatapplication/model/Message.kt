package com.firebase.chatapplication.model

data class Message(val text: String? = "", val name: String = "", val photoUrl: String? = null, var key: String? = null)
