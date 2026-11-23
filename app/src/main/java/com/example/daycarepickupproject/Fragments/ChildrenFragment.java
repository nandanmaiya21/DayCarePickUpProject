package com.example.daycarepickupproject.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.HomeScreen;
import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.Models.PickupLog;
import com.example.daycarepickupproject.R;
import com.example.daycarepickupproject.Views.ChildDialog;
import com.example.daycarepickupproject.Views.Ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Children tab: every child with today's status. Tap a child to open their profile.
 */
public class ChildrenFragment extends Fragment {

    /** A child plus what the list row needs. */
    private static final class Row {
        Child child;
        int guardianCount;
        @Nullable PickupLog pickup; // today's pick-up, if any
    }

    private final List<Row> rows = new ArrayList<>();
    private EditText etSearch;

    public ChildrenFragment() {
        super(R.layout.fragment_children);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etSearch = view.findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                render();
            }
        });
        view.findViewById(R.id.btnAddChild).setOnClickListener(v ->
                ChildDialog.show(requireContext(), null, child -> load()));
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        long since = Ui.startOfToday();
        Repository.run(requireContext(), dao -> {
            Map<Long, PickupLog> pickups = new HashMap<>();
            // Oldest first so the latest pick-up of the day wins
            List<PickupLog> today = dao.getLogsBetween(since, Ui.endOfDay(since));
            for (int i = today.size() - 1; i >= 0; i--) {
                PickupLog log = today.get(i);
                if (log.isPickedUp() && log.childId != null) {
                    pickups.put(log.childId, log);
                }
            }
            List<Row> result = new ArrayList<>();
            for (Child c : dao.getChildren()) {
                Row row = new Row();
                row.child = c;
                row.guardianCount = dao.countGuardiansOfChild(c.id);
                row.pickup = pickups.get(c.id);
                result.add(row);
            }
            return result;
        }, result -> {
            rows.clear();
            rows.addAll(result);
            render();
        });
    }

    private void render() {
        View view = getView();
        if (view == null) {
            return;
        }
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        LinearLayout list = view.findViewById(R.id.childrenList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int shown = 0;
        for (Row r : rows) {
            if (!query.isEmpty() && !r.child.name.toLowerCase(Locale.getDefault()).contains(query)) {
                continue;
            }
            if (shown++ > 0) {
                inflater.inflate(R.layout.item_divider, list, true);
            }
            View item = inflater.inflate(R.layout.item_child_row, list, false);
            ((TextView) item.findViewById(R.id.tvAvatar)).setText(r.child.initials());
            ((TextView) item.findViewById(R.id.tvName)).setText(r.child.name);
            ((TextView) item.findViewById(R.id.tvDetail)).setText(detail(r));
            TextView badge = item.findViewById(R.id.tvBadge);
            if (r.pickup != null) {
                Ui.applyBadge(badge, r.pickup.outcome);
                badge.setText(Ui.time(requireContext(), r.pickup.timestamp));
            } else {
                Ui.applyNeutralBadge(badge, R.string.status_still_here);
            }
            item.setOnClickListener(v -> ((HomeScreen) requireActivity()).openChildProfile(r.child.id));
            list.addView(item);
        }
        list.setVisibility(shown == 0 ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.tvEmpty).setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String detail(Row r) {
        String guardians = r.guardianCount == 0 ? getString(R.string.child_no_guardians)
                : r.guardianCount == 1 ? getString(R.string.child_one_guardian)
                : getString(R.string.child_guardian_count, r.guardianCount);
        return r.child.room.isEmpty() ? guardians : getString(R.string.detail_dot, r.child.room, guardians);
    }
}
