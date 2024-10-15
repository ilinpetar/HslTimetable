package com.example.hslwidget;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;

import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceManager;
import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // settings button in the upper right corner of the screen
        Button settingsButton = findViewById(R.id.btnSettings);
        settingsButton.setOnClickListener(view -> {
            // opening a new intent to open settings activity.
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // open settings screen on first run
        var preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getAll().isEmpty()) {
            settingsButton.callOnClick();
        }

        // start a thread that retrieves GraphQL data every 10 seconds
        var handler = new Handler();
        var runnable = new Runnable() {
            final HslTimetable hslTimetable = new HslTimetable(MainActivity.this);
            @Override
            public void run() {
                hslTimetable.obtainTimetables(findViewById(R.id.timetable));
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }
}