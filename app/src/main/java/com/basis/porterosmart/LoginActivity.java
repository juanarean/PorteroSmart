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
import com.basis.porterosmart.Common.MyApp;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText Usuario;
    private EditText Password;
    private EditText Nserie;
    private Button btnLogin;
    private TextInputLayout til1;
    private TextInputLayout til2;
    private TextInputLayout til3;
    TextView Revision;
    private static final String UrlLogin = "http://35.166.19.153/Login.php";
    private String timbre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Revision = findViewById(R.id.tvRev);
        Revision.setText("1.3.0");

        Usuario = findViewById(R.id.tvUsuario);
        Password =findViewById(R.id.tvPassword);
        Nserie = findViewById(R.id.tvNserie);
        btnLogin = findViewById(R.id.btnLogin);

        til1 = findViewById(R.id.textInputLayout);
        til2 = findViewById(R.id.textInputLayout2);
        til3 = findViewById(R.id.textInputLayout3);

        // Abro la memoria de preferencias. Si hay un usuario ya guardado, cargo los datos del usuario y hago el login directo
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.getString("usuario","default").equals("default")) {
            Usuario.setText(sharedPreferences.getString("usuario", "default"));
            Password.setText(sharedPreferences.getString("password", "default"));
            Nserie.setText(sharedPreferences.getString("nserie", "default"));

            til1.setVisibility(View.GONE);
            til2.setVisibility(View.GONE);
            til3.setVisibility(View.GONE);
            Usuario.setVisibility(View.GONE);
            Password.setVisibility(View.GONE);
            Nserie.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);

            login();
        }

        // Si no hay usuario pido las credenciales.

        til1.setVisibility(View.VISIBLE);
        til2.setVisibility(View.VISIBLE);
        til3.setVisibility(View.VISIBLE);
        Usuario.setVisibility(View.VISIBLE);
        Password.setVisibility(View.VISIBLE);
        Nserie.setVisibility(View.VISIBLE);
        btnLogin.setVisibility(View.VISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }

        });

    }

    public void login () {
        // Instancia de la request. Libreria Volley para hacer una peticion POST HTTP con las credenciales.
        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UrlLogin,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (!response.equals("Usuario no encontrado")) {
                            timbre = response;
                            // Si la respuesta es positiva, guardo las credenciales en la SharedMemory.
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("usuario", Usuario.getText().toString());
                            editor.putString("password", Password.getText().toString());
                            editor.putString("nserie", Nserie.getText().toString());
                            editor.putString("timbre", timbre);
                            editor.apply();

                            // Si hay respuesta lanzo el servicio MQTT y luego voy al video.
                            Intent jobServiceIntent = new Intent(MyApp.getContext(), MyService.class);
                            MyService.enqueueWork(MyApp.getContext(),jobServiceIntent);

                            Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Si no se encuentran las credenciales en la base de datos.
                            Toast.makeText(getApplicationContext(),"Error de Login! Revise sus credenciales.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Error en la respuesta.
                Toast.makeText(LoginActivity.this, "Error en servidor!" + error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // Aca se parsea el body del POST.
                Map<String, String> params = new HashMap<>();
                params.put("Usuario", Usuario.getText().toString());
                params.put("Contrasena", Password.getText().toString());
                params.put("NumeSerie", Nserie.getText().toString());
                return params;
            }
        };

        //Aca agrego la request, si no, Volley no la manda.
        queue.add(stringRequest);


    }

}
