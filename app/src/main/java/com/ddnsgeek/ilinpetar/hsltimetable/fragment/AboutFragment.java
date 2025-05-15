package com.ddnsgeek.ilinpetar.hsltimetable.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.ddnsgeek.ilinpetar.hsltimetable.R;

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_about, container, false);
        TextView textAbout = view.findViewById(R.id.textAbout);
        textAbout.setText(String.format(
            getResources().getString(R.string.about_details),
            getResources().getString(R.string.versionName),
            getResources().getString(R.string.copyrightYear))
        );
        return view;
    }
}