package com.softdev.weekimessenger.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import com.softdev.weekimessenger.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Signup extends AppCompatActivity {

    EditText txtUsername;
    EditText txtName;
    EditText txtEmail;
    EditText txtPassword;
    Button btnRegister;
    TextView btnLogin;
    CircleImageView profileImage;
    Bitmap bitmap;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtUsername = (EditText) findViewById(R.id.username_text_field);
        txtPassword = (EditText) findViewById(R.id.password_text_field);
        txtEmail = (EditText) findViewById(R.id.email_text_field);
        txtName = (EditText) findViewById(R.id.name_text_field);
        profileImage = (CircleImageView) findViewById(R.id.image);
        btnRegister = (Button) findViewById(R.id.register_button);
        btnLogin = (TextView) findViewById(R.id.signin_button);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUsername = txtUsername.getText().toString().trim();
                String strEmail = txtEmail.getText().toString().trim();
                String strName = txtName.getText().toString();
                String strPassword = txtPassword.getText().toString();
                txtUsername.setError(null);
                txtEmail.setError(null);
                txtName.setError(null);
                txtPassword.setError(null);
                if (strName.trim().isEmpty()) {
                    txtName.setError("Profile name field cannot be empty.");
                    return;
                } else if (strName.length() > 30) {
                    txtName.setError("Profile name is too long.");
                    return;
                } else if (strEmail.isEmpty()) {
                    txtEmail.setError("Email field cannot be empty.");
                    return;
                } else  if (strUsername.isEmpty()) {
                    txtUsername.setError("Username field cannot be empty.");
                    return;
                } else if (strPassword.trim().isEmpty()) {
                    txtPassword.setError("Password field cannot be empty.");
                    return;
                } else if (strPassword.length() < 8) {
                    txtPassword.setError("Password must be 8 characters long.");
                    return;
                }
                progressDialog = new ProgressDialog(Signup.this);
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Registering your account...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                Log.d("SignUP", getStringImage(bitmap));
                Register(strUsername, strEmail, strPassword, strName);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Signup.this, Login.class));
                finish();
            }
        });
    }

    private void Register(final String username, final String email, final String password, final String name) {
        StringRequest request = new StringRequest(Request.Method.POST, Config.REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    Log.d("Register", response);
                    if (!obj.getBoolean("error")) {
                        // Registration successful.
                        JSONObject accountObj = obj.getJSONObject("user");
                        // Storing user account information.
                        AppHandler.getInstance().getDataManager().setString("user", accountObj.getString("user"));
                        AppHandler.getInstance().getDataManager().setString("username", accountObj.getString("username"));
                        AppHandler.getInstance().getDataManager().setString("email", accountObj.getString("email"));
                        AppHandler.getInstance().getDataManager().setString("name", accountObj.getString("name"));
                        AppHandler.getInstance().getDataManager().setString("created_At", accountObj.getString("created_At"));
                        AppHandler.getInstance().getDataManager().setString("icon", accountObj.getString("icon"));
                        AppHandler.getInstance().getDataManager().setString("status", accountObj.getString("status"));
                        AppHandler.getInstance().getDataManager().setString("api", obj.getString("api"));
                        // Starting messaging activity.
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        progressDialog.dismiss();
                        finish();
                    } else {
                        int responseCode = obj.getInt("code");
                        if (responseCode == Config.USER_ALREADY_EXISTS) {
                            progressDialog.dismiss();
                            Toast.makeText(Signup.this, "Username or email already exists. Try to login with this email or register with a different one.", Toast.LENGTH_SHORT).show();
                        } else if (responseCode == Config.UNKNOWN_ERROR) {
                            progressDialog.dismiss();
                            Toast.makeText(Signup.this, "Server returning unknown error.", Toast.LENGTH_SHORT).show();
                        } else if (responseCode == Config.EMAIL_INVALID) {
                            progressDialog.dismiss();
                            txtEmail.setError("Email is not valid.");
                        }
                    }
                } catch (JSONException ex) {
                    progressDialog.dismiss();
                    Toast.makeText(Signup.this, "Server returning unknown error.", Toast.LENGTH_SHORT).show();
                    Log.e("SignUp", "JSONException: " + ex.getMessage() + "\nResponse: " + response);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(Signup.this, "Unable to connect to server. "+ error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("SignUp", "Mantap "+error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                params.put("name", name);
                params.put("icon", getStringImage(bitmap));
                return params;
            }
        };
        int socketTimeout = 0;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        AppHandler.getInstance().addToRequestQueue(request);
    }

    public String getStringImage(Bitmap bmp) {
        if (bmp == null)
            return "null";

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        byte[] imageBytes = output.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException ex) {
                Log.d("SignUp", "Exception: " + ex.getMessage());
            }
        }
    }
}
