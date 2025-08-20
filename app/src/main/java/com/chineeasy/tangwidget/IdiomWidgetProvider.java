package com.chineeasy.tangwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class IdiomWidgetProvider extends AppWidgetProvider {

    private static final String PREFS_NAME = "com.chineeasy.tangwidget.IdiomWidgetProvider";
    private static final String PREF_PREFIX_KEY = "idiom_";
    private static List<String[]> idioms;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (idioms == null) {
            loadIdioms(context);
        }

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void loadIdioms(Context context) {
        idioms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("365tabbed.u8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                idioms.add(line.split("\t"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int idiomIndex = prefs.getInt(PREF_PREFIX_KEY + appWidgetId, 0);

        if (prefs.getInt("day", 0) != dayOfYear) {
            idiomIndex = (idiomIndex + 1) % idioms.size();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PREF_PREFIX_KEY + appWidgetId, idiomIndex);
            editor.putInt("day", dayOfYear);
            editor.apply();
        }

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        String[] idiom = null;

        if (idioms != null && !idioms.isEmpty()) {
            idiom = idioms.get(idiomIndex);
            views.setTextViewText(R.id.idiom_pinyin, idiom[0]);
            views.setTextViewText(R.id.idiom_chinese, idiom[1]);
            views.setTextViewText(R.id.idiom_meaning, idiom[2]);
        }

        // Apply theme colors
        applyTheme(context, views);

        Intent intent = new Intent(context, SettingsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void applyTheme(Context context, RemoteViews views) {
        // Get theme colors from SettingsActivity
        int backgroundColor = SettingsActivity.getBackgroundColor(context);
        int backgroundAlpha = SettingsActivity.getBackgroundAlpha(context);
        int pinyinColor = SettingsActivity.getPinyinColor(context);
        int chineseColor = SettingsActivity.getChineseColor(context);
        int meaningColor = SettingsActivity.getMeaningColor(context);

        // Apply background color with alpha
        int bgColorWithAlpha = android.graphics.Color.argb(backgroundAlpha, 
            android.graphics.Color.red(backgroundColor), 
            android.graphics.Color.green(backgroundColor), 
            android.graphics.Color.blue(backgroundColor));
        views.setInt(R.id.widget_container, "setBackgroundColor", bgColorWithAlpha);

        // Apply text colors
        views.setTextColor(R.id.idiom_pinyin, pinyinColor);
        views.setTextColor(R.id.idiom_chinese, chineseColor);
        views.setTextColor(R.id.idiom_meaning, meaningColor);

        // Apply font sizes
        int pinyinFontSize = SettingsActivity.getPinyinFontSize(context);
        int chineseFontSize = SettingsActivity.getChineseFontSize(context);
        int meaningFontSize = SettingsActivity.getMeaningFontSize(context);
        
        views.setFloat(R.id.idiom_pinyin, "setTextSize", pinyinFontSize);
        views.setFloat(R.id.idiom_chinese, "setTextSize", chineseFontSize);
        views.setFloat(R.id.idiom_meaning, "setTextSize", meaningFontSize);
    }
}
