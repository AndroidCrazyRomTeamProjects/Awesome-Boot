package org.crazyromteam.qmgstore.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.api.ThemeItem

class ThemeAdapter :
    ListAdapter<ThemeItem, ThemeAdapter.ThemeViewHolder>(ThemeDiffCallback()) {

    class ThemeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val previewImage: ImageView = view.findViewById(R.id.themePreview)
        val nameText: TextView = view.findViewById(R.id.themeName)
        val creatorText: TextView = view.findViewById(R.id.themeCreator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = getItem(position)
        holder.nameText.text = theme.name
        holder.creatorText.text = theme.creator

        Glide.with(holder.itemView.context)
            .load(theme.previewUrl)
            .centerCrop()
            .into(holder.previewImage)
    }

    fun updateData(newThemes: List<ThemeItem>) {
        submitList(newThemes)
    }
}

class ThemeDiffCallback : DiffUtil.ItemCallback<ThemeItem>() {
    override fun areItemsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
        // Use name and creator to uniquely identify an item, as there is no specific ID field
        return oldItem.name == newItem.name && oldItem.creator == newItem.creator
    }

    override fun areContentsTheSame(oldItem: ThemeItem, newItem: ThemeItem): Boolean {
        return oldItem == newItem
    }
}
