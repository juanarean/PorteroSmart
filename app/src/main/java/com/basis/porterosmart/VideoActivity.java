package com.basis.porterosmart;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    VideoView videoView;
    MediaPlayer mediaPlayer;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    final String videoSrc = "rtsp://admin:proyecto@200.125.80.16/11";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        /*videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        videoView.setVideoURI(Uri.parse(videoUrl));

        videoView.requestFocus();

        videoView.start();*/
        surfaceView = findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("AppTAG", "MEDIA player Prepared");
        mediaPlayer.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Map<String, String> headers = getHeathers();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
        try {
            mediaPlayer.setDataSource(videoSrc);
            //mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
            //mediaPlayer.prepareAsync();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        } catch (IllegalArgumentException e) {
            Toast.makeText(VideoActivity.this,"Argumento ilegal",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (SecurityException e) {
            Toast.makeText(VideoActivity.this,"Seguridad",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Toast.makeText(VideoActivity.this,"Estado ilegal",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(VideoActivity.this,"IO",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private Map<String, String> getHeathers() {
        Map<String, String> heather = new HashMap<>();
        String describe = "DESCRIBE " + "rtsp://200.125.80.16/" + " RTSP/1.0";
        String accept = "application/sdp";
        String basicAuthValue = getBasicAuthValue("admin","proyecto");
        heather.put("Authorization", basicAuthValue);
        heather.put("Request", describe);
        heather.put("Accept", accept);
        return heather;
    }

    private String getBasicAuthValue(String usr, String pwd) {
        String credentials = usr + ":" + pwd;
        int flags = Base64.URL_SAFE|Base64.NO_WRAP;
        byte[] bytes = credentials.getBytes();
        return "Basic " + Base64.encodeToString(bytes, flags);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mediaPlayer.release();
    }
}
