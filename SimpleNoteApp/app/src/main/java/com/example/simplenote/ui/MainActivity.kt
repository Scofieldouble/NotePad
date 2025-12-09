package com.example.simplenote.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplenote.R
import com.example.simplenote.data.Note
import com.example.simplenote.data.NoteStorage
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvNotes: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var tvEmpty: TextView
    private lateinit var btnAbout: ImageButton

    private val notes = mutableListOf<Note>()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvNotes = findViewById(R.id.rvNotes)
        fabAdd = findViewById(R.id.fabAdd)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnAbout = findViewById(R.id.btnAbout)

        adapter = NoteAdapter(
            notes,
            onItemClick = { note ->
                // 点击进入编辑
                val intent = Intent(this, EditNoteActivity::class.java)
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id)
                startActivity(intent)
            },
            onItemLongClick = { note ->
                showDeleteDialog(note)
            }
        )
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        fabAdd.setOnClickListener {
            startActivity(Intent(this, EditNoteActivity::class.java))
        }

        btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次返回主界面时刷新列表
        val loaded = NoteStorage.loadNotes(this)
        adapter.updateData(loaded)
        tvEmpty.visibility = if (loaded.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("删除笔记")
            .setMessage("确定要删除这条笔记吗？")
            .setPositiveButton("删除") { _, _ ->
                val list = NoteStorage.loadNotes(this)
                val newList = list.filter { it.id != note.id }
                NoteStorage.saveNotes(this, newList)
                adapter.updateData(newList)
                tvEmpty.visibility = if (newList.isEmpty()) View.VISIBLE else View.GONE
            }
            .setNegativeButton("取消", null)
            .show()
    }
}


