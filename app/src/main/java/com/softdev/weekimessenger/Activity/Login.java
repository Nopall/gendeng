package com.softdev.weekimessenger.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.R;
import com.softdev.weekimessenger.Services.GCMService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private static final String TAG = GCMService.class.getSimpleName();

    private EditText txtUsername;
    private EditText txtPassword;
    private Button btnLogin;
    private TextView btnRegister;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtUsername = (EditText) findViewById(R.id.username_text_field);
        txtPassword = (EditText) findViewById(R.id.password_text_field);
        btnLogin = (Button) findViewById(R.id.signin_button);
        btnRegister = (TextView) findViewById(R.id.signup_button);
        pDialog = new ProgressDialog(Login.this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("Signing In ...");
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtUsername.setEnabled(false);
                txtPassword.setEnabled(false);
                if (txtUsername.getText().toString().trim().isEmpty()) {
                    txtUsername.setEnabled(true);
                    txtPassword.setEnabled(true);
                    txtUsername.setError("Username field cannot be empty.");
                } else if (txtPassword.getText().toString().trim().isEmpty()) {
                    txtUsername.setEnabled(true);
                    txtPassword.setEnabled(true);
                    txtPassword.setError("Password field cannot be empty.");
                } else {
                    txtUsername.setError(null);
                    txtPassword.setError(null);
                    pDialog.show();
                    Login(txtUsername.getText().toString(), txtPassword.getText().toString());
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Signup.class));
                finish();
            }
        });
    }

    private void Login(final String username, final String password) {
        final String token = FirebaseInstanceId.getInstance().getToken();
        Log.w("TOKENOI", token);
        StringRequest request = new StringRequest(Request.Method.GET, Config.LOGIN + "?username=" + username + "&password=" + password, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error")) {
                        JSONObject accountObj = obj.getJSONObject("account");
                        AppHandler.getInstance().getDataManager().setString("user", accountObj.getString("user"));
                        AppHandler.getInstance().getDataManager().setString("username", accountObj.getString("username"));
                        AppHandler.getInstance().getDataManager().setString("email", accountObj.getString("email"));
                        AppHandler.getInstance().getDataManager().setString("name", accountObj.getString("name"));
                        AppHandler.getInstance().getDataManager().setString("icon", accountObj.getString("icon"));
                        AppHandler.getInstance().getDataManager().setString("status", accountObj.getString("status"));
                        AppHandler.getInstance().getDataManager().setString("api", obj.getString("api"));
                        AppHandler.getInstance().getDataManager().setString("created_At", accountObj.getString("created_At"));
                        AppHandler.getInstance().getDataManager().setInt("pause_notification", 0);

                        StringRequest request1 = new StringRequest(Request.Method.PUT, Config.GCM_UPDATE, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                System.out.println("EEQ onResponse volley-nya");
                                try {
                                    JSONObject obj = new JSONObject(response);
                                    if (!obj.getBoolean("error"))
                                    {
                                        Log.w("GCMService", "Updated.");
                                    }
                                    else {
                                        Log.w("GCMService", obj.getString("code"));
                                    }

                                } catch (JSONException e) {
                                    Log.w(TAG, "JSONException: " + e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println("EEQ di dalem error volley-nya");
                                NetworkResponse networkResponse = error.networkResponse;
                                Log.w(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);

                                if (networkResponse != null) {
                                    System.out.println("EEQ di dalam error : " + networkResponse.statusCode);
                                    System.out.println("EEQ di dalam error : " + new String(networkResponse.data, 0, networkResponse.data.length));
                                }
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                System.out.println("EEQ lumba2");
                                Map<String, String> params = new HashMap<>();
                                params.put("gcm", token);
//                params.put("gcm", "asu");
                                return params;
                            }

                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                System.out.println("EEQ lumba2 3");
                                return AppHandler.getInstance().getAuthorization();
                            }
                        };

                        AppHandler.getInstance().addToRequestQueue(request1);

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        int responseCode = obj.getInt("code");
                        if (responseCode == Config.PASSWORD_INCORRECT) {
                            txtUsername.setEnabled(true);
                            txtPassword.setEnabled(true);
                            pDialog.dismiss();
                            showError("Password isn't valid.");
                        } else if (responseCode == Config.USER_INVALID) {
                            txtUsername.setEnabled(true);
                            txtPassword.setEnabled(true);
                            pDialog.dismiss();
                            showError("Username or password isn't valid.");
                        }
                        else if (responseCode == Config.ACCOUNT_DISABLED) {
                            txtUsername.setEnabled(true);
                            txtPassword.setEnabled(true);
                            pDialog.dismiss();
                            showError("Account is currently disabled.");
                        }
                        else {
                            txtUsername.setEnabled(true);
                            txtPassword.setEnabled(true);
                            pDialog.dismiss();
                            showError("Unknown error while signing in.");
                        }
                    }
                } catch (JSONException ex) {
                    txtUsername.setEnabled(true);
                    txtPassword.setEnabled(true);
                    pDialog.dismiss();
                    showError("There was an error while parsing data.");
                }
            }
        }, new Response.ErrorListener() // Response error.
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                pDialog.dismiss();
                showError(error.getMessage());
            }
        });
        AppHandler.getInstance().addToRequestQueue(request);
    }

    // Show error on Snack Bar.
    public void showError(String error) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_content), error, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
