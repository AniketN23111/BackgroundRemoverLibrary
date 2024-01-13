package com.example.backgroundremoverlibrary

import android.graphics.Bitmap
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
            uri?.let { uri ->
                binding.img.setImageURI(uri)
            }
        }

    private var currentBackgroundColor: Int = Color.WHITE // Initial background color

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
    }

    private fun removeBg() {
        binding.img.invalidate()
        BackgroundRemover.bitmapForProcessing(
            binding.img.drawable.toBitmap(),
            true,
            object : OnBackgroundChangeListener {
                override fun onSuccess(bitmap: Bitmap) {
                    binding.img.setImageBitmap(bitmap)
                }

                override fun onFailed(exception: Exception) {
                    Toast.makeText(this@MainActivity, "Error Occurred", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun changeBackgroundColor() {
        currentBackgroundColor = getRandomColor()
        binding.img.setBackgroundColor(currentBackgroundColor)
    }

    private fun getRandomColor(): Int {
        // Generate a random color for the background
        return Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }

    private fun showFilterMenu() {
        val popupMenu = PopupMenu(this, binding.applyFilterBtn)
        popupMenu.menuInflater.inflate(R.menu.filter_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_item_negative -> applyNegativeFilter()
                R.id.menu_item_sepia -> applySepiaFilter()
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

}