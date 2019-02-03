package com.firebase.chatapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_message.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.DataViewHolder>() {

    private val data = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.DataViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.text.text = data[position].text
        holder.name.text = data[position].name
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.messageTextView
        val name: TextView = view.nameTextView
    }

    fun swapData(message: Message) {
        data.add(message)
        notifyDataSetChanged()
    }
}

//val isPhoto = message!!.photoUrl != null
//if (isPhoto) {
//    messageTextView.visibility = View.GONE
//    photoImageView.visibility = View.VISIBLE
//    Glide.with(photoImageView.context)
//            .load(message.photoUrl)
//            .into(photoImageView)
//} else {
//    messageTextView.visibility = View.VISIBLE
//    photoImageView.visibility = View.GONE
//    messageTextView.text = message.text
//}
//authorTextView.text = message.name
