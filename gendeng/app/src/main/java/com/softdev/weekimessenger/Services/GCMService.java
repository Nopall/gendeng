package com.softdev.weekimessenger.Services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.softdev.weekimessenger.Activity.Login;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GCMService extends IntentService
{
    private static final String TAG = GCMService.class.getSimpleName();
    public GCMService()
    {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Registering new GCM ID.
        registerGCM();
        Log.d("GCM", "started");
    }

    private void registerGCM() {
        String refreshedToken = null;
        try
        {
            refreshedToken = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "Refreshed token: " + refreshedToken);
            UpdateUserId(refreshedToken);
            AppHandler.getInstance().getDataManager().setInt("gcmUpdate", 1);
        }
        catch (Exception ex) {
            Log.d(TAG, "Failed to complete token refresh", ex);
            AppHandler.getInstance().getDataManager().setInt("gcmUpdate", 0);
        }
        Intent registrationComplete = new Intent(Config.GCM_UPDATED);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void UpdateUserId(final String token) {

        String SELF = AppHandler.getInstance().getDataManager().getString("user", null);
        if (SELF == null)
        {
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        StringRequest request = new StringRequest(Request.Method.PUT, Config.GCM_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (!obj.getBoolean("error"))
                    {
                        Log.d("GCMService", "Updated.");
                    }
                    else {
                        Log.d("GCMService", obj.getString("code"));
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.d(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("gcm", token);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };

        AppHandler.getInstance().addToRequestQueue(request);
    }
}
