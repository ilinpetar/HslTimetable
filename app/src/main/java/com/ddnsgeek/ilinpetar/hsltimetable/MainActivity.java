package com.ddnsgeek.ilinpetar.hsltimetable;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.ddnsgeek.ilinpetar.hsltimetable.client.HslTimetable;
import com.ddnsgeek.ilinpetar.hsltimetable.fragment.AboutFragment;
import com.ddnsgeek.ilinpetar.hsltimetable.fragment.HomeFragment;
import com.ddnsgeek.ilinpetar.hsltimetable.fragment.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "HstTimetableNotificationChannel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        var hslTimetable = new HslTimetable(this);

        // create bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        var homeFragment = new HomeFragment(hslTimetable, builder);
        var settingsFragment = new SettingsFragment();
        var aboutFragment = new AboutFragment();

        setCurrentFragment(homeFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (R.id.btnHome == item.getItemId()) {
                setCurrentFragment(homeFragment);
            } else if (R.id.btnSettings == item.getItemId()) {
                setCurrentFragment(settingsFragment);
            } else if (R.id.btnAbout == item.getItemId()) {
                setCurrentFragment(aboutFragment);
            }
            return true;
        });
        // open settings screen on first run
        var preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getAll().isEmpty()) {
            setCurrentFragment(settingsFragment);
        }

        var runnable = new Runnable() {
            @Override
            public void run() {
                homeFragment.refresh();
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentPlaceholder, fragment)
            .commit();
    }
}