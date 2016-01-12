package com.appiaries.pushsample;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/** Simple splash activity. */
public class SplashActivity extends AppCompatActivity implements PushRegistrationListener {

    /** Flag if splash job is done. */
    private boolean mIsSplashEnd;

    /** Flag registered in GCM. */
    private boolean mIsGcmRegistered;

    /**
     * CountDownTimer
     */
    private CountDownTimer mTimer;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mIsSplashEnd = false;
        mIsGcmRegistered = false;

        // Begin GCM jobs.
        if (savedInstanceState == null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(new PushRegistrationFragment(), "registration").commit();
        }

        mTimer = new CountDownTimer(Config.SPLASH_TIME_MILLIS, Config.SPLASH_TIME_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                // do nothing
            }

            @Override
            public void onFinish() {
                mIsSplashEnd = true;
                startNextActivity();
            }
        }.start();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        mIsSplashEnd = false;
        mTimer.cancel();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess() {
        mIsGcmRegistered = true;
        startNextActivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFailure() {
        mIsGcmRegistered = false;
        Toast.makeText(this, R.string.error_splash, Toast.LENGTH_LONG).show();
    }

    /** Off to the next view! */
    private void startNextActivity() {
        if (mIsGcmRegistered && mIsSplashEnd) {
            final Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
