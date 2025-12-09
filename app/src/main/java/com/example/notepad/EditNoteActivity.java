package com.example.notepad;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity {
    private EditText etNoteTitle;
    private EditText etNoteContent;
    private AutoCompleteTextView categoryAutoComplete;
    private SwitchMaterial switchTodo;
    private MaterialCardView todoOptionsCard;
    private LinearLayout todoOptionsLayout;
    private RadioGroup priorityRadioGroup;
    private MaterialButton btnSetReminder;
    private TextView tvReminderTime;
    private Note currentNote;
    private int position = -1;
    private List<String> categories;
    private Date reminderDate;
    private SimpleDateFormat dateTimeFormat;
    
    // 媒体相关
    private ImageView ivImagePreview;
    private VideoView vvVideoPreview;
    private MaterialButton btnPlayAudio;
    private MaterialCardView cardImagePreview;
    private MaterialCardView cardVideoPreview;
    private MaterialCardView cardAudioPreview;
    private TextView tvAudioFileName;
    private MediaPlayer mediaPlayer;
    private String currentImagePath;
    private String currentAudioPath;
    private String currentVideoPath;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private ActivityResultLauncher<Intent> audioPickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        initViews();
        setToolbar();
        setupActivityResultLaunchers();
        loadNoteData();
    }

    private void initViews() {
        etNoteTitle = findViewById(R.id.et_note_title);
        etNoteContent = findViewById(R.id.et_note_content);
        categoryAutoComplete = findViewById(R.id.category_auto_complete);
        switchTodo = findViewById(R.id.switch_todo);
        todoOptionsCard = findViewById(R.id.todo_options_card);
        todoOptionsLayout = findViewById(R.id.todo_options_layout);
        priorityRadioGroup = findViewById(R.id.priority_radio_group);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        // 初始化分类列表
        categories = new ArrayList<>(Arrays.asList(
            getString(R.string.default_category),
            getString(R.string.work_category),
            getString(R.string.life_category),
            getString(R.string.study_category),
            getString(R.string.other_category)
        ));
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);
        categoryAutoComplete.setOnClickListener(v -> categoryAutoComplete.showDropDown());
        
        // 待办开关监听
        switchTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                todoOptionsCard.setVisibility(View.VISIBLE);
                todoOptionsLayout.setVisibility(View.VISIBLE);
            } else {
                todoOptionsCard.setVisibility(View.GONE);
                todoOptionsLayout.setVisibility(View.GONE);
            }
        });
        
        // 设置提醒按钮
        btnSetReminder.setOnClickListener(v -> showReminderDialog());
        
        // 媒体相关视图
        ivImagePreview = findViewById(R.id.iv_image_preview);
        vvVideoPreview = findViewById(R.id.vv_video_preview);
        btnPlayAudio = findViewById(R.id.btn_play_audio);
        cardImagePreview = findViewById(R.id.card_image_preview);
        cardVideoPreview = findViewById(R.id.card_video_preview);
        cardAudioPreview = findViewById(R.id.card_audio_preview);
        tvAudioFileName = findViewById(R.id.tv_audio_file_name);
        
        MaterialButton btnAddImage = findViewById(R.id.btn_add_image);
        MaterialButton btnAddAudio = findViewById(R.id.btn_add_audio);
        MaterialButton btnAddVideo = findViewById(R.id.btn_add_video);
        MaterialButton btnRemoveImage = findViewById(R.id.btn_remove_image);
        MaterialButton btnRemoveVideo = findViewById(R.id.btn_remove_video);
        MaterialButton btnRemoveAudio = findViewById(R.id.btn_remove_audio);
        
        btnAddImage.setOnClickListener(v -> showImageSourceDialog());
        btnAddAudio.setOnClickListener(v -> showAudioSourceDialog());
        btnAddVideo.setOnClickListener(v -> selectVideo());
        btnRemoveImage.setOnClickListener(v -> removeImage());
        btnRemoveVideo.setOnClickListener(v -> removeVideo());
        btnRemoveAudio.setOnClickListener(v -> removeAudio());
        btnPlayAudio.setOnClickListener(v -> playAudio());
    }
    
    private void setupActivityResultLaunchers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        saveImageFromUri(uri);
                    }
                }
            }
        );
        
        videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        saveVideoFromUri(uri);
                    }
                }
            }
        );
        
        audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        saveAudioFromUri(uri);
                    }
                }
            }
        );
        
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && currentImagePath != null) {
                    loadImagePreview(currentImagePath);
                }
            }
        );
        
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "需要权限才能使用此功能", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        }
    }

    private void loadNoteData() {
        Intent intent = getIntent();
        if (intent.hasExtra("note")) {
            currentNote = (Note) intent.getSerializableExtra("note");
            position = intent.getIntExtra("position", -1);
            etNoteTitle.setText(currentNote.getTitle());
            etNoteContent.setText(currentNote.getContent());
            categoryAutoComplete.setText(currentNote.getCategory());
            
            // 加载待办设置
            if (currentNote.isTodo()) {
                switchTodo.setChecked(true);
                todoOptionsCard.setVisibility(View.VISIBLE);
                todoOptionsLayout.setVisibility(View.VISIBLE);
                
                // 设置优先级
                int priority = currentNote.getPriority();
                if (priority == 0) {
                    priorityRadioGroup.check(R.id.radio_priority_low);
                } else if (priority == 2) {
                    priorityRadioGroup.check(R.id.radio_priority_high);
                } else {
                    priorityRadioGroup.check(R.id.radio_priority_medium);
                }
                
                // 设置提醒时间
                if (currentNote.getReminderDate() != null) {
                    reminderDate = currentNote.getReminderDate();
                    tvReminderTime.setVisibility(View.VISIBLE);
                    tvReminderTime.setText("提醒时间: " + dateTimeFormat.format(reminderDate));
                }
            }
            
            // 加载媒体文件
            if (currentNote.getImagePath() != null && !currentNote.getImagePath().isEmpty()) {
                currentImagePath = currentNote.getImagePath();
                loadImagePreview(currentImagePath);
            }
            if (currentNote.getVideoPath() != null && !currentNote.getVideoPath().isEmpty()) {
                currentVideoPath = currentNote.getVideoPath();
                loadVideoPreview(currentVideoPath);
            }
            if (currentNote.getAudioPath() != null && !currentNote.getAudioPath().isEmpty()) {
                currentAudioPath = currentNote.getAudioPath();
                loadAudioPreview(currentAudioPath);
            }
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.edit_note));
            }
        } else {
            currentNote = new Note("", "");
            categoryAutoComplete.setText(getString(R.string.default_category));
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.add_note));
            }
        }
    }
    
    private void showReminderDialog() {
        Calendar calendar = Calendar.getInstance();
        if (reminderDate != null) {
            calendar.setTime(reminderDate);
        } else {
            calendar.add(Calendar.HOUR_OF_DAY, 1); // 默认1小时后
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, minute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        reminderDate = calendar.getTime();
                        tvReminderTime.setVisibility(View.VISIBLE);
                        tvReminderTime.setText("提醒时间: " + dateTimeFormat.format(reminderDate));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                );
                timePickerDialog.show();
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etNoteTitle.getText().toString().trim();
        String content = etNoteContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            finish();
            return;
        }

        if (title.isEmpty()) {
            title = content.length() > 20 ? content.substring(0, 20) + "..." : content;
        }

        String category = categoryAutoComplete.getText().toString().trim();
        if (category.isEmpty()) {
            category = getString(R.string.default_category);
        }
        
        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setCategory(category);
        currentNote.setModifiedDate(new Date());
        
        // 保存媒体文件路径
        if (currentImagePath != null) {
            currentNote.setImagePath(currentImagePath);
        }
        if (currentVideoPath != null) {
            currentNote.setVideoPath(currentVideoPath);
        }
        if (currentAudioPath != null) {
            currentNote.setAudioPath(currentAudioPath);
        }
        
        // 保存待办设置
        boolean isTodo = switchTodo.isChecked();
        currentNote.setTodo(isTodo);
        
        if (isTodo) {
            // 设置优先级
            int priority = 1; // 默认中
            int checkedId = priorityRadioGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.radio_priority_low) {
                priority = 0;
            } else if (checkedId == R.id.radio_priority_high) {
                priority = 2;
            }
            currentNote.setPriority(priority);
            
            // 设置提醒时间
            if (reminderDate != null) {
                currentNote.setReminderDate(reminderDate);
            }
        } else {
            currentNote.setReminderDate(null);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", currentNote);
        if (position != -1) {
            resultIntent.putExtra("position", position);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        saveNote();
        super.onBackPressed();
    }
    
    // 图片相关方法
    private void showImageSourceDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.from_gallery)};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_image))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        takePhoto();
                    } else {
                        selectImageFromGallery();
                    }
                })
                .show();
    }
    
    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA);
            return;
        }
        
        try {
            File photoFile = createImageFile();
            currentImagePath = photoFile.getAbsolutePath();
            Uri imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);
            
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }
    
    private void saveImageFromUri(Uri uri) {
        try {
            File imageFile = createImageFile();
            currentImagePath = imageFile.getAbsolutePath();
            
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            loadImagePreview(currentImagePath);
        } catch (IOException e) {
            Toast.makeText(this, "保存图片失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadImagePreview(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            ivImagePreview.setImageBitmap(bitmap);
            cardImagePreview.setVisibility(View.VISIBLE);
        }
    }
    
    private void removeImage() {
        currentImagePath = null;
        cardImagePreview.setVisibility(View.GONE);
    }
    
    // 视频相关方法
    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        videoPickerLauncher.launch(intent);
    }
    
    private void saveVideoFromUri(Uri uri) {
        try {
            File videoFile = createVideoFile();
            currentVideoPath = videoFile.getAbsolutePath();
            
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(videoFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            loadVideoPreview(currentVideoPath);
        } catch (IOException e) {
            Toast.makeText(this, "保存视频失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadVideoPreview(String videoPath) {
        vvVideoPreview.setVideoPath(videoPath);
        cardVideoPreview.setVisibility(View.VISIBLE);
    }
    
    private void removeVideo() {
        currentVideoPath = null;
        vvVideoPreview.stopPlayback();
        cardVideoPreview.setVisibility(View.GONE);
    }
    
    // 音频相关方法
    private void showAudioSourceDialog() {
        String[] options = {"录制音频", "从文件选择"};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_audio))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        recordAudio();
                    } else {
                        selectAudioFromGallery();
                    }
                })
                .show();
    }
    
    private void recordAudio() {
        // 简化实现：直接选择音频文件
        // 完整实现需要MediaRecorder
        selectAudioFromGallery();
    }
    
    private void selectAudioFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        audioPickerLauncher.launch(intent);
    }
    
    private void saveAudioFromUri(Uri uri) {
        try {
            File audioFile = createAudioFile();
            currentAudioPath = audioFile.getAbsolutePath();
            
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(audioFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            inputStream.close();
            outputStream.close();
            
            loadAudioPreview(currentAudioPath);
        } catch (IOException e) {
            Toast.makeText(this, "保存音频失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadAudioPreview(String audioPath) {
        File audioFile = new File(audioPath);
        tvAudioFileName.setText(audioFile.getName());
        cardAudioPreview.setVisibility(View.VISIBLE);
    }
    
    private void playAudio() {
        if (currentAudioPath == null) return;
        
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    btnPlayAudio.setText(getString(R.string.play_audio));
                } else {
                    mediaPlayer.start();
                    btnPlayAudio.setText("停止播放");
                }
            } else {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(currentAudioPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                btnPlayAudio.setText("停止播放");
                
                mediaPlayer.setOnCompletionListener(mp -> {
                    btnPlayAudio.setText(getString(R.string.play_audio));
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
            }
        } catch (IOException e) {
            Toast.makeText(this, "播放音频失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void removeAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        currentAudioPath = null;
        cardAudioPreview.setVisibility(View.GONE);
    }
    
    // 文件创建方法
    private File createImageFile() throws IOException {
        String imageFileName = "NOTE_IMG_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    private File createVideoFile() throws IOException {
        String videoFileName = "NOTE_VID_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(videoFileName, ".mp4", storageDir);
    }
    
    private File createAudioFile() throws IOException {
        String audioFileName = "NOTE_AUD_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(audioFileName, ".mp3", storageDir);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
