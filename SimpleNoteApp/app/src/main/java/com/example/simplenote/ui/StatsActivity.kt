package com.example.simplenote.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.simplenote.R
import com.example.simplenote.data.NoteStorage

class StatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val tvNoteCount: TextView = findViewById(R.id.tvNoteCount)
        val notes = NoteStorage.loadNotes(this)
        tvNoteCount.text = "共 ${notes.size} 条笔记"
    }
}



