package com.example.daycarepickupproject


import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Enrollment screen where a parent registers and their photo is captured.
 */
class EnrollmentScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_enrollment_screen)

        val root = findViewById<View?>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(
            root,
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val bars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            })
    }
}