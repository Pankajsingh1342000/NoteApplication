package com.example.noteapplicationmvvmflow.feature.image

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.Coil
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.databinding.ViewCompactImagePreviewBinding

class CompactImagePreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCompactImagePreviewBinding =
        ViewCompactImagePreviewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    private var currentDisposable: Disposable? = null
    private val imageLoader: ImageLoader = Coil.imageLoader(context)
    private var imagePath: String? = null

    fun setImage(imagePath: String) {
        this.imagePath = imagePath

        currentDisposable?.dispose()

        val request = ImageRequest.Builder(context)
            .data(imagePath)
            .target(binding.ivImage)
            .crossfade(true)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .size(1024, 1024)
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
            .build()

        currentDisposable = imageLoader.enqueue(request)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (currentDisposable == null) {
            imagePath?.let { setImage(it) }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelImageLoading()
    }

    fun cancelImageLoading() {
        currentDisposable?.dispose()
        currentDisposable = null
    }
}
