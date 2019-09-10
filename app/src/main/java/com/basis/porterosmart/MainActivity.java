package com.basis.porterosmart;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener {

    TextView Revision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Revision = findViewById(R.id.tvRev);
        Revision.setText("1.0.0");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
