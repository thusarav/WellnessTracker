package com.example.wellnesstracker.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MoodFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: MoodAdapter
    private lateinit var moods: MutableList<MoodEntry>
    private var selectedEmoji: String = "🙂"

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
            Toast.makeText(requireContext(), getString(R.string.mood_saved_toast), Toast.LENGTH_SHORT).show()
        }

        btnToggleChart.setOnClickListener {
            if (moodLineChart.isGone) {
                moodLineChart.isGone = false
                recyclerView.isGone = true
                btnToggleChart.setText(R.string.show_mood_history)
                updateChart()
            } else {
                moodLineChart.isGone = true
                recyclerView.isGone = false
                btnToggleChart.setText(R.string.show_mood_trend)
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
        scaleUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                scaleDown.start()
            }
        })
    }

    private fun setupChart() {
        moodLineChart.description.isEnabled = false
        moodLineChart.legend.isEnabled = false
        moodLineChart.setNoDataText(getString(R.string.chart_no_data))

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
            sevenDaysList.add(MoodEntry.getFormattedDateFromTimestamp(calendar.timeInMillis))
        }

        for ((index, dayLabel) in sevenDaysList.withIndex()) {
            val moodsOnDay = moodsLast7Days.filter { MoodEntry.getFormattedDateFromTimestamp(it.timestamp) == dayLabel }
            if (moodsOnDay.isNotEmpty()) {
                val averageMood = moodsOnDay.map { it.moodLevel }.average().toFloat()
                entries.add(Entry(index.toFloat(), averageMood))
            } else {
                entries.add(Entry(index.toFloat(), 3f))
            }
            xLabels.add(dayLabel)
        }

        val dataSet = LineDataSet(entries, "Mood Trend") // This "Mood Trend" is for internal legend, not user-facing

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dataSet.color = ContextCompat.getColor(requireContext(), R.color.primary_blue)
            dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            dataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
        } else {
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