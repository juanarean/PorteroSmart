package com.basis.porterosmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText Usuario;
    private EditText Password;
    private Button btnLogin;
    TextView Revision;
    private static final String UrlLogin = "http://";
    private static String UrlCamara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Revision = findViewById(R.id.tvRev);
        Revision.setText("1.1.1");

        Usuario = findViewById(R.id.tvUsuario);
        Password =findViewById(R.id.tvPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Instancia de la request
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, UrlLogin,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("usuario", Usuario.getText().toString());
                                editor.putString("password", Password.getText().toString());
                                editor.apply();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Error en Login!" + error.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("usuario", Usuario.getText().toString());
                        params.put("password", Password.getText().toString());
                        return params;
                    }
                };

                //Aca agrego la request, si no, no la manda.
                queue.add(stringRequest);

                //En un login correcto activo el servicio (una vez que se mis datos)
                //startService(new Intent(LoginActivity.this, MyService.class));

                //Paso a la siguiente activity
                /*String movieurl = "rtsp://admin:proyecto@200.125.80.16:554/11";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieurl));
                startActivity(intent);*/
                Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
                startActivity(intent);
                finish();

            }
        });


    }


}
