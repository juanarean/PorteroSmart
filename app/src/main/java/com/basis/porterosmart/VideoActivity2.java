package com.basis.porterosmart;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.VideoView;

import java.util.HashMap;
import java.util.Map;

public class VideoActivity2 extends AppCompatActivity {

    VideoView videoView;
    final String videoSrc = "rtsp://200.125.80.16/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);

        Map<String, String> headers = getHeathers();

        videoView = findViewById(R.id.videoView);

        videoView.setVideoURI(Uri.parse(videoSrc),headers);

        videoView.requestFocus();

        videoView.start();
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
}
