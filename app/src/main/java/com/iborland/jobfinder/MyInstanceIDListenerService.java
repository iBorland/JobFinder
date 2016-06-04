package com.iborland.jobfinder;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by iBorland on 02.05.2016.
 */

/*
Этот класс нужен для работы Google Cloud Message. Здесь обновляется пользовательский токен GCM (InstanceID).
 */

public class MyInstanceIDListenerService extends InstanceIDListenerService {


    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

}
