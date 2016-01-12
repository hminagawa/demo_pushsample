/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appiaries.meetfriend.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABDevice;
import com.example.appiaries.meetfriend.Config;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * PUSH通知のために端末の登録を行うためのFragmentです。<br/>
 * <p/>
 * このプログラムはキャンセル処理、エラー処理の省略やログ表示を行っています<br/>
 * 業務等で使用する際にはご注意ください。<br/>
 * 実装は以下の内容に従っていますので、合わせてご参照ください。<br/>
 * {@see <a href="https://code.google.com/p/gcm/source/browse/gcm-client/GcmClient/src/main/java/com/google/android/gcm/demo/app/DemoActivity.java">URL</a>}
 */
public class PushRegistrationFragment extends Fragment {

    /**
     * Registration IDのキー
     */
    public static final String PROPERTY_REG_ID = "registration_id";
    /**
     * アプリバージョン用のキー
     */
    private static final String PROPERTY_APP_VERSION = "appVersion";
    /**
     * Google Play servicesの復帰用のリクエストID
     */
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * SENDER ID(プロジェクト毎に異なります)
     */
    private String SENDER_ID = "912379580812";

    /**
     * ログ用のタグ
     */
    private static final String TAG = "AppiariesReg";

    /**
     * {@link GoogleCloudMessaging}
     */
    private GoogleCloudMessaging mGcm;

    /**
     * Registration ID
     */
    private String mRegId;

    /**
     * ApplicationContext
     */
    private Context mApplicationContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context context = getActivity();
        mApplicationContext = context.getApplicationContext();

        // Google Play servicesが有効かチェック
        if (checkPlayServices()) {
            mGcm = GoogleCloudMessaging.getInstance(context);
            mRegId = getRegistrationId(mApplicationContext);

            if (TextUtils.isEmpty(mRegId)) {
                registerInBackground();
            } else {
                Log.i(TAG, "regId is " + mRegId);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Google Play servicesの状態をチェックします。
     *
     * @return 有効な場合はtrue.
     */
    private boolean checkPlayServices() {
        final Activity activity = getActivity();
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registration IDを保存します。
     *
     * @param context {@link Context}
     * @param regId   Registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    /**
     * Registration IDを取得します。
     *
     * @param context {@link Context}
     */
    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * バックグラウンドで登録します。
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mApplicationContext);
                    }
                    mRegId = mGcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + mRegId;
                    // AppiariesサーバにRegistration IDを登録
                    sendRegistrationIdToBackend();
                    // Registration IDを登録
                    storeRegistrationId(mApplicationContext, mRegId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                } catch (Exception ex) {
                    msg = "Error :" + ex;
                }
                return msg;
            }

            /**
             * 処理終了後にメッセージを表示します。
             */
            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg);

                // トーストの表示
                final Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    /**
     * アプリバージョンを取得します。
     *
     * @param context {@link Context}
     * @return アプリのバージョン
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * GCMのプリファレンスを取得します。
     *
     * @param context {@link Context}
     * @return {@link Preference}
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences("MeetFriend", Context.MODE_PRIVATE);
    }

    /**
     * ID登録をバックグラウンドで行います。
     *
     * @throws Exception
     */
    private void sendRegistrationIdToBackend() throws Exception {
        // TODO:以下の行は削除し、ここに追加していきましょう
        //throw new Exception("PUSH通知はまだ完成していません");
        // データストアID、アプリID、アプリケーショントークン、Contextを指定
        AB.Config.setDatastoreID(Config.DATA_STORE_ID);
        AB.Config.setApplicationID(Config.APP_ID);
        AB.Config.setApplicationToken(Config.APP_TOKEN);
        AB.activate(mApplicationContext);

        // Registration IDの登録
        ABDevice device = new ABDevice();
        device.setRegistrationID(mRegId);
        device.register();
    }

}
