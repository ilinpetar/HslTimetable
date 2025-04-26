package com.ddnsgeek.ilinpetar.hsltimetable;

import android.Manifest.permission;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "HstTimetableNotificationChannel";
    private static final int NOTIFICATION_ID = 1;

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

        // notification channel
        var name = getString(R.string.channel_name);
        var description = getString(R.string.channel_description);
        var importance = NotificationManager.IMPORTANCE_HIGH;
        var channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this.
        var notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // notifications builder
        var builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_bus_alert)
            .setContentTitle(getString(R.string.channel_description))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSilent(true);

        // start a thread that retrieves GraphQL data every 10 seconds
        var handler = new Handler(Looper.getMainLooper());
        var runnable = new Runnable() {
            final HslTimetable hslTimetable = new HslTimetable(MainActivity.this);

            @Override
            public void run() {
                hslTimetable.obtainTimetables(findViewById(R.id.timetable), builder);
                if (ContextCompat.checkSelfPermission(MainActivity.this, permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission.POST_NOTIFICATIONS}, 1);
                    // T0D0: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                NotificationManagerCompat.from(MainActivity.this).notify(NOTIFICATION_ID, builder.build());
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }
}