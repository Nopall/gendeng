package com.softdev.weekimessenger.Services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

    // Raises when GCM ID is changed.
    @Override
    public void onTokenRefresh() {
        String refreshToken = FirebaseInstanceId.getInstance().getToken();
        Log.v("ANYING", "Okeh"+refreshToken);
        SharedPreferences.Editor editor = getSharedPreferences("tokenid", MODE_PRIVATE).edit();
        editor.putString("token", refreshToken);
        editor.apply();
        Intent intent = new Intent(this, GCMService.class);
        intent.putExtra("refreshtokenid", refreshToken);
        startService(intent);
    }


}
