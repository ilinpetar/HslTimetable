package com.ddnsgeek.ilinpetar.hsltimetable.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.ddnsgeek.ilinpetar.hsltimetable.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}