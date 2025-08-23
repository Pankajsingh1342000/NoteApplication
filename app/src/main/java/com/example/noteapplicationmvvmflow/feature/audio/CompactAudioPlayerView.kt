package com.example.noteapplicationmvvmflow.feature.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.databinding.ViewCompactAudioPlayerBinding
import java.io.File

class CompactAudioPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewCompactAudioPlayerBinding =
        ViewCompactAudioPlayerBinding.inflate(LayoutInflater.from(context), this, true)

    private var mediaPlayer: MediaPlayer? = null
    private var audioPath: String? = null
    private var isPlaying = false
    private var isPrepared = false
    private var currentPosition = 0
    private var duration = 0

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && mediaPlayer != null) {
                try {
                    currentPosition = mediaPlayer?.currentPosition ?: 0
                    updateProgressDisplay()

                    if (currentPosition < duration) {
                        postDelayed(this, 100)
                    } else {
                        stopProgressUpdate()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating progress", e)
                    stopProgressUpdate()
                }
            }
        }
    }

    companion object {
        private const val TAG = "CompactAudioPlayerView"
    }

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) pauseAudio() else playAudio()
        }
    }

    fun setAudioPath(path: String) {
        Log.d(TAG, "Setting audio path: $path")
        audioPath = path

        if (!isValidAudioFile(path)) {
            Log.e(TAG, "Invalid audio file: $path")
            binding.btnPlayPause.isEnabled = false
            binding.progressBar.progress = 0
            binding.tvProgress.text = "00:00 / 00:00"
            release()
            return
        }

        resetAudio()
        prepareMediaPlayer()
    }

    private fun isValidAudioFile(path: String): Boolean {
        val file = File(path)
        return file.exists() && file.isFile && file.canRead()
    }

    private fun prepareMediaPlayer() {
        try {
            mediaPlayer?.release()
            isPrepared = false
            binding.btnPlayPause.isEnabled = false // Disable button while preparing

            mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(audioPath)

                setOnPreparedListener { mp ->
                    isPrepared = true
                    this@CompactAudioPlayerView.duration = mp.duration
                    Log.d(TAG, "Prepared. Duration: $duration ms")
                    binding.btnPlayPause.isEnabled = true
                    this@CompactAudioPlayerView.currentPosition = 0
                    updateProgressDisplay()
                }

                setOnCompletionListener {
                    this@CompactAudioPlayerView.isPlaying = false
                    this@CompactAudioPlayerView.currentPosition = 0
                    updatePlayPauseButton()
                    stopProgressUpdate()
                    updateProgressDisplay()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    Toast.makeText(context, "Error loading audio", Toast.LENGTH_SHORT).show()
                    binding.btnPlayPause.isEnabled = false
                    isPrepared = false
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare MediaPlayer", e)
            binding.btnPlayPause.isEnabled = false
        }
    }

    private fun playAudio() {
        if (!isPrepared) {
            Toast.makeText(context, "Audio is still loading...", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer?.let { player ->
            try {
                if (!player.isPlaying) {
                    if (currentPosition >= duration && duration > 0) {
                        resetAudio()
                    }
                    player.start()
                    isPlaying = true
                    updatePlayPauseButton()
                    startProgressUpdate()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
                Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.pause()
                    isPlaying = false
                    updatePlayPauseButton()
                    stopProgressUpdate()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing audio", e)
            }
        }
    }

    private fun updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun startProgressUpdate() {
        removeCallbacks(progressRunnable)
        post(progressRunnable)
    }

    private fun stopProgressUpdate() {
        removeCallbacks(progressRunnable)
    }

    private fun updateProgressDisplay() {
        try {
            val progress = if (duration > 0) (currentPosition * 100 / duration) else 0
            binding.progressBar.progress = progress
            val currentTime = formatTime(currentPosition)
            val totalTime = formatTime(duration)
            binding.tvProgress.text = "$currentTime / $totalTime"
        } catch (e: Exception) {
            Log.e(TAG, "Error updating progress display", e)
        }
    }

    private fun formatTime(ms: Int): String {
        val sec = ms / 1000
        val min = sec / 60
        val remaining = sec % 60
        return String.format("%02d:%02d", min, remaining)
    }

    fun resetAudio() {
        mediaPlayer?.let {
            try {
                if (isPlaying) {
                    it.pause()
                    isPlaying = false
                    updatePlayPauseButton()
                    stopProgressUpdate()
                }
                currentPosition = 0
                it.seekTo(0)
                updateProgressDisplay()
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting audio", e)
            }
        }
    }

    fun release() {
        try {
            stopProgressUpdate()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            isPrepared = false
            currentPosition = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Preload when view comes into screen
        if (!isPrepared && audioPath != null) {
            prepareMediaPlayer()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}
