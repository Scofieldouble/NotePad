package com.example.notepad;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StickyNoteActivity extends AppCompatActivity {
    private EditText etStickyContent;
    private FloatingActionButton fabSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticky_note);

        initViews();
        setToolbar();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.sticky_note));
        }

        etStickyContent = findViewById(R.id.et_sticky_content);
        fabSave = findViewById(R.id.fab_save);

        fabSave.setOnClickListener(v -> saveStickyNote());
    }

    private void setToolbar() {
        // Toolbar already set in initViews
    }

    private void saveStickyNote() {
        String content = etStickyContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "便签内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建便签笔记
        Note note = new Note("便签", content);
        note.setStickyNote(true);
        note.setCategory("便签");

        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", note);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        saveStickyNote();
        return super.onSupportNavigateUp();
    }
}

