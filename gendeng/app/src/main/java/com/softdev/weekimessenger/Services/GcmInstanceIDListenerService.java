package com.softdev.weekimessenger.Services;

import android.content.Intent;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class GcmInstanceIDListenerService extends FirebaseInstanceIdService {

    // Raises when GCM ID is changed.
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GCMService.class);
        startService(intent);
    }


}
