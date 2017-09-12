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
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.softdev.weekimessenger.Activity.Login;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GCMService extends FirebaseInstanceIdService
{
    private String refreshedToken;
    private static final String TAG = GCMService.class.getSimpleName();
//    public GCMService()
//    {
//        super(TAG);
//    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        registerGCM(refreshedToken);
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        // Registering new GCM ID.
//        registerGCM();
//        Log.w("GCM", "started");
//    }

    private void registerGCM(String token) {
        try
        {
//             = FirebaseInstanceId.getInstance().getToken();
//            if (refreshedToken==null){
//                refreshedToken = FirebaseInstanceId.getInstance().getToken();
//            }
            Log.w(TAG, "Refreshed token: " + token);
            UpdateUserId(token);
            AppHandler.getInstance().getDataManager().setInt("gcmUpdate", 1);
        }
        catch (Exception ex) {
            Log.w(TAG, "Failed to complete token refresh", ex);
            AppHandler.getInstance().getDataManager().setInt("gcmUpdate", 0);
        }
        Intent registrationComplete = new Intent(Config.GCM_UPDATED);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void UpdateUserId(final String token) {

        Log.w("ANYING", token);
        String SELF = AppHandler.getInstance().getDataManager().getString("user", null);
        if (SELF == null)
        {
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        System.out.println("EEQ ini GCM Service 1");
        StringRequest request = new StringRequest(Request.Method.PUT, Config.GCM_UPDATE, new Response.Listener<String>() {
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

        System.out.println("EEQ Volley add to request");
        AppHandler.getInstance().addToRequestQueue(request);
    }
}
