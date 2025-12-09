package com.example.notepad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class VoiceSearchActivity extends AppCompatActivity {
    private static final String TAG = "VoiceSearchActivity";
    private SpeechRecognizer speechRecognizer;
    private TextView tvStatus;
    private TextView tvResult;
    private MaterialButton btnStart;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_search);

        initViews();
        initSpeechRecognizer();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.voice_search));
        }

        tvStatus = findViewById(R.id.tv_status);
        tvResult = findViewById(R.id.tv_result);
        btnStart = findViewById(R.id.btn_start_voice);

        btnStart.setOnClickListener(v -> {
            if (!isListening) {
                startListening();
            } else {
                stopListening();
            }
        });
    }

    private void initSpeechRecognizer() {
        // 首先尝试使用SpeechRecognizer API（需要Google服务）
        // 但即使不可用，也不退出，而是使用Intent方式
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                if (speechRecognizer == null) {
                    // 创建失败，使用Intent方式
                    return;
                }
                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        tvStatus.setText(getString(R.string.speaking));
                        isListening = true;
                        btnStart.setText("停止");
                    }

                    @Override
                    public void onBeginningOfSpeech() {}

                    @Override
                    public void onRmsChanged(float rmsdB) {}

                    @Override
                    public void onBufferReceived(byte[] buffer) {}

                    @Override
                    public void onEndOfSpeech() {
                        isListening = false;
                        btnStart.setText(getString(R.string.voice_search));
                    }

                    @Override
                    public void onError(int error) {
                        isListening = false;
                        btnStart.setText(getString(R.string.voice_search));
                        String errorMessage = "识别错误";
                        switch (error) {
                            case SpeechRecognizer.ERROR_AUDIO:
                                errorMessage = "音频错误";
                                break;
                            case SpeechRecognizer.ERROR_CLIENT:
                                errorMessage = "客户端错误";
                                break;
                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                errorMessage = "权限不足";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK:
                                errorMessage = "网络错误";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                errorMessage = "网络超时";
                                break;
                            case SpeechRecognizer.ERROR_NO_MATCH:
                                errorMessage = "未识别到内容";
                                break;
                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                errorMessage = "识别器忙碌";
                                break;
                            case SpeechRecognizer.ERROR_SERVER:
                                errorMessage = "服务器错误";
                                break;
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                errorMessage = "说话超时";
                                break;
                        }
                        tvStatus.setText(errorMessage);
                        Toast.makeText(VoiceSearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            String result = matches.get(0);
                            tvResult.setText(result);
                            tvStatus.setText("识别完成");
                            
                            // 自动执行搜索
                            performSearch(result);
                        }
                        isListening = false;
                        btnStart.setText(getString(R.string.voice_search));
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (matches != null && !matches.isEmpty()) {
                            tvResult.setText(matches.get(0));
                        }
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {}
                });
            } catch (Exception e) {
                // 如果创建失败，使用Intent方式
                speechRecognizer = null;
            }
        }
        
        // 如果SpeechRecognizer不可用，将使用Intent方式（不需要Google服务）
    }

    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            // 直接使用Intent方式，它会自动处理权限
            useIntentRecognition();
            return;
        }

        // 如果SpeechRecognizer可用，使用它
        if (speechRecognizer != null) {
            try {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");
                
                speechRecognizer.startListening(intent);
            } catch (Exception e) {
                // 如果失败，使用Intent方式
                useIntentRecognition();
            }
        } else {
            // 使用Intent方式（兼容性更好，不需要Google服务）
            useIntentRecognition();
        }
    }
    
    private void useIntentRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        
        // 首先检查是否有应用可以处理这个Intent
        boolean hasRecognizer = intent.resolveActivity(getPackageManager()) != null;
        
        // 如果resolveActivity返回null，尝试检查已安装的常见输入法
        if (!hasRecognizer) {
            hasRecognizer = checkAvailableRecognizers(intent);
        }
        
        // 无论检测结果如何，都尝试启动Intent
        // 因为某些输入法可能已安装但未正确注册Intent过滤器
        try {
            startActivityForResult(intent, 100);
            tvStatus.setText(getString(R.string.speaking));
            isListening = true;
            btnStart.setText("停止");
            Log.d(TAG, "成功启动语音识别Intent");
        } catch (android.content.ActivityNotFoundException e) {
            Log.e(TAG, "启动语音识别失败: " + e.getMessage(), e);
            // 启动失败，显示错误信息和解决方案
            showRecognizerNotFoundDialog();
        } catch (Exception e) {
            Log.e(TAG, "启动语音识别时发生未知错误: " + e.getMessage(), e);
            Toast.makeText(this, "启动语音识别失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean checkAvailableRecognizers(Intent intent) {
        // 列出所有可以处理语音识别Intent的应用
        java.util.List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
        
        if (activities != null && !activities.isEmpty()) {
            Log.d(TAG, "找到 " + activities.size() + " 个语音识别应用:");
            for (ResolveInfo info : activities) {
                String packageName = info.activityInfo.packageName;
                String appName = info.loadLabel(getPackageManager()).toString();
                Log.d(TAG, "  - " + appName + " (" + packageName + ")");
            }
            return true;
        }
        
        // 检查常见的语音输入应用包名
        String[] commonPackages = {
            "com.google.android.googlequicksearchbox", // Google语音输入
            "com.sohu.inputmethod.sogou", // 搜狗输入法
            "com.iflytek.inputmethod", // 讯飞输入法
            "com.baidu.input", // 百度输入法
            "com.tencent.qqpim", // QQ输入法
        };
        
        for (String pkg : commonPackages) {
            try {
                getPackageManager().getPackageInfo(pkg, 0);
                Log.d(TAG, "检测到已安装的语音输入应用: " + pkg);
                // 即使resolveActivity返回null，也尝试直接启动
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                // 应用未安装，继续检查下一个
            }
        }
        
        return false;
    }
    
    private void showRecognizerNotFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("语音识别不可用")
                .setMessage("未检测到可用的语音识别应用。\n\n解决方案：\n1. 确保已安装并启用搜狗输入法\n2. 在系统设置中设置搜狗输入法为默认输入法\n3. 或安装Google语音输入\n4. 或安装讯飞输入法\n\n安装后请重启应用。")
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", (dialog, which) -> finish())
                .show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String result = matches.get(0);
                tvResult.setText(result);
                tvStatus.setText("识别完成");
                performSearch(result);
            }
            isListening = false;
            btnStart.setText(getString(R.string.voice_search));
        } else if (requestCode == 100) {
            isListening = false;
            btnStart.setText(getString(R.string.voice_search));
            tvStatus.setText("识别已取消");
        }
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        // Intent方式无法停止，只能等待完成或取消
        isListening = false;
        btnStart.setText(getString(R.string.voice_search));
        tvStatus.setText("已停止");
    }

    private void performSearch(String query) {
        // 返回搜索结果到MainActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("search_query", query);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}

