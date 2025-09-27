package com.example.wellnesstracker.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter // Import AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Build // Import Build class for API level checks
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat // Recommended for getColor for API < 23
import androidx.core.view.isGone // For View.isGone KTX extension
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.MoodEntry
import com.example.wellnesstracker.utils.PreferencesManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat // Import SimpleDateFormat
import java.util.Calendar
import java.util.Date // Import Date
import java.util.Locale // Import Locale
import java.util.concurrent.TimeUnit

class MoodFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: MoodAdapter
    private lateinit var moods: MutableList<MoodEntry>
    private var selectedEmoji: String = "🙂" // default emoji for selection

    private lateinit var moodLineChart: LineChart
    private lateinit var btnToggleChart: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        prefs = PreferencesManager(requireContext())
        moods = prefs.loadMoods()

        val etNote = view.findViewById<EditText>(R.id.etMoodNote)
        val fabSave = view.findViewById<FloatingActionButton>(R.id.fabSaveMood)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMoods)

        moodLineChart = view.findViewById(R.id.moodLineChart)
        btnToggleChart = view.findViewById(R.id.btnToggleChart)

        adapter = MoodAdapter(moods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

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

        fabSave.setOnClickListener {
            val entry = MoodEntry(
                emoji = selectedEmoji,
                moodLevel = MoodEntry.getMoodLevelFromEmoji(selectedEmoji),
                note = etNote.text.toString()
            )

            moods.add(0, entry)
            prefs.saveMoods(moods)
            adapter.updateList(moods)
            etNote.text.clear()

            updateChart()
            Toast.makeText(requireContext(), "Mood saved ✅", Toast.LENGTH_SHORT).show()
        }

        btnToggleChart.setOnClickListener {
            if (moodLineChart.isGone) { // Use isGone KTX property
                moodLineChart.isGone = false // Show chart
                recyclerView.isGone = true // Hide list
                btnToggleChart.setText(R.string.show_mood_history) // Use string resource
                updateChart()
            } else {
                moodLineChart.isGone = true // Hide chart
                recyclerView.isGone = false // Show list
                btnToggleChart.setText(R.string.show_mood_trend) // Use string resource
            }
        }

        setupChart()
        updateChart()

        return view
    }

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
        scaleUp.addListener(object : AnimatorListenerAdapter() { // Fixed: animation? parameter
            override fun onAnimationEnd(animation: Animator) { // Fixed: non-nullable Animator
                scaleDown.start()
            }
        })
    }

    private fun setupChart() {
        moodLineChart.description.isEnabled = false
        moodLineChart.legend.isEnabled = false
        moodLineChart.setNoDataText("Log some moods to see your trend!")

        val xAxis = moodLineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textSize = 10f

        val leftAxis = moodLineChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.setAxisMinimum(0f)
        leftAxis.setAxisMaximum(6f)
        leftAxis.granularity = 1f
        leftAxis.labelCount = 6
        leftAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("", "😔", "", "😐", "", "😄", ""))

        moodLineChart.axisRight.isEnabled = false

        moodLineChart.setTouchEnabled(true)
        moodLineChart.setPinchZoom(true)
    }

    private fun updateChart() {
        if (moods.isEmpty()) {
            moodLineChart.clear()
            return
        }

        val entries = ArrayList<Entry>()
        val calendar = Calendar.getInstance()
        val sevenDaysAgo = calendar.timeInMillis - TimeUnit.DAYS.toMillis(7)

        val moodsLast7Days = moods
            .filter { it.timestamp >= sevenDaysAgo }
            .sortedBy { it.timestamp }

        val xLabels = ArrayList<String>()
        val sevenDaysList = ArrayList<String>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            // Use MoodEntry helper for date formatting
            sevenDaysList.add(MoodEntry.getFormattedDateFromTimestamp(calendar.timeInMillis))
        }

        for ((index, dayLabel) in sevenDaysList.withIndex()) {
            val moodsOnDay = moodsLast7Days.filter { MoodEntry.getFormattedDateFromTimestamp(it.timestamp) == dayLabel }
            if (moodsOnDay.isNotEmpty()) {
                val averageMood = moodsOnDay.map { it.moodLevel }.average().toFloat()
                entries.add(Entry(index.toFloat(), averageMood))
            } else {
                entries.add(Entry(index.toFloat(), 3f)) // Default to neutral if no mood logged
            }
            xLabels.add(dayLabel)
        }

        val dataSet = LineDataSet(entries, "Mood Trend")

        // --- API 23+ getColor fix ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // M = API 23
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary_blue)
            dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
        } else {
            // Fallback for API < 23 (e.g., just use default colors or define simpler ones)
            dataSet.color = android.graphics.Color.BLUE
            dataSet.valueTextColor = android.graphics.Color.BLACK
            dataSet.setCircleColor(android.graphics.Color.GREEN)
        }

        dataSet.lineWidth = 2f
        dataSet.circleRadius = 5f
        dataSet.setDrawCircleHole(false)

        val lineData = LineData(dataSet)
        moodLineChart.data = lineData

        moodLineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels.toTypedArray())
        moodLineChart.xAxis.labelCount = xLabels.size
        moodLineChart.xAxis.granularity = 1f

        moodLineChart.invalidate()
    }
}