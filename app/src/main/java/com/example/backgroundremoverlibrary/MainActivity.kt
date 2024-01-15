package com.example.backgroundremoverlibrary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

import android.net.Uri
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.example.backgroundremoverlibrary.databinding.ActivityMainBinding
import com.slowmac.autobackgroundremover.BackgroundRemover
import com.slowmac.autobackgroundremover.OnBackgroundChangeListener


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val imageResult =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { selectedImageUri = it }
            binding.img.setImageURI(selectedImageUri)
        }
    private val backgroundResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedBackgroundUri = it
                binding.imgBackground.setImageURI(selectedBackgroundUri)

                // Check if an image is already selected, and if yes, set the background directly
                if (selectedImageUri != null) {
                    removeBg()
                }
            }
        }

    private var selectedBackgroundUri: Uri? = null
    private var currentBackgroundColor: Int = Color.WHITE // Initial background color
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.removeBgBtn.setOnClickListener {
            removeBg()
        }
        binding.changeBackgroundColorBtn.setOnClickListener {
            changeBackgroundColor()
        }
        binding.uploadBtn.setOnClickListener {
            imageResult.launch("image/*")
        }
        binding.applyFilterBtn.setOnClickListener {
            showFilterMenu()
        }
        binding.resetBackground.setOnClickListener {
            resetBackground()
        }
        binding.changeBackground.setOnClickListener {
            backgroundResult.launch("image/*")
        }
    }

    private fun removeBg() {
        binding.img.invalidate()
        BackgroundRemover.bitmapForProcessing(
            binding.img.drawable.toBitmap(),
            true,
            object : OnBackgroundChangeListener {
                override fun onSuccess(bitmap: Bitmap) {
                    binding.img.setImageBitmap(bitmap)
                    // After removing the background, set the removed background to a custom image
                    setBackgroundAfterRemoval()
                }

                override fun onFailed(exception: Exception) {
                    Toast.makeText(this@MainActivity, "Error Occurred", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun changeBackgroundColor() {
        // Instead of generating a random color, show a menu with predefined colors
        val popupMenu = PopupMenu(this, binding.changeBackgroundColorBtn)
        popupMenu.menuInflater.inflate(R.menu.color_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_blue -> currentBackgroundColor = Color.BLUE
                R.id.menu_item_red -> currentBackgroundColor = Color.RED
                R.id.menu_item_green -> currentBackgroundColor = Color.GREEN
                // Add more colors as needed
            }
            binding.img.setBackgroundColor(currentBackgroundColor)
            true
        }
        popupMenu.show()
    }


    private fun showFilterMenu() {
        val popupMenu = PopupMenu(this, binding.applyFilterBtn)
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_negative -> applyNegativeFilter()
                R.id.menu_item_sepia -> applySepiaFilter()
                R.id.menu_item_reset -> resetImage()
                // Add more filters as needed
            }
            true
        }
        popupMenu.show()
    }

    private fun applyNegativeFilter() {
        val originalBitmap = binding.img.drawable.toBitmap()
        val filteredBitmap = applyNegative(originalBitmap)
        binding.img.setImageBitmap(filteredBitmap)
    }

    private fun applySepiaFilter() {
        val originalBitmap = binding.img.drawable.toBitmap()
        val filteredBitmap = applySepia(originalBitmap)
        binding.img.setImageBitmap(filteredBitmap)
    }

    private fun applyNegative(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val negativeBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixelColor = bitmap.getPixel(x, y)
                val red = 255 - Color.red(pixelColor)
                val green = 255 - Color.green(pixelColor)
                val blue = 255 - Color.blue(pixelColor)
                negativeBitmap.setPixel(x, y, Color.argb(Color.alpha(pixelColor), red, green, blue))
            }
        }

        return negativeBitmap
    }

    private fun applySepia(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val sepiaBitmap = Bitmap.createBitmap(width, height, bitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixelColor = bitmap.getPixel(x, y)
                val red = (Color.red(pixelColor) * 0.393 + Color.green(pixelColor) * 0.769 + Color.blue(pixelColor) * 0.189).coerceAtMost(255.0)
                val green = (Color.red(pixelColor) * 0.349 + Color.green(pixelColor) * 0.686 + Color.blue(pixelColor) * 0.168).coerceAtMost(255.0)
                val blue = (Color.red(pixelColor) * 0.272 + Color.green(pixelColor) * 0.534 + Color.blue(pixelColor) * 0.131).coerceAtMost(255.0)
                sepiaBitmap.setPixel(x, y, Color.argb(Color.alpha(pixelColor), red.toInt(), green.toInt(), blue.toInt()))
            }
        }
        return sepiaBitmap
    }
    private fun resetImage() {
        selectedImageUri?.let { uri ->
            binding.img.setImageURI(uri)
            currentBackgroundColor = Color.TRANSPARENT // Set the initial background color to transparent
            binding.img.setBackgroundColor(currentBackgroundColor)
        }
    }

    private fun setBackgroundAfterRemoval() {
        selectedBackgroundUri?.let { backgroundUri ->
            val backgroundBitmap = binding.imgBackground.drawable.toBitmap()
            val originalBitmap = binding.img.drawable.toBitmap()

            // Resize background bitmap to match the original image size
            val resizedBackgroundBitmap = Bitmap.createScaledBitmap(
                backgroundBitmap,
                originalBitmap.width,
                originalBitmap.height,
                false
            )

            // Create a new Bitmap to hold the result
            val resultBitmap = Bitmap.createBitmap(
                originalBitmap.width,
                originalBitmap.height,
                originalBitmap.config
            )

            // Create a Canvas and draw the resized background first
            val canvas = Canvas(resultBitmap)
            canvas.drawBitmap(resizedBackgroundBitmap, 0f, 0f, null)

            // Draw the processed image on top of the background
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)

            // Set the combined bitmap as the image in your ImageView
            binding.img.setImageBitmap(resultBitmap)
        }
    }
    private fun resetBackground() {
        selectedImageUri?.let { uri ->
            binding.img.setImageURI(uri)
            currentBackgroundColor = Color.TRANSPARENT // Set the initial background color to transparent
            binding.img.setBackgroundColor(currentBackgroundColor)
            binding.imgBackground.setImageDrawable(null) // Clear the background image
        }
    }


}