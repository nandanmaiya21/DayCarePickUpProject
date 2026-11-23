package com.example.daycarepickupproject.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.R;
import com.example.daycarepickupproject.Views.Ui;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Log tab: every pick-up attempt for one day, searchable by child or guardian
 * and filterable by outcome.
 */
public class LogFragment extends Fragment {

    private static final String STATE_DAY = "dayStart";
    private static final String STATE_TAB = "tab";

    /** Tab positions, in the order they are added. */
    private static final String[] TAB_OUTCOMES = {null, PickupLog.VERIFIED, PickupLog.OVERRIDE, PickupLog.REJECTED};

    private TextView chipToday;
    private TextView chipYesterday;
    private TextView chipPickDate;
    private TabLayout tabs;
    private EditText etSearch;

    private long dayStart;
    private final List<PickupLog> dayLogs = new ArrayList<>();

    public LogFragment() {
        super(R.layout.fragment_log);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dayStart = savedInstanceState != null
                ? savedInstanceState.getLong(STATE_DAY, Ui.startOfToday())
                : Ui.startOfToday();

        tabs = view.findViewById(R.id.statusTabs);
        for (int i = 0; i < TAB_OUTCOMES.length; i++) {
            tabs.addTab(tabs.newTab());
        }
        if (savedInstanceState != null) {
            TabLayout.Tab tab = tabs.getTabAt(savedInstanceState.getInt(STATE_TAB, 0));
            if (tab != null) {
                tab.select();
            }
        }
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                render();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                render();
            }
        });

        chipToday = view.findViewById(R.id.chipToday);
        chipYesterday = view.findViewById(R.id.chipYesterday);
        chipPickDate = view.findViewById(R.id.chipPickDate);
        chipToday.setOnClickListener(v -> selectDay(Ui.startOfToday()));
        chipYesterday.setOnClickListener(v -> selectDay(yesterday()));
        chipPickDate.setOnClickListener(v -> pickDate());
    }

    @Override
    public void onResume() {
        super.onResume();
        selectDay(dayStart);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DAY, dayStart);
        if (tabs != null) {
            outState.putInt(STATE_TAB, tabs.getSelectedTabPosition());
        }
    }

    private static long yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(Ui.startOfToday());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTimeInMillis();
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayStart);
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (picker, y, m, d) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(y, m, d);
            selectDay(Ui.startOfDay(chosen.getTimeInMillis()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void selectDay(long start) {
        dayStart = start;
        boolean isToday = start == Ui.startOfToday();
        boolean isYesterday = start == yesterday();
        styleChip(chipToday, isToday);
        styleChip(chipYesterday, isYesterday);
        boolean custom = !isToday && !isYesterday;
        styleChip(chipPickDate, custom);
        chipPickDate.setText(custom
                ? DateUtils.formatDateTime(requireContext(), start,
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR)
                : getString(R.string.log_pick_date));
        TextView header = requireView().findViewById(R.id.tvDateHeader);
        header.setText(DateUtils.formatDateTime(requireContext(), start,
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
                        | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));

        Repository.run(requireContext(), dao -> dao.getLogsBetween(start, Ui.endOfDay(start)), logs -> {
            if (start != dayStart) {
                return; // a newer selection is loading
            }
            dayLogs.clear();
            dayLogs.addAll(logs);
            render();
        });
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        int color = ContextCompat.getColor(requireContext(), selected ? R.color.white : R.color.text_primary);
        chip.setTextColor(color);
        chip.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(color));
    }

    private void render() {
        View view = getView();
        if (view == null) {
            return;
        }
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.getDefault());

        // Search first, then count per outcome for the tab labels
        List<PickupLog> matching = new ArrayList<>();
        int verified = 0;
        int override = 0;
        int rejected = 0;
        for (PickupLog log : dayLogs) {
            if (!query.isEmpty() && !contains(log.childName, query) && !contains(log.guardianName, query)) {
                continue;
            }
            matching.add(log);
            if (PickupLog.VERIFIED.equals(log.outcome)) {
                verified++;
            } else if (PickupLog.OVERRIDE.equals(log.outcome)) {
                override++;
            } else {
                rejected++;
            }
        }
        setTabText(0, getString(R.string.log_tab_all, matching.size()));
        setTabText(1, getString(R.string.log_tab_verified, verified));
        setTabText(2, getString(R.string.log_tab_override, override));
        setTabText(3, getString(R.string.log_tab_rejected, rejected));

        String outcome = TAB_OUTCOMES[Math.max(0, tabs.getSelectedTabPosition())];
        LinearLayout list = view.findViewById(R.id.logList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int shown = 0;
        for (PickupLog log : matching) {
            if (outcome != null && !outcome.equals(log.outcome)) {
                continue;
            }
            if (shown++ > 0) {
                inflater.inflate(R.layout.item_divider, list, true);
            }
            View row = inflater.inflate(R.layout.item_log_row, list, false);
            ((TextView) row.findViewById(R.id.tvTime)).setText(Ui.time(requireContext(), log.timestamp));
            ((TextView) row.findViewById(R.id.tvName)).setText(title(log));
            ((TextView) row.findViewById(R.id.tvDetail)).setText(detail(log));
            Ui.applyBadge(row.findViewById(R.id.tvBadge), log.outcome);
            list.addView(row);
        }
        list.setVisibility(shown == 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.tvEmpty).setVisibility(shown == 0 ? View.VISIBLE : View.GONE);
    }

    private String title(PickupLog log) {
        if (log.childName != null) {
            return log.childName;
        }
        return getString(R.string.unknown_face);
    }

    /** Second line: who picked up and how sure the match was, or why it was rejected / overridden. */
    private String detail(PickupLog log) {
        if (PickupLog.VERIFIED.equals(log.outcome)) {
            return getString(R.string.detail_dot, log.guardianName, Ui.percent(requireContext(), log.score));
        }
        if (PickupLog.OVERRIDE.equals(log.outcome)) {
            String who = getString(R.string.detail_dot, log.guardianName,
                    getString(R.string.by_staff, log.staffName));
            return log.note == null ? who : getString(R.string.detail_dot, who, log.note);
        }
        String note = log.note != null ? log.note : getString(R.string.unknown_face);
        return log.score >= 0 ? getString(R.string.detail_dot, note, Ui.percent(requireContext(), log.score)) : note;
    }

    private void setTabText(int index, String text) {
        TabLayout.Tab tab = tabs.getTabAt(index);
        if (tab != null) {
            tab.setText(text);
        }
    }

    private static boolean contains(@Nullable String value, String query) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(query);
    }
}
