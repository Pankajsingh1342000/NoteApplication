package com.example.noteapplicationmvvmflow.feature.image

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.Coil
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.databinding.ViewImagePreviewBinding

class ImagePreview@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context,attrs, defStyleAttr) {
    private var imagePath: String? = null
    private val binding: ViewImagePreviewBinding = ViewImagePreviewBinding.inflate(LayoutInflater.from(context), this, true)
    private var currentRequest: ImageRequest? = null
    private var currentDisposable: Disposable? = null
    private val imageLoader: ImageLoader = Coil.imageLoader(context)

    var onImageDeleted : (() -> Unit)? = null

    init {
        setupClickListeners()
    }

    fun setImage(imagePath: String) {
        this.imagePath = imagePath

        currentDisposable?.dispose()

        currentRequest = ImageRequest.Builder(context)
            .data(imagePath)
            .target(binding.ivImage)
            .crossfade(false)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .size(1024, 1024)
            .allowHardware(true)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .build()

        currentDisposable = imageLoader.enqueue(currentRequest!!)
    }
    
    fun deleteImage() {
        try {
            currentDisposable?.dispose()
            
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
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentDisposable?.dispose()
    }

    fun getImagePath(): String? = imagePath
}