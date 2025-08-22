package com.chineeeasy.tangwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private static final String PREFS_NAME = "widget_theme_prefs";
    private static final String THEME_TYPE = "theme_type";
    private static final String BACKGROUND_COLOR = "background_color";
    private static final String BACKGROUND_ALPHA = "background_alpha";
    private static final String PINYIN_COLOR = "pinyin_color";
    private static final String CHINESE_COLOR = "chinese_color";
    private static final String MEANING_COLOR = "meaning_color";
    private static final String PINYIN_FONT_SIZE = "pinyin_font_size";
    private static final String CHINESE_FONT_SIZE = "chinese_font_size";
    private static final String MEANING_FONT_SIZE = "meaning_font_size";

    // Theme types
    private static final int THEME_LIGHT = 0;
    private static final int THEME_DARK = 1;
    private static final int THEME_CUSTOM = 2;

    // Default colors
    private static final int DEFAULT_LIGHT_BG = Color.WHITE;
    private static final int DEFAULT_DARK_BG = Color.parseColor("#2C2C2C");
    private static final int DEFAULT_LIGHT_TEXT = Color.BLACK;
    private static final int DEFAULT_DARK_TEXT = Color.WHITE;
    private static final int DEFAULT_LIGHT_MEANING = Color.parseColor("#666666");
    private static final int DEFAULT_DARK_MEANING = Color.parseColor("#CCCCCC");

    private RadioGroup themeRadioGroup;
    private LinearLayout customThemeContainer;
    private Button backgroundColorButton;
    private SeekBar backgroundAlphaSeekBar;
    private TextView backgroundAlphaText;
    private Button pinyinColorButton;
    private Button chineseColorButton;
    private Button meaningColorButton;
    private SeekBar pinyinFontSeekBar;
    private SeekBar chineseFontSeekBar;
    private SeekBar meaningFontSeekBar;
    private TextView pinyinFontText;
    private TextView chineseFontText;
    private TextView meaningFontText;
    private LinearLayout widgetPreview;
    private TextView previewPinyin;
    private TextView previewChinese;
    private TextView previewMeaning;

    private SharedPreferences prefs;
    private int currentBackgroundColor = DEFAULT_LIGHT_BG;
    private int currentBackgroundAlpha = 255;
    private int currentPinyinColor = DEFAULT_LIGHT_TEXT;
    private int currentChineseColor = DEFAULT_LIGHT_TEXT;
    private int currentMeaningColor = DEFAULT_LIGHT_MEANING;
    private int currentPinyinFontSize = 16;
    private int currentChineseFontSize = 24;
    private int currentMeaningFontSize = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();
        loadSettings();
        setupListeners();
        updatePreview();
    }

    private void initViews() {
        themeRadioGroup = findViewById(R.id.theme_radio_group);
        customThemeContainer = findViewById(R.id.custom_theme_container);
        backgroundColorButton = findViewById(R.id.background_color_button);
        backgroundAlphaSeekBar = findViewById(R.id.background_alpha_seekbar);
        backgroundAlphaText = findViewById(R.id.background_alpha_text);
        pinyinColorButton = findViewById(R.id.pinyin_color_button);
        chineseColorButton = findViewById(R.id.chinese_color_button);
        meaningColorButton = findViewById(R.id.meaning_color_button);
        pinyinFontSeekBar = findViewById(R.id.pinyin_font_seekbar);
        chineseFontSeekBar = findViewById(R.id.chinese_font_seekbar);
        meaningFontSeekBar = findViewById(R.id.meaning_font_seekbar);
        pinyinFontText = findViewById(R.id.pinyin_font_text);
        chineseFontText = findViewById(R.id.chinese_font_text);
        meaningFontText = findViewById(R.id.meaning_font_text);
        widgetPreview = findViewById(R.id.widget_preview);
        previewPinyin = findViewById(R.id.preview_pinyin);
        previewChinese = findViewById(R.id.preview_chinese);
        previewMeaning = findViewById(R.id.preview_meaning);
    }

    private void loadSettings() {
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        currentBackgroundColor = prefs.getInt(BACKGROUND_COLOR, DEFAULT_LIGHT_BG);
        currentBackgroundAlpha = prefs.getInt(BACKGROUND_ALPHA, 255);
        currentPinyinColor = prefs.getInt(PINYIN_COLOR, DEFAULT_LIGHT_TEXT);
        currentChineseColor = prefs.getInt(CHINESE_COLOR, DEFAULT_LIGHT_TEXT);
        currentMeaningColor = prefs.getInt(MEANING_COLOR, DEFAULT_LIGHT_MEANING);
        currentPinyinFontSize = prefs.getInt(PINYIN_FONT_SIZE, 16);
        currentChineseFontSize = prefs.getInt(CHINESE_FONT_SIZE, 24);
        currentMeaningFontSize = prefs.getInt(MEANING_FONT_SIZE, 14);

        // Set radio button
        switch (themeType) {
            case THEME_LIGHT:
                ((RadioButton) findViewById(R.id.theme_light)).setChecked(true);
                break;
            case THEME_DARK:
                ((RadioButton) findViewById(R.id.theme_dark)).setChecked(true);
                break;
            case THEME_CUSTOM:
                ((RadioButton) findViewById(R.id.theme_custom)).setChecked(true);
                customThemeContainer.setVisibility(View.VISIBLE);
                break;
        }

        backgroundAlphaSeekBar.setProgress(currentBackgroundAlpha);
        updateAlphaText();
        updateColorButtons();
        
        // Set font size seekbars (convert sp to seekbar values)
        pinyinFontSeekBar.setProgress(currentPinyinFontSize - 10); // 10-30sp range
        chineseFontSeekBar.setProgress(currentChineseFontSize - 10); // 10-40sp range  
        meaningFontSeekBar.setProgress(currentMeaningFontSize - 10); // 10-26sp range
        updateFontTexts();
    }

    private void setupListeners() {
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.theme_light) {
                customThemeContainer.setVisibility(View.GONE);
                applyLightTheme();
            } else if (checkedId == R.id.theme_dark) {
                customThemeContainer.setVisibility(View.GONE);
                applyDarkTheme();
            } else if (checkedId == R.id.theme_custom) {
                customThemeContainer.setVisibility(View.VISIBLE);
            }
            updatePreview();
        });

        backgroundColorButton.setOnClickListener(v -> showColorPicker("Background Color", currentBackgroundColor, color -> {
            currentBackgroundColor = color;
            updateColorButtons();
            updatePreview();
        }));

        backgroundAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentBackgroundAlpha = progress;
                updateAlphaText();
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        pinyinColorButton.setOnClickListener(v -> showColorPicker("Pinyin Color", currentPinyinColor, color -> {
            currentPinyinColor = color;
            updateColorButtons();
            updatePreview();
        }));

        chineseColorButton.setOnClickListener(v -> showColorPicker("Chinese Color", currentChineseColor, color -> {
            currentChineseColor = color;
            updateColorButtons();
            updatePreview();
        }));

        meaningColorButton.setOnClickListener(v -> showColorPicker("Meaning Color", currentMeaningColor, color -> {
            currentMeaningColor = color;
            updateColorButtons();
            updatePreview();
        }));

        // Font size seekbar listeners
        pinyinFontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentPinyinFontSize = progress + 10; // 10-30sp range
                updateFontTexts();
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chineseFontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentChineseFontSize = progress + 10; // 10-40sp range
                updateFontTexts();
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        meaningFontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentMeaningFontSize = progress + 10; // 10-26sp range
                updateFontTexts();
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.reset_button).setOnClickListener(v -> resetToDefault());
        findViewById(R.id.apply_button).setOnClickListener(v -> applyTheme());
    }

    private void applyLightTheme() {
        currentBackgroundColor = DEFAULT_LIGHT_BG;
        currentBackgroundAlpha = 255;
        currentPinyinColor = DEFAULT_LIGHT_TEXT;
        currentChineseColor = DEFAULT_LIGHT_TEXT;
        currentMeaningColor = DEFAULT_LIGHT_MEANING;
        currentPinyinFontSize = 16;
        currentChineseFontSize = 24;
        currentMeaningFontSize = 14;
        backgroundAlphaSeekBar.setProgress(255);
        pinyinFontSeekBar.setProgress(6); // 16sp
        chineseFontSeekBar.setProgress(14); // 24sp
        meaningFontSeekBar.setProgress(4); // 14sp
        updateAlphaText();
        updateColorButtons();
        updateFontTexts();
    }

    private void applyDarkTheme() {
        currentBackgroundColor = DEFAULT_DARK_BG;
        currentBackgroundAlpha = 255;
        currentPinyinColor = DEFAULT_DARK_TEXT;
        currentChineseColor = DEFAULT_DARK_TEXT;
        currentMeaningColor = DEFAULT_DARK_MEANING;
        currentPinyinFontSize = 16;
        currentChineseFontSize = 24;
        currentMeaningFontSize = 14;
        backgroundAlphaSeekBar.setProgress(255);
        pinyinFontSeekBar.setProgress(6); // 16sp
        chineseFontSeekBar.setProgress(14); // 24sp
        meaningFontSeekBar.setProgress(4); // 14sp
        updateAlphaText();
        updateColorButtons();
        updateFontTexts();
    }

    private void updateAlphaText() {
        int percentage = (int) ((currentBackgroundAlpha / 255.0) * 100);
        backgroundAlphaText.setText(percentage + "%");
    }

    private void updateFontTexts() {
        pinyinFontText.setText(currentPinyinFontSize + "sp");
        chineseFontText.setText(currentChineseFontSize + "sp");
        meaningFontText.setText(currentMeaningFontSize + "sp");
    }

    private void updateColorButtons() {
        setButtonColor(backgroundColorButton, currentBackgroundColor);
        setButtonColor(pinyinColorButton, currentPinyinColor);
        setButtonColor(chineseColorButton, currentChineseColor);
        setButtonColor(meaningColorButton, currentMeaningColor);
    }

    private void setButtonColor(Button button, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setStroke(2, Color.GRAY);
        drawable.setCornerRadius(8);
        button.setBackground(drawable);
    }

    private void updatePreview() {
        // Apply background with alpha
        int bgColorWithAlpha = Color.argb(currentBackgroundAlpha, 
            Color.red(currentBackgroundColor), 
            Color.green(currentBackgroundColor), 
            Color.blue(currentBackgroundColor));
        widgetPreview.setBackgroundColor(bgColorWithAlpha);

        // Apply text colors
        previewPinyin.setTextColor(currentPinyinColor);
        previewChinese.setTextColor(currentChineseColor);
        previewMeaning.setTextColor(currentMeaningColor);

        // Apply font sizes
        previewPinyin.setTextSize(currentPinyinFontSize);
        previewChinese.setTextSize(currentChineseFontSize);
        previewMeaning.setTextSize(currentMeaningFontSize);
    }

    private void showColorPicker(String title, int currentColor, ColorPickerCallback callback) {
        // Simple color picker with predefined colors
        int[] colors = {
            Color.WHITE, Color.BLACK, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY,
            Color.parseColor("#FF5722"), Color.parseColor("#4CAF50"), Color.parseColor("#2196F3"),
            Color.parseColor("#9C27B0"), Color.parseColor("#FF9800"), Color.parseColor("#607D8B")
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Create color buttons in rows
        for (int i = 0; i < colors.length; i += 5) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            
            for (int j = i; j < Math.min(i + 5, colors.length); j++) {
                Button colorButton = new Button(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
                params.setMargins(8, 8, 8, 8);
                colorButton.setLayoutParams(params);
                
                final int color = colors[j];
                setButtonColor(colorButton, color);
                colorButton.setOnClickListener(v -> {
                    callback.onColorSelected(color);
                    ((AlertDialog) colorButton.getTag()).dismiss();
                });
                
                row.addView(colorButton);
            }
            layout.addView(row);
        }

        builder.setView(layout);
        builder.setNegativeButton("Cancel", null);
        
        AlertDialog dialog = builder.create();
        
        // Set tag for buttons to access dialog
        for (int i = 0; i < layout.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) layout.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                row.getChildAt(j).setTag(dialog);
            }
        }
        
        dialog.show();
    }

    private void resetToDefault() {
        ((RadioButton) findViewById(R.id.theme_light)).setChecked(true);
        customThemeContainer.setVisibility(View.GONE);
        applyLightTheme();
        updatePreview();
    }

    private void applyTheme() {
        SharedPreferences.Editor editor = prefs.edit();
        
        int themeType = THEME_LIGHT;
        if (((RadioButton) findViewById(R.id.theme_dark)).isChecked()) {
            themeType = THEME_DARK;
        } else if (((RadioButton) findViewById(R.id.theme_custom)).isChecked()) {
            themeType = THEME_CUSTOM;
        }
        
        editor.putInt(THEME_TYPE, themeType);
        editor.putInt(BACKGROUND_COLOR, currentBackgroundColor);
        editor.putInt(BACKGROUND_ALPHA, currentBackgroundAlpha);
        editor.putInt(PINYIN_COLOR, currentPinyinColor);
        editor.putInt(CHINESE_COLOR, currentChineseColor);
        editor.putInt(MEANING_COLOR, currentMeaningColor);
        editor.putInt(PINYIN_FONT_SIZE, currentPinyinFontSize);
        editor.putInt(CHINESE_FONT_SIZE, currentChineseFontSize);
        editor.putInt(MEANING_FONT_SIZE, currentMeaningFontSize);
        editor.apply();

        // Update all widgets
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName componentName = new ComponentName(this, IdiomWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        
        for (int appWidgetId : appWidgetIds) {
            IdiomWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);
        }

        // Show confirmation
        new AlertDialog.Builder(this)
            .setTitle("Theme Applied")
            .setMessage("Your widget theme has been updated!")
            .setPositiveButton("OK", null)
            .show();
    }

    private interface ColorPickerCallback {
        void onColorSelected(int color);
    }

    // Static methods for accessing theme settings from widget
    public static int getBackgroundColor(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        
        switch (themeType) {
            case THEME_DARK:
                return DEFAULT_DARK_BG;
            case THEME_CUSTOM:
                return prefs.getInt(BACKGROUND_COLOR, DEFAULT_LIGHT_BG);
            default:
                return DEFAULT_LIGHT_BG;
        }
    }

    public static int getBackgroundAlpha(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        
        if (themeType == THEME_CUSTOM) {
            return prefs.getInt(BACKGROUND_ALPHA, 255);
        }
        return 255;
    }

    public static int getPinyinColor(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        
        switch (themeType) {
            case THEME_DARK:
                return DEFAULT_DARK_TEXT;
            case THEME_CUSTOM:
                return prefs.getInt(PINYIN_COLOR, DEFAULT_LIGHT_TEXT);
            default:
                return DEFAULT_LIGHT_TEXT;
        }
    }

    public static int getChineseColor(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        
        switch (themeType) {
            case THEME_DARK:
                return DEFAULT_DARK_TEXT;
            case THEME_CUSTOM:
                return prefs.getInt(CHINESE_COLOR, DEFAULT_LIGHT_TEXT);
            default:
                return DEFAULT_LIGHT_TEXT;
        }
    }

    public static int getMeaningColor(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeType = prefs.getInt(THEME_TYPE, THEME_LIGHT);
        
        switch (themeType) {
            case THEME_DARK:
                return DEFAULT_DARK_MEANING;
            case THEME_CUSTOM:
                return prefs.getInt(MEANING_COLOR, DEFAULT_LIGHT_MEANING);
            default:
                return DEFAULT_LIGHT_MEANING;
        }
    }

    public static int getPinyinFontSize(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(PINYIN_FONT_SIZE, 16);
    }

    public static int getChineseFontSize(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(CHINESE_FONT_SIZE, 24);
    }

    public static int getMeaningFontSize(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(MEANING_FONT_SIZE, 14);
    }
}