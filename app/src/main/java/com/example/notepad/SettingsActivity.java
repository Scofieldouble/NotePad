package com.example.notepad;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private RadioGroup themeRadioGroup;
    private RadioGroup backgroundColorRadioGroup;
    private SeekBar fontSizeSeekBar;
    private MaterialCardView previewCard;
    
    private static final String PREFS_NAME = "NotepadSettings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_FONT_SIZE = "font_size";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        loadSettings();
        setListeners();
    }
    
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.settings));
        }
        
        themeRadioGroup = findViewById(R.id.theme_radio_group);
        backgroundColorRadioGroup = findViewById(R.id.background_color_radio_group);
        fontSizeSeekBar = findViewById(R.id.font_size_seek_bar);
        previewCard = findViewById(R.id.preview_card);
    }
    
    private void loadSettings() {
        // 加载主题设置
        String theme = preferences.getString(KEY_THEME, "light");
        if ("dark".equals(theme)) {
            themeRadioGroup.check(R.id.radio_dark_theme);
        } else {
            themeRadioGroup.check(R.id.radio_light_theme);
        }
        
        // 加载背景颜色设置
        String bgColor = preferences.getString(KEY_BACKGROUND_COLOR, "white");
        switch (bgColor) {
            case "cream":
                backgroundColorRadioGroup.check(R.id.radio_cream);
                break;
            case "blue":
                backgroundColorRadioGroup.check(R.id.radio_blue);
                break;
            case "green":
                backgroundColorRadioGroup.check(R.id.radio_green);
                break;
            default:
                backgroundColorRadioGroup.check(R.id.radio_white);
        }
        
        // 加载字体大小设置
        int fontSize = preferences.getInt(KEY_FONT_SIZE, 16);
        fontSizeSeekBar.setProgress(fontSize - 12); // 12-20 范围
    }
    
    private void setListeners() {
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String theme = checkedId == R.id.radio_dark_theme ? "dark" : "light";
            preferences.edit().putString(KEY_THEME, theme).apply();
            // 注意：实际应用主题需要重启Activity或使用更复杂的主题切换机制
        });
        
        backgroundColorRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String bgColor = "white";
            if (checkedId == R.id.radio_cream) {
                bgColor = "cream";
            } else if (checkedId == R.id.radio_blue) {
                bgColor = "blue";
            } else if (checkedId == R.id.radio_green) {
                bgColor = "green";
            }
            preferences.edit().putString(KEY_BACKGROUND_COLOR, bgColor).apply();
            updatePreview();
        });
        
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fontSize = progress + 12;
                preferences.edit().putInt(KEY_FONT_SIZE, fontSize).apply();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void updatePreview() {
        String bgColor = preferences.getString(KEY_BACKGROUND_COLOR, "white");
        int colorRes = android.R.color.white;
        switch (bgColor) {
            case "cream":
                colorRes = R.color.cream;
                break;
            case "blue":
                colorRes = R.color.light_blue;
                break;
            case "green":
                colorRes = R.color.light_green;
                break;
        }
        previewCard.setCardBackgroundColor(getResources().getColor(colorRes));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public static String getTheme(SharedPreferences prefs) {
        return prefs.getString(KEY_THEME, "light");
    }
    
    public static String getBackgroundColor(SharedPreferences prefs) {
        return prefs.getString(KEY_BACKGROUND_COLOR, "white");
    }
    
    public static int getFontSize(SharedPreferences prefs) {
        return prefs.getInt(KEY_FONT_SIZE, 16);
    }
}

