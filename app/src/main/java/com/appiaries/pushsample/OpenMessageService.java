package com.appiaries.pushsample;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABDevice;
import com.appiaries.baas.sdk.ABException;
import com.appiaries.baas.sdk.ABPushMessage;
import com.appiaries.baas.sdk.ABResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Async sending opened-message request.
 * Pass "pushId" (Config.KEY_PUSH_ID）via Intent.
 */
public class OpenMessageService extends IntentService {

    /**
     * constructor
     */
    public OpenMessageService() {
        super("OpenMessageService");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("openMessageの開始");

        // Appiaries init
        AB.Config.setDatastoreID(Config.DATASTORE_ID);
        AB.Config.setApplicationID(Config.APPLICATION_ID);
        AB.Config.setApplicationToken(Config.APPLICATION_TOKEN);
        AB.Config.Push.setSenderID(Config.SENDER_ID);
        // Prepare for the Opened-Message event.
        AB.Config.Push.setOpenMessage(true);
        AB.activate(this);

        try {
            // Obtaining Registration ID
            final SharedPreferences preferences = getSharedPreferences(Config.GCM_PREFERENCE, Context.MODE_PRIVATE);
            final String regId = preferences.getString(Config.PROPERTY_REG_ID, "");
            final long pushId = Long.parseLong(intent.getStringExtra(Config.KEY_PUSH_ID));
            // Register "Reigstration ID" to ABDevice.
            final ABDevice device = new ABDevice();
            device.setID(regId);
            // Setting device information to ABPushMessage.
            final Map<String, Object> map = new HashMap<>();
            map.put("device", device);
            final ABPushMessage pushMessage = new ABPushMessage();
            pushMessage.setDevice(device);
            pushMessage.setPushId(pushId);
            // Request Opened-Message and retrieve the result.
            final ABResult<Void> result = AB.PushService.openMessageSynchronously(pushMessage);
            System.out.println("pushId:" + pushId);
            System.out.println("regId:" + regId);
            System.out.println(result.getCode());
        } catch (ABException e) {
            System.out.println("error:" + e);
        }
    }
}
