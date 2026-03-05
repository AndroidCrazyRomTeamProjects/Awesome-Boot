package org.crazyromteam.qmgstore.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.crazyromteam.qmgstore.R
import org.crazyromteam.qmgstore.api.ThemeItem

class ThemeAdapter(private var themes: List<ThemeItem> = emptyList()) :
    RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

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
        val theme = themes[position]
        holder.nameText.text = theme.name
        holder.creatorText.text = theme.creator

        Glide.with(holder.itemView.context)
            .load(theme.previewUrl)
            .centerCrop()
            .into(holder.previewImage)
    }

    override fun getItemCount() = themes.size

    fun updateData(newThemes: List<ThemeItem>) {
        themes = newThemes
        notifyDataSetChanged()
    }
}
