package com.example.daycarepickupproject.Views;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.daycarepickupproject.Database.Repository;
import com.example.daycarepickupproject.Models.Child;
import com.example.daycarepickupproject.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Dialog to add a new child or edit / delete an existing one. */
public final class ChildDialog {

    public interface Listener {
        /** Called after saving. */
        void onSaved(Child child);

        /** Called after deleting (edit mode only). */
        default void onDeleted(Child child) {
        }
    }

    private ChildDialog() {
    }

    public static void show(Context context, @Nullable Child existing, Listener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_child, null);
        TextInputLayout tilName = view.findViewById(R.id.tilName);
        TextInputEditText etName = view.findViewById(R.id.etName);
        TextInputEditText etRoom = view.findViewById(R.id.etRoom);
        TextInputEditText etAge = view.findViewById(R.id.etAge);

        if (existing != null) {
            etName.setText(existing.name);
            etRoom.setText(existing.room);
            if (existing.age > 0) {
                etAge.setText(String.valueOf(existing.age));
            }
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(existing == null ? R.string.child_dialog_add_title : R.string.child_dialog_edit_title)
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null);
        if (existing != null) {
            builder.setNeutralButton(R.string.action_delete, (d, w) -> confirmDelete(context, existing, listener));
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = text(etName);
            if (name.isEmpty()) {
                tilName.setError(context.getString(R.string.child_name_required));
                return;
            }
            Child child = existing != null ? existing : new Child();
            child.name = name;
            child.room = text(etRoom);
            String age = text(etAge);
            child.age = TextUtils.isEmpty(age) ? 0 : Integer.parseInt(age);
            if (existing == null) {
                child.createdAt = System.currentTimeMillis();
            }
            Repository.run(context, dao -> {
                if (existing == null) {
                    child.id = dao.insertChild(child);
                } else {
                    dao.updateChild(child);
                }
                return child;
            }, saved -> {
                if (existing == null) {
                    Toast.makeText(context, context.getString(R.string.child_added, saved.name),
                            Toast.LENGTH_SHORT).show();
                }
                listener.onSaved(saved);
            });
            dialog.dismiss();
        }));
        dialog.show();
    }

    private static void confirmDelete(Context context, Child child, Listener listener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.child_delete_title, child.name))
                .setMessage(R.string.child_delete_message)
                .setPositiveButton(R.string.action_delete, (d, w) ->
                        Repository.run(context, dao -> {
                            dao.deleteChild(child);
                            return child;
                        }, listener::onDeleted))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private static String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }
}
