package com.example.simplenote.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.simplenote.R
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteStorage

class EditNoteActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: TextView

    private var currentNoteId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)

        btnBack.setOnClickListener { finish() }

        // 读取传进来的笔记 ID，判断是新建还是编辑
        currentNoteId = if (intent.hasExtra(EXTRA_NOTE_ID)) {
            intent.getLongExtra(EXTRA_NOTE_ID, -1L).takeIf { it > 0 }
        } else {
            null
        }

        currentNoteId?.let { id ->
            val notes = NoteStorage.loadNotes(this)
            val note = notes.find { it.id == id }
            if (note != null) {
                etTitle.setText(note.title)
                etContent.setText(note.content)
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "内容是空的，没必要保存哦～", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val notes = NoteStorage.loadNotes(this)
            val now = System.currentTimeMillis()

            if (currentNoteId == null) {
                // 新建
                val newId = NoteStorage.generateId(notes)
                val newNote = Note(
                    id = newId,
                    title = title,
                    content = content,
                    time = now
                )
                notes.add(0, newNote)
            } else {
                // 修改
                val idx = notes.indexOfFirst { it.id == currentNoteId }
                if (idx >= 0) {
                    notes[idx] = notes[idx].copy(
                        title = title,
                        content = content,
                        time = now
                    )
                }
            }

            NoteStorage.saveNotes(this, notes)
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}




