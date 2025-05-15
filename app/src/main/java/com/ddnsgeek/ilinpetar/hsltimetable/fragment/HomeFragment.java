package com.ddnsgeek.ilinpetar.hsltimetable.fragment;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.app.NotificationCompat.Builder;
import androidx.fragment.app.Fragment;
import com.ddnsgeek.ilinpetar.hsltimetable.R;
import com.ddnsgeek.ilinpetar.hsltimetable.client.HslTimetable;
import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {

    private final HslTimetable hslTimetable;
    private final Builder builder;

    public HomeFragment(HslTimetable hslTimetable, Builder builder) {
        this.hslTimetable = hslTimetable;
        this.builder = builder;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_home, container, false);
        // make main panel scrollable
        TextView timetable = view.findViewById(R.id.timetable);
        timetable.setMovementMethod(new ScrollingMovementMethod());
        // refresh immediately when we switch to home fragment
        hslTimetable.obtainTimetables(timetable, builder);

        return view;
    }

    public void refresh() {
        var view = getView();
        if (view != null) {
            hslTimetable.obtainTimetables(view.findViewById(R.id.timetable), builder);
        }
    }
}