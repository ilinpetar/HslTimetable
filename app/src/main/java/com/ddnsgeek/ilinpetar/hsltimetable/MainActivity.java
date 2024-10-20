package com.ddnsgeek.ilinpetar.hsltimetable;

import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

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

        // make main panel scrollable
        TextView timetable = findViewById(R.id.timetable);
        timetable.setMovementMethod(new ScrollingMovementMethod());

        // settings button in the upper right corner of the screen
        var settingsButton = findViewById(R.id.btnSettings);
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