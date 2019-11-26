package com.firebase.chatapplication

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.chatapplication.model.Message
import com.firebase.chatapplication.utils.inflateItem
import com.squareup.picasso.Picasso
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_message.*

class ListAdapter: RecyclerView.Adapter<DataViewHolder>() {

    private var messages = emptyList<Message>()
    var onDeleteClick: ((String, String) -> Unit)? = null

    fun setMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DataViewHolder(parent)

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(messages[position], ::onItemClicked)
    }

    private fun onItemClicked(photoUrl: String, key: String) {
        onDeleteClick?.invoke(photoUrl, key)
    }
}

class DataViewHolder(
    parent: ViewGroup,
    override val containerView: View = parent.inflateItem(R.layout.item_message)
): RecyclerView.ViewHolder(containerView), LayoutContainer {

    fun bind(message: Message, onDeleteClick: (String, String) -> Unit) = with(message) {

        nameTextView.text = name

        if (photoUrl != null) {
            messageTextView.visibility = View.GONE
            photoImageView.visibility = View.VISIBLE
            Picasso.get()
                .load(photoUrl)
                .into(photoImageView)
        } else {
            messageTextView.visibility = View.VISIBLE
            photoImageView.visibility = View.GONE
            messageTextView.text = text
        }

        photoImageView.setOnCreateContextMenuListener { contextMenu, _, _ ->
            contextMenu.setHeaderTitle("Select Action:")

            contextMenu.add(Menu.NONE, 1, 1, "Delete").setOnMenuItemClickListener {
                onDeleteClick.invoke(photoUrl!!, key!!)
                true
            }
        }
    }
}