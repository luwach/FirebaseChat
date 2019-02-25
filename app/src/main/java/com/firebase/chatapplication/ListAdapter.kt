package com.firebase.chatapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_message.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.DataViewHolder>() {

    private val data = ArrayList<Message>()
    var onItemClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.DataViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.name.text = data[position].name

        if (data[position].photoUrl != null) {
            holder.text.visibility = View.GONE
            holder.photo.visibility = View.VISIBLE
            Picasso.get()
                    .load(data[position].photoUrl)
                    .into(holder.photo)
        } else {
            holder.text.visibility = View.VISIBLE
            holder.photo.visibility = View.GONE
            holder.text.text = data[position].text
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class DataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.messageTextView
        val name: TextView = view.nameTextView
        val photo: ImageView = view.photoImageView

        init {
            photo.setOnClickListener { onItemClick?.invoke(data[adapterPosition].photoUrl!!) }
        }
    }

    fun swapData(message: Message) {
        data.add(message)
        notifyDataSetChanged()
    }

    fun clearData() {
        data.clear()
    }
}