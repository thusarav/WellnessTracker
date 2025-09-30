package com.example.wellnesstracker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.Habit
import com.example.wellnesstracker.utils.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<Habit>
    private lateinit var tvCompletion: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        prefs = PreferencesManager(requireContext())
        habits = prefs.loadHabits()

        tvCompletion = view.findViewById(R.id.tvCompletion)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerHabits)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddHabit)

        adapter = HabitAdapter(
            habits,
            onCheckedChange = { habit, isChecked ->
                habit.isCompletedToday = isChecked
                updateCompletion()
                prefs.saveHabits(habits)
            },
            onDelete = { habit ->
                habits.remove(habit)
                adapter.updateList(habits)
                updateCompletion()
                prefs.saveHabits(habits)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Added layout manager
        recyclerView.adapter = adapter

        fabAdd.setOnClickListener { showAddHabitDialog() }

        updateCompletion()
        return view
    }

    private fun updateCompletion() {
        if (habits.isEmpty()) {
            tvCompletion.text = getString(R.string.habits_completion_default)
        } else {
            val doneCount = habits.count { it.isCompletedToday }
            val percent = (doneCount * 100) / habits.size
            tvCompletion.text = getString(R.string.habits_completion_progress, percent)
        }
    }

    private fun showAddHabitDialog() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.dialog_habit_name_hint)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_new_habit_title)
            .setView(input)
            .setPositiveButton(R.string.button_add) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newHabit = Habit(name = name)
                    habits.add(newHabit)
                    adapter.updateList(habits)
                    updateCompletion()
                    prefs.saveHabits(habits)
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }
}