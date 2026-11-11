package com.example.daycarepickupproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.daycarepickupproject.Models.PinModel;


public class LockScreen extends AppCompatActivity {

    private static final int[] DIGIT_IDS = {
            R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
            R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
    };

    private final PinModel pinModel = new PinModel();

    private View[] dots;
    private TextView tvSubTitle;

    private ConstraintLayout main;
    private TextView tvTitle;
    private LinearLayout pinDotsRow, footer;
    private View dot1, dot2, dot3, dot4;
    private GridLayout keypad;
    private TextView[] digitKeys;
    private ImageButton keyBackspace;

    private void bindViews() {
        main = findViewById(R.id.main);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        pinDotsRow = findViewById(R.id.pinDots);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);
        dots = new View[]{dot1, dot2, dot3, dot4};
        keypad = findViewById(R.id.keypad);
        digitKeys = new TextView[DIGIT_IDS.length];
        for (int i = 0; i < DIGIT_IDS.length; i++) {
            digitKeys[i] = findViewById(DIGIT_IDS[i]);
        }
        keyBackspace = findViewById(R.id.keyBackspace);
        footer = findViewById(R.id.footer);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lock_screen);
        bindViews();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvSubTitle = findViewById(R.id.tvSubTitle);
        pinDotsRow = findViewById(R.id.pinDots);
        dots = new View[] {
                findViewById(R.id.dot1), findViewById(R.id.dot2),
                findViewById(R.id.dot3), findViewById(R.id.dot4)
        };

        // Digit keys: the key's own text is the digit
        View.OnClickListener digitListener = v -> {
            String digit = ((TextView) v).getText().toString();
            if (pinModel.addDigit(digit)) {
                showInstruction();
                updateDots();
                if (pinModel.isComplete()) {
                    checkPin();
                }
            }
        };
        for (int id : DIGIT_IDS) {
            findViewById(id).setOnClickListener(digitListener);
        }

        findViewById(R.id.keyBackspace).setOnClickListener(v -> {
            pinModel.removeLastDigit();
            updateDots();
        });
    }

    private void checkPin() {
        if (pinModel.isCorrect()) {
            startActivity(new Intent(this, HomeScreen.class));
            finish(); // so Back from Home doesn't return to the lock screen
        } else {
            showWrongPin();
        }
    }

    private void updateDots() {
        int length = pinModel.getLength();
        for (int i = 0; i < dots.length; i++) {
            dots[i].setBackgroundResource(i < length
                    ? R.drawable.bg_pin_dot_filled
                    : R.drawable.bg_pin_dot_empty);
        }
    }

    private void showWrongPin() {
        tvSubTitle.setText(R.string.lock_wrong_pin);
        tvSubTitle.setTextColor(ContextCompat.getColor(this, R.color.status_rejected_text));


        pinDotsRow.animate().translationX(24f).setDuration(50).withEndAction(() ->
                pinDotsRow.animate().translationX(-24f).setDuration(80).withEndAction(() ->
                        pinDotsRow.animate().translationX(0f).setDuration(50).withEndAction(() -> {
                            pinModel.clear();
                            updateDots();
                        })));
    }

    private void showInstruction() {
        tvSubTitle.setText(R.string.lockScreenInstructionText);
        tvSubTitle.setTextColor(ContextCompat.getColor(this, R.color.grey));
    }
}
