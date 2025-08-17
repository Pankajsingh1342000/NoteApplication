package com.example.noteapplicationmvvmflow.feature.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.noteapplicationmvvmflow.databinding.FragmentAudioBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioFragment : Fragment() {
    private lateinit var binding: FragmentAudioBinding
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null
    private var recordingStartTime: Long = 0

    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        isGranted: Boolean ->
        if (isGranted) {
            startRecording()
        }
        else{
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAudioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        checkPermission()
    }

    private fun setupClickListeners(){
        binding.btnRecord.setOnClickListener {
            if (isRecording){
                stopRecording()
            } else {
                checkPermission()
            }
        }

        binding.btnFinish.setOnClickListener {

            if(audioFilePath != null ){
                navigateToAddFragment()
            }else {
                Toast.makeText(context, "Please Record Audio First", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(context, "Audio Permission is Required", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            else -> {
                permissionRequestLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startRecording() {
        try {
            val audioFile = createAudioFile()
            audioFilePath = audioFile.absolutePath

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            updateUI()
            startTimer()
        } catch (e: IOException) {
            Toast.makeText(context, "Failed to Start Recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            updateUI()
            stopTimer()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to Stop Recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUDIO_$timeStamp.m4a"
        val storageDir = requireContext().getExternalFilesDir(null)
        val file = File(storageDir, fileName)

        Log.d("AudioFragment", "Creating audio file: ${file.absolutePath}")
        Log.d("AudioFragment", "Storage directory exists: ${storageDir?.exists()}")
        Log.d("AudioFragment", "Storage directory path: ${storageDir?.absolutePath}")

        return file
    }

    private fun updateUI() {
        if (isRecording) {
            binding.btnRecord.text = "Stop Recording"
            binding.btnRecord.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            binding.btnFinish.visibility = View.GONE
            binding.tvStatus.text = "Recording..."
        } else {
            binding.btnRecord.text = "Record Again"
            binding.btnRecord.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
            binding.btnFinish.visibility = View.VISIBLE
            binding.tvStatus.text = "Recording Completed"
        }
    }

    private fun startTimer() {
        binding.tvTimer.post(object : Runnable {
            override fun run() {
                if (isRecording) {
                    val elapsedTime = System.currentTimeMillis() - recordingStartTime
                    val seconds = (elapsedTime/1000).toInt()
                    val minutes = seconds/60
                    val remainingSeconds = seconds%60
                    binding.tvTimer.text = String.format("%02d:%02d", minutes, remainingSeconds)
                    binding.tvTimer.postDelayed(this, 1000)
                }
            }

        })
    }

    private fun stopTimer() {
        binding.tvTimer.removeCallbacks(null)
    }

    private fun navigateToAddFragment() {
        Log.d("AudioFragment", "Navigating to AddFragment")
        Log.d("AudioFragment", "Audio file path: $audioFilePath")
        Log.d("AudioFragment", "Audio file exists: ${File(audioFilePath!!).exists()}")
        Log.d("AudioFragment", "Audio file size: ${File(audioFilePath!!).length()} bytes")

        val action = AudioFragmentDirections.actionAudioFragmentToAddFragment(
            contentType = "audio",
            audioPath = audioFilePath!!
        )
        findNavController().navigate(action)
    }


}