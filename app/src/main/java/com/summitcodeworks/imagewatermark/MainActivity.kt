package com.summitcodeworks.imagewatermark

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso
import com.summitcodeworks.watermarklib.TextWatermarkOptions
import com.summitcodeworks.watermarklib.WatermarkUtil

class MainActivity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_CODE = 1001

    private lateinit var ivMain: ImageView
    private lateinit var bMain: Button
    private lateinit var spinnerPlacement: Spinner
    private lateinit var spinnerTiling: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ivMain = findViewById(R.id.ivMain)
        bMain = findViewById(R.id.bMain)
        spinnerPlacement = findViewById(R.id.spinnerPlacement)
        spinnerTiling = findViewById(R.id.spinnerTiling)

        setupSpinners()

        bMain.setOnClickListener {
            checkPermissionsAndPickImage()
        }
    }

    private fun setupSpinners() {
        val placementOptions = arrayOf("Select Position", "Top Left", "Top Right", "Bottom Left", "Bottom Right", "Center")
        val tilingOptions = arrayOf("Select Tiling", "No Tiling", "Tile Horizontally", "Tile Vertically", "Tile Both Directions")

        val placementAdapter = ArrayAdapter(this, R.layout.spinner_item, placementOptions)
        val tilingAdapter = ArrayAdapter(this, R.layout.spinner_item, tilingOptions)

        placementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tilingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerPlacement.adapter = placementAdapter
        spinnerTiling.adapter = tilingAdapter

        spinnerPlacement.setSelection(0)
        spinnerTiling.setSelection(0)

        spinnerPlacement.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    applyWatermark()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTiling.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) {
                    applyWatermark()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                processSelectedImage(it)
            }
        }
    }

    private fun processSelectedImage(imageUri: Uri) {
        Picasso.get().load(imageUri).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    ivMain.setImageBitmap(it)
                    applyWatermark()
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Toast.makeText(this@MainActivity, "Failed to load image", Toast.LENGTH_SHORT).show()
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
        })
    }

    private fun applyWatermark() {
        val drawable = ivMain.drawable
        if (drawable != null) {
            val bitmap = drawable.toBitmap()
            Log.d("Watermark", "Bitmap size: ${bitmap.width} x ${bitmap.height}")

            val placementIndex = spinnerPlacement.selectedItemPosition + 1
            val tilingIndex = spinnerTiling.selectedItemPosition

            val x = getPositionX(placementIndex)
            val y = getPositionY(placementIndex)
            val tile = tilingIndex != 0

            val watermarkedBitmap = WatermarkUtil.addTextWatermark(
                context = this,
                originalBitmap = bitmap,
                options = TextWatermarkOptions(
                    text = "Your Watermark",
                    x = x,
                    y = y,
                    tile = tile
                )
            )
            ivMain.setImageBitmap(watermarkedBitmap)
            ivMain.invalidate()
        } else {
            Toast.makeText(this, "No image to watermark", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPositionX(positionIndex: Int): Float {
        return when (positionIndex) {
            1 -> 0f // Top Left
            2 -> ivMain.width.toFloat() - 100 // Top Right
            3 -> 0f // Bottom Left
            4 -> ivMain.width.toFloat() - 100 // Bottom Right
            5 -> (ivMain.width - 100) / 2f // Center
            else -> 0f
        }
    }

    private fun getPositionY(positionIndex: Int): Float {
        return when (positionIndex) {
            1 -> 100f // Top Left
            2 -> 100f // Top Right
            3 -> ivMain.height.toFloat() - 100 // Bottom Left
            4 -> ivMain.height.toFloat() - 100 // Bottom Right
            5 -> (ivMain.height - 100) / 2f // Center
            else -> 0f
        }
    }

    private fun checkPermissionsAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_CODE
                )
            } else {
                pickImageFromGallery()
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE
                )
            } else {
                pickImageFromGallery()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageFromGallery()
            } else {
                Toast.makeText(this, "Permission denied to read media images", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
