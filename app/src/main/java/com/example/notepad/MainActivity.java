package com.example.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteClickListener {
    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> notes;
    private List<Note> allNotes;
    private FloatingActionButton fabAddNote;
    private TextInputEditText searchEditText;
    private ChipGroup categoryChipGroup;
    private Toolbar toolbar;
    private String selectedCategory = null;
    private int sortMode = 0; // 0=时间, 1=标题, 2=分类, 3=优先级
    private boolean showTodoOnly = false;
    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_EDIT_NOTE = 2;
    private static final int REQUEST_CODE_OCR = 3;
    private static final int REQUEST_CODE_STICKY_NOTE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 检查是否已登录
        if (!UserStorage.isLoggedIn(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_main);

        initViews();
        initNotes();
        initRecyclerView();
        setListeners();
        updateToolbarTitle();
    }
    
    private void updateToolbarTitle() {
        String currentUser = UserStorage.getCurrentUser(this);
        if (currentUser != null && toolbar != null) {
            toolbar.setTitle(getString(R.string.welcome, currentUser));
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        fabAddNote = findViewById(R.id.fab_add_note);
        searchEditText = findViewById(R.id.search_edit_text);
        categoryChipGroup = findViewById(R.id.category_chip_group);
    }

    private void initNotes() {
        allNotes = NoteStorage.loadNotes(this);
        notes = new ArrayList<>(allNotes);
        // 如果没有保存的笔记，添加一些示例笔记
        if (notes.isEmpty()) {
            notes.add(new Note("示例笔记1", "这是第一条示例笔记的内容，你可以点击编辑或者长按删除。", "默认"));
            notes.add(new Note("示例笔记2", "这是第二条示例笔记的内容，展示了如何在记事本应用中创建和管理笔记。", "工作"));
            NoteStorage.saveNotes(this, notes);
            allNotes = new ArrayList<>(notes);
        }
        initCategoryChips();
    }
    
    private void initCategoryChips() {
        categoryChipGroup.removeAllViews();
        
        // 添加"全部"选项
        Chip allChip = new Chip(this);
        allChip.setText(getString(R.string.all_categories));
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCategory = null;
                filterNotes();
            }
        });
        categoryChipGroup.addView(allChip);
        
        // 获取所有分类
        Set<String> categories = new HashSet<>();
        for (Note note : allNotes) {
            categories.add(note.getCategory());
        }
        
        // 添加分类选项
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category;
                    filterNotes();
                }
            });
            categoryChipGroup.addView(chip);
        }
    }

    private void initRecyclerView() {
        notesAdapter = new NotesAdapter(notes, this);
        notesRecyclerView.setAdapter(notesAdapter);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setListeners() {
        fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
        });
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void filterNotes() {
        String query = searchEditText.getText().toString().toLowerCase().trim();
        List<Note> filtered = new ArrayList<>();
        
        for (Note note : allNotes) {
            // 分类过滤
            if (selectedCategory != null && !note.getCategory().equals(selectedCategory)) {
                continue;
            }
            
            // 待办过滤
            if (showTodoOnly && !note.isTodo()) {
                continue;
            }
            
            // 搜索过滤
            if (query.isEmpty() || 
                note.getTitle().toLowerCase().contains(query) || 
                note.getContent().toLowerCase().contains(query)) {
                filtered.add(note);
            }
        }
        
        // 排序
        sortNotes(filtered);
        
        notes = filtered;
        notesAdapter.updateNotes(notes);
    }
    
    private void sortNotes(List<Note> notesList) {
        Collections.sort(notesList, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                switch (sortMode) {
                    case 1: // 按标题
                        return n1.getTitle().compareToIgnoreCase(n2.getTitle());
                    case 2: // 按分类
                        int catCompare = n1.getCategory().compareToIgnoreCase(n2.getCategory());
                        if (catCompare != 0) return catCompare;
                        return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                    case 3: // 按优先级
                        int priorityCompare = Integer.compare(n2.getPriority(), n1.getPriority());
                        if (priorityCompare != 0) return priorityCompare;
                        return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                    default: // 按时间（默认）
                        return n2.getModifiedDate().compareTo(n1.getModifiedDate());
                }
            }
        });
    }

    @Override
    public void onNoteClick(int position) {
        // 检查位置是否有效
        if (position < 0 || position >= notes.size()) {
            Toast.makeText(this, "无法打开笔记：无效的位置", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra("note", notes.get(position));
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE);
    }

    @Override
    public void onNoteLongClick(int position) {
        // 检查位置是否有效（双重保险）
        if (position < 0 || position >= notes.size()) {
            Toast.makeText(this, "删除失败：无效的位置", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 保存Note对象而不是position，避免列表变化导致position失效
        Note noteToDelete = notes.get(position);
        if (noteToDelete == null) {
            Toast.makeText(this, "删除失败：笔记不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 直接删除，不弹窗确认
        deleteNote(noteToDelete);
    }

    private void deleteNote(Note noteToDelete) {
        if (noteToDelete == null) {
            Toast.makeText(this, "删除失败：笔记不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 删除前自动创建备份，方便恢复
        BackupManager.backupNotes(this, new ArrayList<>(allNotes));
        
        // 从过滤后的notes列表中删除
        // 先尝试直接匹配对象引用（更高效）
        int positionInFiltered = notes.indexOf(noteToDelete);
        // 如果找不到，通过匹配标题、内容和日期来查找
        if (positionInFiltered < 0) {
            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                if (note.getTitle().equals(noteToDelete.getTitle()) &&
                    note.getContent().equals(noteToDelete.getContent()) &&
                    note.getDate().equals(noteToDelete.getDate())) {
                    positionInFiltered = i;
                    break;
                }
            }
        }
        
        if (positionInFiltered >= 0) {
            notes.remove(positionInFiltered);
            notesAdapter.notifyItemRemoved(positionInFiltered);
        }
        
        // 从allNotes列表中删除
        // 先尝试直接匹配对象引用
        boolean removed = allNotes.remove(noteToDelete);
        // 如果没找到，通过匹配标题、内容和日期来查找并删除
        if (!removed) {
            for (int i = 0; i < allNotes.size(); i++) {
                Note note = allNotes.get(i);
                if (note.getTitle().equals(noteToDelete.getTitle()) &&
                    note.getContent().equals(noteToDelete.getContent()) &&
                    note.getDate().equals(noteToDelete.getDate())) {
                    allNotes.remove(i);
                    break;
                }
            }
        }
        
        NoteStorage.saveNotes(this, allNotes);
        initCategoryChips();
        Toast.makeText(this, "笔记已删除（已自动备份，可通过恢复功能恢复）", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 5 && data != null) {
                // 语音搜索结果
                String query = data.getStringExtra("search_query");
                if (query != null && searchEditText != null) {
                    searchEditText.setText(query);
                    filterNotes();
                }
            } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                Note newNote = (Note) data.getSerializableExtra("note");
                allNotes.add(newNote);
                NoteStorage.saveNotes(this, allNotes);
                filterNotes();
                initCategoryChips();
                ReminderManager.setReminder(this, newNote);
            } else if (requestCode == REQUEST_CODE_EDIT_NOTE) {
                Note editedNote = (Note) data.getSerializableExtra("note");
                int position = data.getIntExtra("position", -1);
                if (position != -1 && position < notes.size()) {
                    Note oldNote = notes.get(position);
                    // 更新 allNotes 中的笔记
                    for (int i = 0; i < allNotes.size(); i++) {
                        Note note = allNotes.get(i);
                        if (note.getTitle().equals(oldNote.getTitle()) && 
                            note.getContent().equals(oldNote.getContent()) &&
                            note.getDate().equals(oldNote.getDate())) {
                            allNotes.set(i, editedNote);
                            break;
                        }
                    }
                    NoteStorage.saveNotes(this, allNotes);
                    filterNotes();
                    initCategoryChips();
                    ReminderManager.setReminder(this, editedNote);
                }
            } else if (requestCode == REQUEST_CODE_OCR) {
                Note ocrNote = (Note) data.getSerializableExtra("note");
                if (ocrNote != null) {
                    allNotes.add(ocrNote);
                    NoteStorage.saveNotes(this, allNotes);
                    filterNotes();
                    initCategoryChips();
                }
            } else if (requestCode == REQUEST_CODE_STICKY_NOTE) {
                Note stickyNote = (Note) data.getSerializableExtra("note");
                if (stickyNote != null) {
                    allNotes.add(stickyNote);
                    NoteStorage.saveNotes(this, allNotes);
                    filterNotes();
                    initCategoryChips();
                }
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 更新所有提醒
        ReminderManager.updateAllReminders(this, allNotes);
    }
    
    private void startVoiceSearch() {
        Intent intent = new Intent(this, VoiceSearchActivity.class);
        startActivityForResult(intent, 5);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            exportNotes();
            return true;
        } else if (id == R.id.action_settings) {
            // 打开设置界面
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_todo) {
            showTodoOnly = !showTodoOnly;
            item.setChecked(showTodoOnly);
            filterNotes();
            Toast.makeText(this, showTodoOnly ? "显示待办事项" : "显示所有笔记", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_backup) {
            showBackupDialog();
            return true;
        } else if (id == R.id.action_restore) {
            showRestoreDialog();
            return true;
        } else if (id == R.id.action_ocr) {
            Intent intent = new Intent(this, OCRActivity.class);
            startActivityForResult(intent, REQUEST_CODE_OCR);
            return true;
        } else if (id == R.id.action_sticky_note) {
            Intent intent = new Intent(this, StickyNoteActivity.class);
            startActivityForResult(intent, REQUEST_CODE_STICKY_NOTE);
            return true;
        } else if (id == R.id.action_voice_search) {
            startVoiceSearch();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showSortDialog() {
        String[] sortOptions = {
            getString(R.string.sort_by_time),
            getString(R.string.sort_by_title),
            getString(R.string.sort_by_category),
            getString(R.string.sort_by_priority)
        };
        
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sort))
                .setSingleChoiceItems(sortOptions, sortMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sortMode = which;
                        filterNotes();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void showBackupDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.backup))
                .setMessage("确定要备份所有笔记吗？")
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (BackupManager.backupNotes(MainActivity.this, allNotes)) {
                            Toast.makeText(MainActivity.this, getString(R.string.backup_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "备份失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void showRestoreDialog() {
        String[] backupFiles = BackupManager.getBackupFiles(this);
        if (backupFiles.length == 0) {
            Toast.makeText(this, "没有找到备份文件", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.restore))
                .setItems(backupFiles, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Note> restoredNotes = BackupManager.restoreNotes(MainActivity.this, backupFiles[which]);
                        if (restoredNotes != null) {
                            // 恢复笔记，保持备份时的原始顺序
                            allNotes = restoredNotes;
                            NoteStorage.saveNotes(MainActivity.this, allNotes);
                            
                            // 检查是否有过滤条件
                            String query = searchEditText.getText().toString().toLowerCase().trim();
                            boolean hasFilter = !query.isEmpty() || selectedCategory != null || showTodoOnly;
                            
                            if (hasFilter) {
                                // 有过滤条件时，应用过滤但使用时间排序以尽量保持备份顺序
                                int savedSortMode = sortMode;
                                sortMode = 0; // 临时设置为按时间排序
                                filterNotes();
                                sortMode = savedSortMode; // 恢复原来的排序模式
                            } else {
                                // 没有过滤条件时，直接使用恢复的笔记列表，完全保持备份时的顺序
                                notes = new ArrayList<>(allNotes);
                                notesAdapter.updateNotes(notes);
                            }
                            
                            initCategoryChips();
                            Toast.makeText(MainActivity.this, getString(R.string.restore_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "恢复失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void logout() {
        UserStorage.logout(this);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void exportNotes() {
        try {
            File exportDir = new File(getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            String fileName = "notes_export_" + sdf.format(new java.util.Date()) + ".txt";
            File exportFile = new File(exportDir, fileName);
            
            FileWriter writer = new FileWriter(exportFile);
            writer.write("=== 笔记导出 ===\n");
            writer.write("导出时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new java.util.Date()) + "\n");
            writer.write("共 " + allNotes.size() + " 条笔记\n\n");
            
            for (int i = 0; i < allNotes.size(); i++) {
                Note note = allNotes.get(i);
                writer.write("--- 笔记 " + (i + 1) + " ---\n");
                writer.write("分类: " + note.getCategory() + "\n");
                writer.write("标题: " + note.getTitle() + "\n");
                writer.write("内容: " + note.getContent() + "\n");
                writer.write("时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(note.getDate()) + "\n");
                writer.write("\n");
            }
            
            writer.close();
            Toast.makeText(this, getString(R.string.export_success) + ": " + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.export_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在应用关闭前保存笔记
        NoteStorage.saveNotes(this, allNotes);
    }
}