package com.example.wellnesstracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.MoodEntry

class MoodAdapter(private var moods: MutableList<MoodEntry>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        val tvNote: TextView = itemView.findViewById(R.id.tvMoodNote)
        val tvTime: TextView = itemView.findViewById(R.id.tvMoodTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        // Show emoji + note
        holder.tvEmoji.text = mood.emoji
        holder.tvNote.text = if (mood.note.isNotEmpty()) mood.note else "(no note)"

        // Use MoodEntry helper functions for formatted date/time
        holder.tvTime.text = "${mood.getFormattedDate()} • ${mood.getFormattedTime()}"
    }

    override fun getItemCount(): Int = moods.size

    fun updateList(newList: MutableList<MoodEntry>) {
        moods = newList
        notifyDataSetChanged()
    }
}