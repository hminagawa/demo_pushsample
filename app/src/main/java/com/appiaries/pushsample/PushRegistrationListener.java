package com.appiaries.pushsample;

/** Callback when Push Notification registration is success. */
public interface PushRegistrationListener {

    /** For success */
    void onSuccess();

    /** For failure */
    void onFailure();

}
