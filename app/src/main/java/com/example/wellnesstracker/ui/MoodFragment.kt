package com.example.wellnesstracker.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.MoodEntry
import com.example.wellnesstracker.utils.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MoodFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: MoodAdapter
    private lateinit var moods: MutableList<MoodEntry>
    private var selectedEmoji: String = "🙂" // default emoji

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate fragment layout
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        // Initialize SharedPreferences helper
        prefs = PreferencesManager(requireContext())
        moods = prefs.loadMoods()

        // UI components
        val etNote = view.findViewById<EditText>(R.id.etMoodNote)
        val fabSave = view.findViewById<FloatingActionButton>(R.id.fabSaveMood)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMoods)

        // Setup RecyclerView
        adapter = MoodAdapter(moods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Emoji selectors with bounce animation
        view.findViewById<TextView>(R.id.emojiHappy).setOnClickListener {
            selectedEmoji = "😄"
            animateEmoji(it)
        }

        view.findViewById<TextView>(R.id.emojiNeutral).setOnClickListener {
            selectedEmoji = "😐"
            animateEmoji(it)
        }

        view.findViewById<TextView>(R.id.emojiSad).setOnClickListener {
            selectedEmoji = "😔"
            animateEmoji(it)
        }

        // Save Mood (via FAB)
        fabSave.setOnClickListener {
            val entry = MoodEntry(
                emoji = selectedEmoji,
                note = etNote.text.toString()
            )

            moods.add(0, entry)              // Add at top of list
            prefs.saveMoods(moods)           // Persist to SharedPreferences
            adapter.updateList(moods)        // Refresh RecyclerView
            etNote.text.clear()              // Clear note input

            Toast.makeText(requireContext(), "Mood saved ✅", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    /**
     * Bounce animation when an emoji is selected (scale up then back down)
     */
    private fun animateEmoji(view: View) {
        val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.3f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.3f)
        )
        scaleUp.duration = 150

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)
        )
        scaleDown.duration = 150

        scaleUp.start()
        scaleUp.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scaleDown.start()
            }
        })
    }
}