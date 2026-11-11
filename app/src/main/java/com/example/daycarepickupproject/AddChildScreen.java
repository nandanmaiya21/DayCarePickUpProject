package com.example.daycarepickupproject;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class AddChildScreen extends AppCompatActivity {

    private TextInputLayout layoutChildName;
    private TextInputEditText etChildName;
    private TextInputEditText etChildRoom;
    private TextInputEditText etChildDob;
    private MaterialButton btnSaveChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_child_screen);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        layoutChildName = findViewById(R.id.layoutChildName);
        etChildName = findViewById(R.id.etChildName);
        etChildRoom = findViewById(R.id.etChildRoom);
        etChildDob = findViewById(R.id.etChildDob);
        btnSaveChild = findViewById(R.id.btnSaveChild);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSaveChild.setOnClickListener(v -> saveChild());
    }

    private void saveChild() {
        String name = etChildName.getText() == null ? "" : etChildName.getText().toString().trim();

        if (name.isEmpty()) {
            layoutChildName.setError("Please enter the child's name");
            return;
        }
        layoutChildName.setError(null);

        // TODO: build a Child model and store it once the data layer exists.
        Toast.makeText(this, name + " added", Toast.LENGTH_SHORT).show();
        finish();
    }
}
