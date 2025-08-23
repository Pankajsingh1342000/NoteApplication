package com.example.noteapplicationmvvmflow.feature.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.databinding.ViewAudioPlayerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAudioPlayerBinding =
        ViewAudioPlayerBinding.inflate(LayoutInflater.from(context), this, true)

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var isPrepared = false
    private var currentPosition = 0
    private var duration = 0
    private var audioPath: String? = null

    private var progressRunnable: Runnable? = null

    var onAudioDeleted: (() -> Unit)? = null

    companion object {
        private const val TAG = "AudioPlayerView"
    }

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) pauseAudio() else playAudio()
        }

        binding.btnDelete.setOnClickListener {
            deleteAudio()
        }
    }

    fun setAudioPath(path: String) {
        audioPath = path
        prepareMediaPlayer()
    }

    private fun prepareMediaPlayer() {
        try {
            mediaPlayer?.release()
            isPrepared = false
            binding.btnPlayPause.isEnabled = false

            if (!isValidAudioFile(audioPath)) {
                Toast.makeText(context, "Invalid audio file", Toast.LENGTH_SHORT).show()
                return
            }

            mediaPlayer = MediaPlayer().apply {
                reset()
                setDataSource(audioPath)
                setOnPreparedListener { mp ->
                    this@AudioPlayerView.duration = mp.duration
                    isPrepared = true
                    binding.btnPlayPause.isEnabled = true
                    updateProgressDisplay()
                }
                setOnCompletionListener {
                    this@AudioPlayerView.isPlaying = false
                    this@AudioPlayerView.currentPosition = 0
                    updatePlayPauseButton()
                    stopProgressUpdate()
                    updateProgressDisplay()
                    seekTo(0)
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    Toast.makeText(context, "Error loading audio", Toast.LENGTH_SHORT).show()
                    binding.btnPlayPause.isEnabled = false
                    isPrepared = false
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing MediaPlayer", e)
        }
    }

    private fun isValidAudioFile(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        val file = File(path)
        return file.exists() && file.isFile && file.canRead()
    }

    private fun playAudio() {
        if (!isPrepared) {
            Toast.makeText(context, "Audio not ready yet...", Toast.LENGTH_SHORT).show()
            return
        }
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                if (currentPosition >= duration && duration > 0) {
                    currentPosition = 0
                    player.seekTo(0)
                }
                player.start()
                isPlaying = true
                updatePlayPauseButton()
                startProgressUpdate()
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
                updatePlayPauseButton()
                stopProgressUpdate()
            }
        }
    }

    fun deleteAudio() {
        try {
            if (isPlaying) {
                pauseAudio()
            }
            mediaPlayer?.release()
            mediaPlayer = null

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    audioPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Audio Player View", "Error deleting file", e)
                }
            }

            onAudioDeleted?.invoke()
        } catch (e: Exception) {
            Log.e("Audio Player View", "Error deleting audio", e)
        }
    }


    private fun updatePlayPauseButton() {
        binding.btnPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressRunnable = object : Runnable {
            override fun run() {
                if (isPlaying) {
                    mediaPlayer?.let { player ->
                        currentPosition = player.currentPosition
                        updateProgressDisplay()
                        postDelayed(this, 200)
                    }
                }
            }
        }
        post(progressRunnable!!)
    }

    private fun stopProgressUpdate() {
        progressRunnable?.let { removeCallbacks(it) }
        progressRunnable = null
    }

    private fun updateProgressDisplay() {
        val progress = if (duration > 0) (currentPosition * 100 / duration) else 0
        binding.progressBar.progress = progress

        val currentTime = formatTime(currentPosition)
        val totalTime = formatTime(duration)
        binding.tvProgress.text = "$currentTime / $totalTime"
    }

    private fun formatTime(ms: Int): String {
        val sec = ms / 1000
        val min = sec / 60
        val remaining = sec % 60
        return String.format("%02d:%02d", min, remaining)
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    fun getAudioPath(): String? = audioPath
}
