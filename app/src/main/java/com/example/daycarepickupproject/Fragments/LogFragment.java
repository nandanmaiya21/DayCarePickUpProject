package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.R;
import com.google.android.material.tabs.TabLayout;

/**
 * Log tab: pick-up history with a search box, date chips and status tabs.
 */
public class LogFragment extends Fragment {

    private TextView chipToday;
    private TextView chipYesterday;

    private ScrollView logScroll;
    private EditText etSearch;
    private TextView chipPickDate, tvDateHeader;
    private TabLayout statusTabs;
    private LinearLayout logList;

    private void bindViews(View view) {
        logScroll = view.findViewById(R.id.logScroll);
        etSearch = view.findViewById(R.id.etSearch);
        chipToday = view.findViewById(R.id.chipToday);
        chipYesterday = view.findViewById(R.id.chipYesterday);
        chipPickDate = view.findViewById(R.id.chipPickDate);
        statusTabs = view.findViewById(R.id.statusTabs);
        tvDateHeader = view.findViewById(R.id.tvDateHeader);
        logList = view.findViewById(R.id.logList);
    }

    public LogFragment() {
        super(R.layout.fragment_log);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Status tabs. Counts are sample values until the log model exists.
        TabLayout tabs = view.findViewById(R.id.statusTabs);
        tabs.addTab(tabs.newTab().setText(getString(R.string.log_tab_all, 15)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.log_tab_verified, 13)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.log_tab_override, 1)));
        tabs.addTab(tabs.newTab().setText(getString(R.string.log_tab_rejected, 1)));

        // Today / Yesterday chips: only one is highlighted at a time
        chipToday = view.findViewById(R.id.chipToday);
        chipYesterday = view.findViewById(R.id.chipYesterday);
        chipToday.setOnClickListener(v -> selectChip(chipToday, chipYesterday));
        chipYesterday.setOnClickListener(v -> selectChip(chipYesterday, chipToday));
    }

    private void selectChip(TextView selected, TextView other) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        other.setBackgroundResource(R.drawable.bg_chip_unselected);
        other.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
    }
}
