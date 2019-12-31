package com.basis.porterosmart;

import android.util.Log;
import org.videolan.libvlc.MediaPlayer;
import java.lang.ref.WeakReference;

class MyPlayerListener implements MediaPlayer.EventListener {

    private static String TAG = "PlayerListener";
    private WeakReference<VideoActivity3> mOwner;


    public MyPlayerListener(VideoActivity3 owner) {
        mOwner = new WeakReference<VideoActivity3>(owner);
    }

    @Override
    public void onEvent(MediaPlayer.Event event) {
        VideoActivity3 player = mOwner.get();

        switch(event.type) {
            case MediaPlayer.Event.EndReached:
                Log.d(TAG, "MediaPlayerEndReached");
                player.releasePlayer();
                break;
            case MediaPlayer.Event.Playing:
            case MediaPlayer.Event.Paused:
            case MediaPlayer.Event.Stopped:
            default:
                break;
        }
    }
}
