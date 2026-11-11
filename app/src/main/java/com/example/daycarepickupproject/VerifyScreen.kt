package com.example.daycarepickupproject

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

/**
 * Verification screen where the person picking up is photographed and checked.
 * The button opens the device camera and shows the photo that comes back.
 * Face matching is not built yet, so the result card shows a sample result.
 */
class VerifyScreen : AppCompatActivity() {

    private lateinit var cameraPreview: View
    private lateinit var ivCapturedPhoto: ImageView
    private lateinit var resultCard: View
    private lateinit var tvResultName: TextView
    private lateinit var btnCapture: MaterialButton

    // Opens the camera app and returns a small photo (null if the user cancels)
    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo: Bitmap? ->
            if (photo != null) {
                showPhoto(photo)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_screen)

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        cameraPreview = findViewById(R.id.cameraPreview)
        ivCapturedPhoto = findViewById(R.id.ivCapturedPhoto)
        resultCard = findViewById(R.id.resultCard)
        tvResultName = findViewById(R.id.tvResultName)
        btnCapture = findViewById(R.id.btnCapture)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        btnCapture.setOnClickListener { takePhoto.launch(null) }
    }

    private fun showPhoto(photo: Bitmap) {
        ivCapturedPhoto.setImageBitmap(photo)
        ivCapturedPhoto.visibility = View.VISIBLE
        cameraPreview.visibility = View.GONE

        // TODO: replace with the real face match result.
        tvResultName.text = "Daniel Okafor"
        resultCard.visibility = View.VISIBLE

        btnCapture.text = "Retake photo"
    }
}