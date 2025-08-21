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
    private var currentPosition = 0
    private var duration = 0

    companion object {
        private const val TAG = "CompactAudioPlayerView"
    }

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }
    }

    fun setAudioPath(path: String) {
        Log.d(TAG, "Setting audio path: $path")
        audioPath = path

        if (!isValidAudioFile(path)) {
            Log.e(TAG, "Audio file does not exist: $path")
            binding.btnPlayPause.isEnabled = false
            return
        }
        resetAudio()
        prepareMediaPlayer()
    }

    private fun isValidAudioFile(path: String): Boolean {
        val file = File(path)
        val exists = file.exists()
        val isFile = file.isFile
        val canRead = file.canRead()

        Log.d(TAG, "File validation - exists: $exists, isFile: $isFile, canRead: $canRead")

        return exists && isFile && canRead
    }

    private fun prepareMediaPlayer() {
        try {
            Log.d(TAG, "Preparing MediaPlayer for path: $audioPath")

            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)

                setOnCompletionListener { mp ->
                    Log.d(TAG, "Audio playback completed")
                    this@CompactAudioPlayerView.isPlaying = false
                    this@CompactAudioPlayerView.currentPosition = 0
                    updatePlayPauseButton()
                    stopProgressUpdate()
                    updateProgressDisplay()
                    // Reset to beginning for next play
                    mp.seekTo(0)
                    resetAudio()
                }

                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer prepared successfully")
                    this@CompactAudioPlayerView.duration = mp.duration
                    Log.d(TAG, "Audio duration: $duration ms")
                    binding.btnPlayPause.isEnabled = true
                    // Reset position to beginning
                    this@CompactAudioPlayerView.currentPosition = 0
                    updateProgressDisplay()
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error - what: $what, extra: $extra")
                    Toast.makeText(context, "Error loading audio", Toast.LENGTH_SHORT).show()
                    binding.btnPlayPause.isEnabled = false
                    true
                }

                prepareAsync()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare MediaPlayer", e)
            Toast.makeText(context, "Failed to load audio: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.btnPlayPause.isEnabled = false
        }
    }

    private fun playAudio() {
        mediaPlayer?.let { player ->
            try {
                if (!player.isPlaying) {
                    Log.d(TAG, "Starting audio playback")

                    if (currentPosition >= duration && duration > 0) {
                        Log.d(TAG, "Resetting audio to beginning")
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
        } ?: run {
            Log.e(TAG, "MediaPlayer is null, cannot play audio")
            Toast.makeText(context, "Audio not ready", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    Log.d(TAG, "Pausing audio playback")
                    player.pause()
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
        if(isPlaying){
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        }else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun startProgressUpdate() {
        post(object : Runnable {
            override fun run() {
                if (isPlaying && mediaPlayer != null) {
                    try {
                        currentPosition = mediaPlayer?.currentPosition ?: 0
                        updateProgressDisplay()

                        // Check if we've reached the end
                        if (currentPosition >= duration && duration > 0) {
                            Log.d(TAG, "Audio reached end, stopping progress updates")
                            stopProgressUpdate()
                        } else {
                            postDelayed(this, 100)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating progress", e)
                        stopProgressUpdate()
                    }
                }
            }
        })
    }

    private fun stopProgressUpdate() {
        removeCallbacks(null)
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

    private fun formatTime(milliseconds: Int): String {
        return try {
            val seconds = (milliseconds / 1000).toInt()
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            String.format("%02d:%02d", minutes, remainingSeconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time", e)
            "00:00"
        }
    }

    fun resetAudio() {
        mediaPlayer?.let { player ->
            try {
                if (isPlaying) {
                    player.pause()
                    isPlaying = false
                    updatePlayPauseButton()
                    stopProgressUpdate()
                }
                currentPosition = 0
                player.seekTo(0)
                updateProgressDisplay()
                Log.d(TAG, "Audio reset to beginning")
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting audio", e)
            }
        }
    }

    fun release() {
        try {
            Log.d(TAG, "Releasing MediaPlayer")
            stopProgressUpdate()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            currentPosition = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
    }

    fun pauseIfPlaying() {
        if (isPlaying) {
            pauseAudio()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}