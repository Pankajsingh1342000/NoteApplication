package com.example.noteapplicationmvvmflow.feature.image

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.load
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.example.noteapplicationmvvmflow.databinding.ViewImagePreviewBinding

class ImagePreview@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context,attrs, defStyleAttr) {
    var imagePath: String? = null
    private val binding: ViewImagePreviewBinding = ViewImagePreviewBinding.inflate(LayoutInflater.from(context), this, true)

    var onImageDeleted : (() -> Unit)? = null

    init {
        setupClickListeners()
    }

    fun setImage(imagePath: String) {
        this.imagePath = imagePath
        binding.ivImage.load(imagePath) {
            crossfade(false)
            scale(Scale.FIT)
            precision(Precision.INEXACT)
            size(Size.ORIGINAL)
            allowHardware(true)
        }
    }
    fun deleteImage() {
        try {

            imagePath = null
            onImageDeleted?.invoke()

        } catch (e: Exception) {
            Log.e("AudioPlayerView", "Error deleting audio", e)
        }
    }

    private fun setupClickListeners() {

        binding.btnDelete.setOnClickListener {
            deleteImage()
        }
    }
}