package com.softdev.weekimessenger.Services;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.softdev.weekimessenger.Configuration.Config;
import com.softdev.weekimessenger.Handlers.AppHandler;
import java.util.HashMap;
import java.util.Map;
public class WebService {

    public void updateMessageDelivery(final String group_id, final String msg_id) {
        StringRequest request = new StringRequest(Request.Method.PUT, Config.DELIVERY_UPDATE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                Log.d("WebService", "ErrorResponse: " + error.getMessage() + ", Code: " + networkResponse);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("msg_id", msg_id);
                params.put("group_id", group_id);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };

        AppHandler.getInstance().addToRequestQueue(request);
    }

    public void RetrieveMessages()
    {
        StringRequest request = new StringRequest(Request.Method.GET, Config.CONVERSATIONS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Inbox", "err: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return AppHandler.getInstance().getAuthorization();
            }
        };
        AppHandler.getInstance().addToRequestQueue(request);
    }
}