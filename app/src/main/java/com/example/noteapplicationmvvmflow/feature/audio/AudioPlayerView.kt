package com.example.noteapplicationmvvmflow.feature.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.noteapplicationmvvmflow.R
import com.example.noteapplicationmvvmflow.databinding.ViewAudioPlayerBinding
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

//        binding.btnStop.setOnClickListener {
//            stopAudio()
//        }
    }

    fun setAudioPath(path: String) {
        audioPath = path
        prepareMediaPlayer()
    }

    private fun prepareMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)

                // Add completion listener
                setOnCompletionListener { mp ->
                    Log.d("AudioPlayerView", "Audio playback completed")
                    this@AudioPlayerView.isPlaying = false
                    this@AudioPlayerView.currentPosition = 0
                    updatePlayPauseButton()
                    stopProgressUpdate()
                    updateProgressDisplay()
                    // Reset to beginning for next play
                    mp.seekTo(0)
                }

                setOnPreparedListener { mp ->
                    this@AudioPlayerView.duration = mp.duration
                    updateProgressDisplay()
                }

                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playAudio() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                // If we're at the end, reset to beginning
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

    private fun deleteAudio() {
        try {
            if (isPlaying){
                pauseAudio()
            }
            mediaPlayer?.release()
            mediaPlayer = null

            audioPath?.let { path ->
                val file = File(path)
                if (file.exists()){
                    file.delete()
                }
            }
            audioPath = null
            onAudioDeleted?.invoke()

        } catch (e: Exception) {
            Log.e("AudioPlayerView", "Error deleting audio", e)
        }
    }

//    private fun stopAudio() {
//        mediaPlayer?.let { player ->
//            player.stop()
//            player.prepare()
//            currentPosition = 0
//            isPlaying = false
//            updatePlayPauseButton()
//            stopProgressUpdate()
//            updateProgressDisplay()
//        }
//    }

    fun resetAudio() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
                updatePlayPauseButton()
                stopProgressUpdate()
            }
            currentPosition = 0
            player.seekTo(0)
            updateProgressDisplay()
        }
    }

    private fun updatePlayPauseButton() {
        if(isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        }else{
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
//        binding.btnPlayPause.text = if (isPlaying) "⏸️" else "▶️"
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
        mediaPlayer?.release()
        mediaPlayer = null
    }

}