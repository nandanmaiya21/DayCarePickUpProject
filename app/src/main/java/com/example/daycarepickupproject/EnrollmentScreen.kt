package com.example.daycarepickupproject

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Enrollment screen. In guardian mode it collects a guardian's details,
 * the children they may pick up, and (later) their face photos.
 */
class EnrollmentScreen : AppCompatActivity() {

    companion object {
        /** Intent extra telling the screen what is being enrolled. */
        const val EXTRA_MODE = "enrollment_mode"
        const val MODE_GUARDIAN = "guardian"
        const val MODE_CHILD = "child"

        private const val MAX_PHOTOS = 5

        // Sample children until the data layer exists.
        private val SAMPLE_CHILDREN = listOf("Ava Okafor", "Leo Martin", "Zara Khan", "Mia Chen")
    }

    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var chipsRelationship: ChipGroup
    private lateinit var chipsChildren: ChipGroup
    private lateinit var cbConsent: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enrollment_screen)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            // Include the keyboard so the form and Save button stay visible while typing
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_GUARDIAN
        if (mode == MODE_CHILD) {
            showChildPlaceholder()
        } else {
            setUpGuardianForm()
        }
    }

    /** The child form isn't built yet: show a title and a short note. */
    private fun showChildPlaceholder() {
        findViewById<TextView>(R.id.tvTitle).setText(R.string.enrol_title_child)
        findViewById<View>(R.id.guardianForm).visibility = View.GONE
        findViewById<View>(R.id.btnSave).visibility = View.GONE
        findViewById<View>(R.id.tvChildPlaceholder).visibility = View.VISIBLE
    }

    private fun setUpGuardianForm() {
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        chipsRelationship = findViewById(R.id.chipsRelationship)
        chipsChildren = findViewById(R.id.chipsChildren)
        cbConsent = findViewById(R.id.cbConsent)

        // Relationship: pick one
        listOf(
            R.string.rel_father, R.string.rel_mother, R.string.rel_grandparent,
            R.string.rel_relative, R.string.rel_other
        ).forEach { chipsRelationship.addView(createChip(getString(it))) }

        // Children: pick one or more
        SAMPLE_CHILDREN.forEach { chipsChildren.addView(createChip(it)) }

        // Face photo capture is the next step
        findViewById<TextView>(R.id.tvPhotoCount).text =
            getString(R.string.enrol_photos_count, 0, MAX_PHOTOS)
        findViewById<View>(R.id.btnTakePhotos).setOnClickListener {
            Toast.makeText(this, R.string.enrol_photos_coming, Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnSave).setOnClickListener { saveGuardian() }
    }

    private fun createChip(label: String): Chip {
        val chip = layoutInflater.inflate(R.layout.item_choice_chip, null) as Chip
        chip.id = View.generateViewId() // ChipGroup needs ids for selection tracking
        chip.text = label
        return chip
    }

    private fun saveGuardian() {
        val name = etFullName.text.toString().trim()

        // Stop at the first problem so the person fixes one thing at a time
        val error = when {
            name.isEmpty() -> R.string.enrol_error_name
            chipsRelationship.checkedChipId == View.NO_ID -> R.string.enrol_error_relationship
            chipsChildren.checkedChipIds.isEmpty() -> R.string.enrol_error_children
            !cbConsent.isChecked -> R.string.enrol_error_consent
            else -> null
        }
        if (error != null) {
            if (error == R.string.enrol_error_name) etFullName.error = getString(error)
            else Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            return
        }

        // Details are valid. Storing them comes with the data layer.
        Toast.makeText(this, R.string.enrol_saved, Toast.LENGTH_LONG).show()
        finish()
    }
}