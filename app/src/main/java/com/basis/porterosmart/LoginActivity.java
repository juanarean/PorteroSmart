package com.basis.porterosmart;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText Usuario;
    private EditText Password;
    private EditText Nserie;
    private Button btnLogin;
    TextView Revision;
    private static final String UrlLogin = "http://35.166.19.153/Login.php";
    private static String UrlCamara;
    private String topico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Revision = findViewById(R.id.tvRev);
        Revision.setText("1.2.1");

        Usuario = findViewById(R.id.tvUsuario);
        Password =findViewById(R.id.tvPassword);
        Nserie = findViewById(R.id.tvNserie);
        btnLogin = findViewById(R.id.btnLogin);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!sharedPreferences.getString("usuario","default").equals("default")) {
            Usuario.setText(sharedPreferences.getString("usuario", "default"));
            Password.setText(sharedPreferences.getString("password", "default"));
            Nserie.setText(sharedPreferences.getString("nserie", "default"));
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Instancia de la request
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, UrlLogin,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                if (!response.equals("Usuario no encontrado")) {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        topico = json.getJSONObject("TopicoSuscribir").toString();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("usuario", Usuario.getText().toString());
                                    editor.putString("password", Password.getText().toString());
                                    editor.putString("nserie", Nserie.getText().toString());
                                    editor.putString("topico", topico);
                                    editor.apply();
                                    // si hay respuesta lazo el servicio y luego voy al video
                                    Intent jobServiceIntent = new Intent(MyApp.getContext(), MyService.class);
                                    MyService.enqueueWork(MyApp.getContext(),jobServiceIntent);
                                    //TareaAsync tareaAsync = new TareaAsync();
                                    //tareaAsync.execute();
                                    //startService(new Intent(getApplicationContext(),MyService2.class));

                                    //Broadcasts receivers
                                    //BroadcastReceiver br = new MyReceiver();
                                    //IntentFilter filter = new IntentFilter();
                                    //filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                                    //this.registerReceiver(br, filter);

                                    Intent intent = new Intent(LoginActivity.this, VideoActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),"Error de Login!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Error en servidor!" + error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Usuario", Usuario.getText().toString());
                        params.put("Contrasena", Password.getText().toString());
                        params.put("NumeSerie", Nserie.getText().toString());
                        return params;
                    }
                };

                //Aca agrego la request, si no, no la manda.
                queue.add(stringRequest);


            }
        });


    }


}
