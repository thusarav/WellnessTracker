package com.example.wellnesstracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.Habit

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val onCheckedChange: (Habit, Boolean) -> Unit,
    private val onDelete: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val cbCompleted: CheckBox = itemView.findViewById(R.id.cbHabitDone)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteHabit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvHabitName.text = habit.name
        holder.cbCompleted.isChecked = habit.isCompletedToday

        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(habit, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateList(newList: MutableList<Habit>) {
        habits = newList
        notifyDataSetChanged()
    }
}