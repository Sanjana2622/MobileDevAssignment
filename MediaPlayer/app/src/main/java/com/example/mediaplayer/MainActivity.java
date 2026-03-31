package com.example.mediaplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mediaplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // MediaPlayer handles local audio file playback
    private MediaPlayer mediaPlayer;

    // Flag to track current mode: true = audio, false = video
    private boolean isAudioMode = false;

    // Handler to update SeekBar periodically while audio plays
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable seekRunnable;

    // File picker launcher for selecting audio from device storage
    private final ActivityResultLauncher<String> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) playAudioFromUri(uri);
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSeekRunnable();
        setupButtons();
        setupSeekBar();
    }

    /** Initialize the SeekBar update runnable */
    private void setupSeekRunnable() {
        seekRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 500);
                }
            }
        };
    }

    /** Wire up all playback control buttons */
    private void setupButtons() {

        // Open audio file from device storage
        binding.btnOpenFile.setOnClickListener(v -> {
            isAudioMode = true;
            binding.videoView.stopPlayback();
            filePickerLauncher.launch("audio/*");
        });

        // Stream a video from a hardcoded sample URL
        binding.btnOpenUrl.setOnClickListener(v -> {
            isAudioMode = false;
            stopAudio();
            streamVideo("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4");
        });

        // PLAY button
        binding.btnPlay.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    handler.post(seekRunnable);
                    setStatus("Playing audio");
                } else if (mediaPlayer == null) {
                    Toast.makeText(this, "Open an audio file first", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!binding.videoView.isPlaying()) {
                    binding.videoView.start();
                    setStatus("Playing video");
                }
            }
        });

        // PAUSE button
        binding.btnPause.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    setStatus("Audio paused");
                }
            } else {
                if (binding.videoView.isPlaying()) {
                    binding.videoView.pause();
                    setStatus("Video paused");
                }
            }
        });

        // STOP button
        binding.btnStop.setOnClickListener(v -> {
            if (isAudioMode) {
                stopAudio();
                setStatus("Audio stopped");
            } else {
                binding.videoView.stopPlayback();
                setStatus("Video stopped");
            }
        });

        // RESTART button
        binding.btnRestart.setOnClickListener(v -> {
            if (isAudioMode) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    handler.post(seekRunnable);
                    setStatus("Audio restarted");
                }
            } else {
                binding.videoView.seekTo(0);
                binding.videoView.start();
                setStatus("Video restarted");
            }
        });
    }

    /** Play audio file from the given URI using MediaPlayer */
    private void playAudioFromUri(Uri uri) {
        try {
            stopAudio(); // release any existing player first
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            binding.seekBar.setMax(mediaPlayer.getDuration());
            handler.post(seekRunnable);
            mediaPlayer.setOnCompletionListener(mp -> setStatus("Audio playback complete"));
            setStatus("Playing: " + uri.getLastPathSegment());
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Stream a video from URL using VideoView + MediaController */
    private void streamVideo(String url) {
        try {
            MediaController mc = new MediaController(this);
            mc.setAnchorView(binding.videoView);
            binding.videoView.setMediaController(mc);
            binding.videoView.setVideoURI(Uri.parse(url));
            binding.videoView.setOnPreparedListener(mp -> {
                mp.start();
                binding.seekBar.setMax(mp.getDuration());
                setStatus("Streaming video");
            });
            binding.videoView.setOnErrorListener((mp, what, extra) -> {
                setStatus("Error loading video");
                return true;
            });
            binding.videoView.requestFocus();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Stop and release the MediaPlayer safely */
    private void stopAudio() {
        handler.removeCallbacks(seekRunnable);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding.seekBar.setProgress(0);
    }

    /** Allow user to scrub through media using SeekBar */
    private void setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (isAudioMode && mediaPlayer != null) mediaPlayer.seekTo(progress);
                    else binding.videoView.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setStatus(String msg) {
        binding.tvStatus.setText("Status: " + msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
        handler.removeCallbacks(seekRunnable);
    }
}