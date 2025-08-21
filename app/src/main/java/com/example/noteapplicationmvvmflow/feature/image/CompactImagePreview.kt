package com.example.noteapplicationmvvmflow.feature.image

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.load
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.example.noteapplicationmvvmflow.databinding.ViewCompactImagePreviewBinding

class CompactImagePreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewCompactImagePreviewBinding = ViewCompactImagePreviewBinding.inflate(LayoutInflater.from(context), this, true)
    var imagePath: String? = null

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
}