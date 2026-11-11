package com.example.daycarepickupproject.Fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.R;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Log tab: pick-up history with a search box, date chips and status tabs.
 */
public class LogFragment extends Fragment {

    private TextView chipToday;
    private TextView chipYesterday;
    private TextView chipPickDate;
    private TextView tvDateHeader;
    private EditText etSearch;

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

        // Date filter chips
        chipToday = view.findViewById(R.id.chipToday);
        chipYesterday = view.findViewById(R.id.chipYesterday);
        chipPickDate = view.findViewById(R.id.chipPickDate);
        tvDateHeader = view.findViewById(R.id.tvDateHeader);

        chipToday.setOnClickListener(v -> {
            selectChip(chipToday, chipYesterday, chipPickDate);
            tvDateHeader.setText(R.string.log_today);
        });
        chipYesterday.setOnClickListener(v -> {
            selectChip(chipYesterday, chipToday, chipPickDate);
            tvDateHeader.setText(R.string.log_yesterday);
        });
        chipPickDate.setOnClickListener(v -> showDatePicker());

        // Search box: filter log entries dynamically as the user types
        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLogs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard();
                etSearch.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void filterLogs(String query) {
        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();
        View rootView = getView();
        if (rootView == null) return;

        LinearLayout logList = rootView.findViewById(R.id.logList);
        if (logList == null) return;

        int childCount = logList.getChildCount();

        // First pass: update visibility of pickup rows (even indices)
        for (int i = 0; i < childCount; i++) {
            View child = logList.getChildAt(i);
            if (i % 2 == 0) {
                if (child instanceof ViewGroup) {
                    String rowText = extractRowText((ViewGroup) child);
                    boolean matches = lowerQuery.isEmpty() || rowText.toLowerCase(Locale.getDefault()).contains(lowerQuery);
                    child.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }

        // Second pass: update visibility of dividers (odd indices) between visible rows
        for (int i = 1; i < childCount; i += 2) {
            View divider = logList.getChildAt(i);
            boolean hasVisibleBefore = false;
            for (int j = i - 1; j >= 0; j -= 2) {
                if (logList.getChildAt(j).getVisibility() == View.VISIBLE) {
                    hasVisibleBefore = true;
                    break;
                }
            }
            boolean hasVisibleAfter = false;
            for (int k = i + 1; k < childCount; k += 2) {
                if (logList.getChildAt(k).getVisibility() == View.VISIBLE) {
                    hasVisibleAfter = true;
                    break;
                }
            }
            divider.setVisibility((hasVisibleBefore && hasVisibleAfter) ? View.VISIBLE : View.GONE);
        }
    }

    private String extractRowText(ViewGroup row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.getChildCount(); i++) {
            View child = row.getChildAt(i);
            if (child instanceof TextView) {
                sb.append(((TextView) child).getText()).append(" ");
            } else if (child instanceof ViewGroup) {
                sb.append(extractRowText((ViewGroup) child));
            }
        }
        return sb.toString();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePicker, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                    String dateString = sdf.format(selectedDate.getTime()).toUpperCase();

                    chipPickDate.setText(dateString);
                    selectChip(chipPickDate, chipToday, chipYesterday);
                    tvDateHeader.setText(dateString);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void selectChip(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        for (TextView other : others) {
            other.setBackgroundResource(R.drawable.bg_chip_unselected);
            other.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
