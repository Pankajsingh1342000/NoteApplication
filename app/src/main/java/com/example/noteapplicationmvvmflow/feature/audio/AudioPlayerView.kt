package com.example.noteapplicationmvvmflow.feature.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
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
): FrameLayout(context,attrs, defStyleAttr) {

    private val binding: ViewAudioPlayerBinding = ViewAudioPlayerBinding.inflate(LayoutInflater.from(context), this, true)
    private var mediaPlayer: MediaPlayer? = null
    private var audioPath: String? = null
    private var isPlaying = false
    private var currentPosition = 0
    private var duration = 0

    var onAudioDeleted : (() -> Unit)? = null

    init {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            }else {
                playAudio()
            }
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
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                setOnPreparedListener { mp ->
                    this@AudioPlayerView.duration = mp.duration
                    updateProgressDisplay()
                }
                setOnCompletionListener { mp ->
                    this@AudioPlayerView.isPlaying = false
                    this@AudioPlayerView.currentPosition = 0
                    updatePlayPauseButton()
                    stopProgressUpdate()
                    updateProgressDisplay()
                    mp.seekTo(0)
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAudio() {
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
            audioPath = null

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
        if(isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        }else{
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun startProgressUpdate() {
        post(object : Runnable {
            override fun run() {
                if (isPlaying) {
                    mediaPlayer?.let { player ->
                        currentPosition = player.currentPosition
                        updateProgressDisplay()
                        postDelayed(this, 100)
                    }
                }
            }
        })
    }

    private fun stopProgressUpdate() {
        removeCallbacks(null)
    }

    private fun updateProgressDisplay() {
        val progress = if (duration > 0) (currentPosition * 100 / duration) else 0
        binding.progressBar.progress = progress

        val currentTime = formatTime(currentPosition)
        val totalTime = formatTime(duration)
        binding.tvProgress.text = "$currentTime / $totalTime"
    }


    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun release() {

        try {
            Log.d("Audio Player View", "Releasing MediaPlayer")
            stopProgressUpdate()
            mediaPlayer?.release()
            mediaPlayer = null
            isPlaying = false
            currentPosition = 0
        } catch (e: Exception) {
            Log.e("Audio Player View", "Error releasing MediaPlayer", e)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

}