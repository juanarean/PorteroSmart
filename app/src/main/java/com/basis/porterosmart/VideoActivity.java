package com.basis.porterosmart;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.basis.porterosmart.Common.MyApp;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.net.*;
import java.util.ArrayList;

/**
Esta Activity utiliza la libreria VLC ubicada en el repositorio: 'de.mrmaffen:libvlc-android:2.1.12@aar' e implementa IVLCVout.Callback
 con lo cual utiliza los métodos de dicha Interface.
 En el onCreate se declaran la SurfaceView donde se reproducirá el video, se crea el objeto libvlc y se configura.
 Se utiliza la libreria MediaPlayer y se la carga con la libreria VLC. Luego el player creado con la MediaLibrary nos da la recepción del video que se configuró previamente.

 */
public class VideoActivity extends AppCompatActivity implements IVLCVout.Callback{

    public final static String TAG = "VideoActivity";
    public String ddns;
    public static final String RTSP_LOGIN = "rtsp://admin:proyecto@";
    public static final String RTSP_PORT = ".ddns.net:554/11";

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;
    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(VideoActivity.this);

    // AudioStreamer RSTP
    private static AudioStream myAudioStream;
    private static AudioGroup myAudioGroup;
    private static AudioManager myAudioManager;

    private TextView volumenTextView;

    private String myAddress;
    TextView IPtv;

    private enum StreamerState {
        STREAMER_STATE_IDLE,
        STREAMER_STATE_STREAMING,
        STREAMER_STATE_STOPPED
    }
    private static StreamerState streamAudioState = StreamerState.STREAMER_STATE_IDLE;

    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        volumenTextView = findViewById(R.id.tvVolumen);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ddns = sharedPreferences.getString("nserie","default");
        if(ddns.equals("default")){
            Toast.makeText(this,"Error en el DDNS",Toast.LENGTH_LONG).show();
        }

        // Get URL
        Log.d(TAG, "Playing back " + RTSP_LOGIN + ddns + RTSP_PORT);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;

        mSurface = findViewById(R.id.surfaceView);
        holder = mSurface.getHolder();

        ArrayList<String> options = new ArrayList<String>();
        options.add("--aout=opensles");
        options.add("--audio-time-stretch"); // time stretching
        options.add("-vvv"); // verbosity
        options.add("--aout=opensles");
        options.add("--avcodec-codec=h264");
        options.add("--file-logging");
        options.add("--logfile=vlc-log.txt");


        libvlc = new LibVLC(this, options);
        holder.setKeepScreenOn(true);

        // Create media player
        mMediaPlayer = new MediaPlayer(libvlc);
        mMediaPlayer.setEventListener(mPlayerListener);

        // --------------------  Audio Stream ------------------------------------

        /** Se intento utilizar la libreria AudioManager para stremear audio ya que se puede configurar como PCM que es el protocolo RTP definido por la cámara.
         * pero no se logró conectar con el puerto correcto de la cámara.
         * Debemos hacerlo con hardware extra.
         */
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        myAddress = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());

        IPtv = findViewById(R.id.tvIP);
        IPtv.setText(myAddress);

        // Boton para audio
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(streamAudioState == StreamerState.STREAMER_STATE_STREAMING) {
                    StopAudioStream();
                } else {
                    StartAudioStream();
                }
            }
        });
    }

    private void StartAudioStream() {
        int volumen;
        int port = 1234;

        if (myAudioStream != null) {
            return;
        }
        java.net.InetAddress address;
        try {
            address = InetAddress.getByName("192.168.0.141");
        } catch (UnknownHostException | NetworkOnMainThreadException e) {
            Toast.makeText(this, "Problema cargando la IP. stream de audio", Toast.LENGTH_LONG).show();
            return;
        }

        myAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        myAudioGroup = new AudioGroup();
        myAudioGroup.setMode(RtpStream.MODE_SEND_ONLY);

        try {
            myAudioStream = new AudioStream(InetAddress.getByName(myAddress));
        } catch (SocketException | UnknownHostException ex) {
            Log.d("Error", "Cannot create audio stream");
        }

        myAudioStream.setCodec(AudioCodec.PCMA);

        myAudioStream.setMode(RtpStream.MODE_SEND_ONLY);

        myAudioStream.associate(address, port);
        myAudioStream.join(myAudioGroup);

        myAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        myAudioManager.setSpeakerphoneOn(true);
        myAudioManager.setMicrophoneMute(false);
        volumen = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);


        volumenTextView.setText(String.valueOf(volumen));

        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
        floatingActionButton.setImageResource(R.drawable.ic_mic_white_48dp);
        updateState(StreamerState.STREAMER_STATE_STREAMING);
    }

    private void StopAudioStream() {
        if (myAudioStream != null) {
            myAudioStream.join(null);
            myAudioGroup.setMode(AudioGroup.MODE_ON_HOLD);
            myAudioStream.release();
            myAudioStream = null;
            myAudioGroup = null;

            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
            floatingActionButton.setImageResource(R.drawable.ic_mic_off_black_24dp);
            updateState(StreamerState.STREAMER_STATE_STOPPED);
        }
    }

    private void updateState(StreamerState state) {
        streamAudioState = state;
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Set up video output, ORIGINALMENTE ESTABA EN EL ONCREATE PERO NO RESUMIA DESPUES DE MINIMIZAR APP.
        // Aca se enlaza el SurfaceView con la salida del VLC
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setVideoView(mSurface);
        vout.setWindowSize(mVideoWidth,mVideoHeight);
        vout.addCallback(this);
        vout.attachViews();

        Media m = new Media(libvlc, Uri.parse(RTSP_LOGIN + ddns + RTSP_PORT));

        mMediaPlayer.setMedia(m);
        mMediaPlayer.play();

        // matener el boton de audio actualizado
        if(streamAudioState == StreamerState.STREAMER_STATE_STREAMING) {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("green")));
            floatingActionButton.setImageResource(R.drawable.ic_mic_white_48dp);
        } else {
            floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("red")));
            floatingActionButton.setImageResource(R.drawable.ic_mic_off_black_24dp);
        }
    }

    // Al sacar la app del primer plano pausa el video para no consumir recursos.
    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayer.stop();
    }

    // Al cerrar la al devuelvo los recursos tomados... el sistema operativo se puede encargar de esto también
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }



    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    public void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    // Creacion y opciones de la Toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // Opcion de la Toolbar de cerrar sesión
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(VideoActivity.this);
                alertBuilder.setMessage("Desea cerrar sesión?")
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(VideoActivity.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("usuario", "default");
                                editor.putString("password", "default");
                                editor.putString("nserie", "default");
                                editor.putString("topico", "default");
                                editor.apply();

                                Intent intent = new Intent(VideoActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });

                AlertDialog alerta = alertBuilder.create();
                alerta.show();

                return true;
    }


}
