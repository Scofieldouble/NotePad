package com.example.notepad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OCRActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private static final String TAG = "OCRActivity";
    private static final int MAX_IMAGE_DIMENSION = 1024; // 最大图片尺寸
    
    private ImageView imageView;
    private EditText etOcrResult;
    private ProgressBar progressBar;
    private TextRecognizer textRecognizer;
    private Uri imageUri;
    private Bitmap currentBitmap;
    private boolean isTakingPhoto = false;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        initViews();
        initTextRecognizer();
        setupActivityResultLaunchers();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.ocr));
        }

        imageView = findViewById(R.id.image_view);
        etOcrResult = findViewById(R.id.et_ocr_result);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.btn_select_image).setOnClickListener(v -> showImageSourceDialog());
        
        // 添加测试图片生成功能（长按选择图片按钮）
        findViewById(R.id.btn_select_image).setOnLongClickListener(v -> {
            generateTestImage();
            return true;
        });
    }

    private void initTextRecognizer() {
        // 使用中文文本识别器（对中文识别更准确）
        try {
            ChineseTextRecognizerOptions options = new ChineseTextRecognizerOptions.Builder().build();
            textRecognizer = TextRecognition.getClient(options);
        } catch (Exception e) {
            // 如果中文识别器不可用，使用通用识别器
            Log.w(TAG, "中文识别器不可用，使用通用识别器: " + e.getMessage());
            textRecognizer = TextRecognition.getClient(com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS);
        }
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && imageUri != null) {
                    loadImageFromUri(imageUri);
                }
            }
        );

        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        loadImageFromUri(uri);
                    }
                }
            }
        );

        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // 权限已授予，重新尝试操作
                    if (isTakingPhoto) {
                        takePhoto();
                    } else {
                        selectFromGallery();
                    }
                } else {
                    String message = isTakingPhoto ? 
                        getString(R.string.camera_permission_required) : 
                        getString(R.string.storage_permission_required);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void showImageSourceDialog() {
        String[] options = {getString(R.string.take_photo), getString(R.string.from_gallery)};
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_image))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        isTakingPhoto = true;
                        takePhoto();
                    } else {
                        isTakingPhoto = false;
                        selectFromGallery();
                    }
                })
                .show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("需要相机权限")
                        .setMessage("拍照功能需要相机权限")
                        .setPositiveButton("确定", (dialog, which) -> 
                            permissionLauncher.launch(Manifest.permission.CAMERA))
                        .setNegativeButton("取消", null)
                        .show();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
            return;
        }

        try {
            File photoFile = createImageFile();
            imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            cameraLauncher.launch(intent);
        } catch (IOException e) {
            Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
            // Android 13+ 使用 READ_MEDIA_IMAGES
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() throws IOException {
        String imageFileName = "OCR_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void loadImageFromUri(Uri uri) {
        try {
            // 先清除之前的结果
            etOcrResult.setText("");
            etOcrResult.setHint(getString(R.string.recognizing));
            
            // 读取图片并处理旋转
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            
            if (bitmap == null) {
                Toast.makeText(this, "无法读取图片", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 处理图片旋转
            bitmap = handleImageRotation(uri, bitmap);
            
            // 压缩图片以适应ML Kit的要求
            currentBitmap = scaleBitmap(bitmap);
            
            // 显示图片
            imageView.setImageBitmap(currentBitmap);
            imageView.setVisibility(View.VISIBLE);
            
            // 开始识别
            recognizeText(currentBitmap);
            
            Log.d(TAG, "图片加载成功，尺寸: " + currentBitmap.getWidth() + "x" + currentBitmap.getHeight());
        } catch (IOException e) {
            Log.e(TAG, "加载图片失败: " + e.getMessage());
            Toast.makeText(this, "加载图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private Bitmap handleImageRotation(Uri uri, Bitmap bitmap) {
        try {
            // 使用AndroidX的ExifInterface（兼容性更好）
            InputStream inputStream = getContentResolver().openInputStream(uri);
            androidx.exifinterface.media.ExifInterface exif = 
                new androidx.exifinterface.media.ExifInterface(inputStream);
            inputStream.close();
            
            int orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL);
            
            Matrix matrix = new Matrix();
            switch (orientation) {
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                default:
                    return bitmap; // 不需要旋转
            }
            
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle(); // 释放原图内存
            return rotatedBitmap;
        } catch (Exception e) {
            Log.w(TAG, "处理图片旋转失败: " + e.getMessage());
            return bitmap; // 如果失败，返回原图
        }
    }
    
    private Bitmap scaleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        // 如果图片尺寸在限制内，直接返回
        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap;
        }
        
        // 计算缩放比例
        float scale = Math.min((float) MAX_IMAGE_DIMENSION / width, (float) MAX_IMAGE_DIMENSION / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        if (scaledBitmap != bitmap) {
            bitmap.recycle(); // 释放原图内存
        }
        
        Log.d(TAG, "图片已缩放: " + newWidth + "x" + newHeight);
        return scaledBitmap;
    }

    private void recognizeText(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(this, "图片无效", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        etOcrResult.setText("");
        etOcrResult.setHint(getString(R.string.recognizing));

        try {
            // 确保使用正确的旋转角度（0度，即正常方向）
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            
            Log.d(TAG, "开始识别，图片尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            
            textRecognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        progressBar.setVisibility(View.GONE);
                        StringBuilder resultBuilder = new StringBuilder();
                        
                        // 提取所有文本块，按顺序组合
                        for (com.google.mlkit.vision.text.Text.TextBlock block : visionText.getTextBlocks()) {
                            String blockText = block.getText();
                            if (blockText != null && !blockText.trim().isEmpty()) {
                                resultBuilder.append(blockText).append("\n");
                            }
                        }
                        
                        String resultText = resultBuilder.toString().trim();
                        
                        Log.d(TAG, "识别完成，结果长度: " + resultText.length());
                        
                        if (resultText.isEmpty()) {
                            etOcrResult.setHint(getString(R.string.no_text_found));
                            Toast.makeText(this, getString(R.string.no_text_found), Toast.LENGTH_SHORT).show();
                        } else {
                            // 清理识别结果：移除明显的乱码和特殊字符
                            resultText = cleanOcrResult(resultText);
                            
                            // 显示识别结果
                            etOcrResult.setText(resultText);
                            etOcrResult.setHint("");
                            Toast.makeText(this, getString(R.string.ocr_success), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "识别结果: " + resultText.substring(0, Math.min(100, resultText.length())));
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        etOcrResult.setHint("");
                        Log.e(TAG, "识别失败: " + e.getMessage(), e);
                        Toast.makeText(this, getString(R.string.ocr_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "创建InputImage失败: " + e.getMessage(), e);
            Toast.makeText(this, "处理图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ocr, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save_to_note) {
            saveToNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveToNote() {
        String text = etOcrResult.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "没有可保存的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 创建新笔记
        Note note = new Note("OCR识别内容", text);
        note.setCategory("OCR");

        Intent resultIntent = new Intent();
        resultIntent.putExtra("note", note);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String cleanOcrResult(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 移除明显的乱码模式（连续的大写字母和数字组合，如 "T, BE10RRA,"）
        text = text.replaceAll("\\b[A-Z]{2,}[0-9]+[A-Z]*\\b", "");
        text = text.replaceAll("\\b[0-9]+[A-Z]{3,}\\b", "");
        
        // 移除单独的特殊字符行和短行
        String[] lines = text.split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 跳过明显的乱码行：
            // 1. 只有大写字母、数字和标点的短行（如 "T, BE10RRA,"）
            // 2. 只有数字和少量字母的组合（如 "f7 500"）
            // 3. 长度很短且主要是特殊字符的行
            boolean isGarbage = false;
            
            // 检查是否是乱码模式
            if (line.length() <= 10) {
                // 短行：如果主要是大写字母、数字和标点，可能是乱码
                int upperCaseCount = 0;
                int digitCount = 0;
                int chineseCount = 0;
                for (char c : line.toCharArray()) {
                    if (Character.isUpperCase(c)) upperCaseCount++;
                    else if (Character.isDigit(c)) digitCount++;
                    else if (c >= 0x4E00 && c <= 0x9FFF) chineseCount++; // 中文字符范围
                }
                
                // 如果主要是大写字母和数字，且没有中文，可能是乱码
                if (chineseCount == 0 && (upperCaseCount + digitCount) * 2 > line.length()) {
                    isGarbage = true;
                }
            }
            
            // 检查是否匹配常见的乱码模式
            if (!isGarbage && line.matches("^[A-Z0-9\\s,;:()]{1,15}$") && 
                !line.matches(".*[\\u4e00-\\u9fa5].*")) { // 不包含中文
                isGarbage = true;
            }
            
            if (!isGarbage && line.length() > 1) {
                cleaned.append(line).append("\n");
            }
        }
        
        String result = cleaned.toString().trim();
        
        // 如果清理后结果为空，返回原始文本（可能原始文本就是正确的）
        if (result.isEmpty() && !text.trim().isEmpty()) {
            return text.trim();
        }
        
        return result;
    }
    
    private void generateTestImage() {
        // 创建一个包含中文文字的测试图片（高分辨率，适合OCR识别）
        int width = 1080;  // 提高分辨率
        int height = 1600;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // 设置白色背景
        canvas.drawColor(Color.WHITE);
        
        // 创建标题画笔（大字体）
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(72);
        titlePaint.setAntiAlias(true);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        
        // 创建正文画笔
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(56);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextAlign(Paint.Align.LEFT);
        
        // 绘制标题
        canvas.drawText("OCR文字识别测试", width / 2f, 80, titlePaint);
        
        // 测试文字内容（包含中文、数字、英文）
        String[] testTexts = {
            "",
            "这是一个测试图片",
            "用于验证OCR功能是否正常",
            "",
            "测试内容：",
            "1. 中文识别测试",
            "2. 数字识别：1234567890",
            "3. 英文识别：Hello World",
            "",
            "注意事项：",
            "• 确保图片清晰",
            "• 文字大小适中",
            "• 对比度足够",
            "",
            "测试日期：2024年12月",
            "测试编号：001",
            "",
            "这是一段较长的文本内容，",
            "用于测试OCR对连续文本的",
            "识别能力。包含多个句子。",
            "",
            "包含标点符号：，。！？；：",
            "包含特殊字符：@#$%^&*()",
            "",
            "多行文本测试：",
            "第一行内容",
            "第二行内容",
            "第三行内容",
            "",
            "测试完成"
        };
        
        float y = 150; // 起始Y坐标（标题下方）
        float lineHeight = 70; // 行高
        
        for (String text : testTexts) {
            if (!text.isEmpty()) {
                // 计算文字宽度，如果超过画布宽度则换行
                float textWidth = textPaint.measureText(text);
                float margin = 40;
                float maxWidth = width - margin * 2;
                
                if (textWidth > maxWidth) {
                    // 文字太长，需要换行
                    int start = 0;
                    while (start < text.length()) {
                        int end = start + 1;
                        while (end < text.length() && 
                               textPaint.measureText(text.substring(start, end + 1)) < maxWidth) {
                            end++;
                        }
                        if (end == start) end = start + 1; // 至少一个字符
                        String line = text.substring(start, end);
                        canvas.drawText(line, margin, y, textPaint);
                        y += lineHeight;
                        start = end;
                    }
                } else {
                    canvas.drawText(text, margin, y, textPaint);
                    y += lineHeight;
                }
            } else {
                y += lineHeight / 2; // 空行
            }
            
            // 如果超出画布，停止绘制
            if (y > height - 40) {
                break;
            }
        }
        
        // 显示生成的图片
        currentBitmap = bitmap;
        imageView.setImageBitmap(currentBitmap);
        imageView.setVisibility(View.VISIBLE);
        
        // 自动开始识别
        recognizeText(currentBitmap);
        
        Toast.makeText(this, "已生成测试图片，开始识别...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "已生成测试图片，尺寸: " + width + "x" + height);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}

