/*
 * Copyright 2013 Team EGG. Co.ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.appiaries.meetfriend;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appiaries.baas.sdk.AB;
import com.appiaries.baas.sdk.ABDBObject;
import com.appiaries.baas.sdk.ABException;
import com.appiaries.baas.sdk.ABResult;
import com.appiaries.baas.sdk.ResultCallback;
import com.example.appiaries.meetfriend.fragment.PushRegistrationFragment;
import com.example.appiaries.meetfriend.util.AddressSearchUtils;
import com.example.appiaries.meetfriend.util.Installation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 地図から地点を選択する画面です。
 *
 * @author yoshihide-sogawa
 */
public class PickPlaceMapActivity extends AppCompatActivity implements GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    /**
     * 接続失敗のリクエストコード
     */
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /**
     * Google Play servicesの接続と接続解除に使用するID
     */
    private static final int GOOGLE_API_CLIENT_ID = 908;
    /**
     * コレクションID
     */
    private static final String COLLECTION_ID = "user_location";
    /**
     * オブジェクトID
     */
    private static final String OBJECT_ID = "place_data";

    /**
     * {@link com.google.android.gms.common.api.GoogleApiClient}
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * {@link com.google.android.gms.maps.GoogleMap}
     */
    private GoogleMap mMap;

    /**
     * 地図上のマーカリスト
     */
    private List<Marker> mMarkerList;
    /**
     * 自分の位置を表す{@link Marker}(マーカを入れ替えるためListにしています)
     */
    private List<Marker> mMyMarker;

    /**
     * 地点名を取得するAsyncTask
     */
    private AsyncTask<Void, Void, Address> mMarkerAddressTask;

    /**
     * メッセージ入力フィールド
     */
    private EditText mMessageText;
    /**
     * 登録ボタン
     */
    private View mAddButton;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_place_map);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(new PushRegistrationFragment(), "registration").commit();
        }

        // TODO:ここに追加していきましょう
        // アピアリーズの初期化(データストアID、アプリID、アプリトークン)
        AB.Config.setDatastoreID(Config.DATA_STORE_ID);
        AB.Config.setApplicationID(Config.APP_ID);
        AB.Config.setApplicationToken(Config.APP_TOKEN);
        AB.activate(getApplicationContext());


        // メッセージ入力フィールド
        mMessageText = (EditText) findViewById(R.id.place_message);

        // 登録ボタン
        mAddButton = findViewById(R.id.add_place);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // メッセージ表示
                Toast.makeText(PickPlaceMapActivity.this, R.string.wait_for_minute, Toast.LENGTH_SHORT).show();
                execUpdateFlow(true);
            }
        });
        mAddButton.setEnabled(false);

        // 位置情報、地図の処理
        setupLocation();
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        if (mMarkerAddressTask != null) {
            mMarkerAddressTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pick_place_map, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        // 更新
        if (id == R.id.action_refresh) {
            // メッセージ表示
            Toast.makeText(this, R.string.wait_for_minute, Toast.LENGTH_SHORT).show();
            execUpdateFlow(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 位置情報サービスの接続エラー解決リクエスト
        if (requestCode == CONNECTION_FAILURE_RESOLUTION_REQUEST) {
            if (isServicesAvailable()) {
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * 位置情報取得機能を作成します。
     */
    private void setupLocation() {
        // GoogleApiClientの構築
        final GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(LocationServices.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        builder.enableAutoManage(this, GOOGLE_API_CLIENT_ID, this);
        mGoogleApiClient = builder.build();
    }

    /**
     * 位置情報更新の一連の処理を行います。
     *
     * @param isUpdateMyPlace 自位置の更新をする場合はtrue
     */
    private void execUpdateFlow(final boolean isUpdateMyPlace) {
        // TODO:ここに追加していきましょう
        ABDBObject dbObject = new ABDBObject(COLLECTION_ID);
        dbObject.setID(OBJECT_ID);
        AB.DBService.fetch(dbObject, new ResultCallback<ABDBObject>() {
            @Override
            public void done(ABResult<ABDBObject> abResult, ABException e) {
                if (e != null) {
                    showErrorMessage("Failure:" + e);
                    return;
                }

                // JSONの最上位部分を取得
                @SuppressWarnings("unchecked")
                HashMap<String, Object> rootMap = (HashMap<String, Object>) abResult.getData().getOriginalData();
                // 地図マーカの更新処理を実行
                handleMarkers(rootMap);

                // 自位置を更新しない場合
                if (!isUpdateMyPlace) {
                    Toast.makeText(PickPlaceMapActivity.this, R.string.place_data_refresh, Toast.LENGTH_SHORT).show();
                    return;
                }

                // 自位置のデータを更新
                updateMyPlace(rootMap);

            }
        });
    }

    /**
     * 自位置を更新します。<br/>
     * TODO:同時アクセスの処理は省略しています。 <br/>
     * TODO:厳密に処理するならば更新後に自分のデータが更新されたか確認する必要があります
     *
     * @param rootMap ルートとなる{@link HashMap}
     */
    private void updateMyPlace(HashMap<String, Object> rootMap) {
        // サーバから自動付加されるパラメータを削除
        rootMap.remove("_uby");
        rootMap.remove("_id");
        rootMap.remove("_uts");
        rootMap.remove("_cts");
        rootMap.remove("_cby");
        rootMap.remove("_coord");
        // 自位置の登録(上書き)
        rootMap.put(Installation.id4Ap(this), createMyPlaceData());

        // TODO:ここに追加していきましょう
        // rootMapオブジェクトを登録
        ABDBObject placeData = new ABDBObject(COLLECTION_ID);
        // オブジェクトIDを設定
        placeData.setID(OBJECT_ID);
        // 更新するためにapply
        placeData.apply();
        // 取得したデータを元に更新データを作成
        for (String key : rootMap.keySet()) {
            placeData.put(key, rootMap.get(key));
        }
        placeData.save(new ResultCallback<ABDBObject>() {
            @Override
            public void done(ABResult<ABDBObject> abResult, ABException e) {
                if (e != null) {
                    showErrorMessage("Failure:" + e);
                    return;
                }
                // 登録完了
                Toast.makeText(PickPlaceMapActivity.this, R.string.place_data_success, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * マーカの表示・削除処理などを行います。
     *
     * @param rootMap JSONパラメータ
     */
    private void handleMarkers(HashMap<String, Object> rootMap) {
        // 他のユーザの位置情報データを取得
        String myUuid = Installation.id4Ap(PickPlaceMapActivity.this);
        Set<String> uuids = rootMap.keySet();

        // 既存の（削除する）マーカの取得
        List<Marker> deleteMarkerList = new ArrayList<>();
        for (Marker marker : mMarkerList) {
            deleteMarkerList.add(marker);
        }

        // 新しいマーカの追加
        for (String uuid : uuids) {
            // 自分の位置は表示しない
            if (myUuid.equals(uuid)) {
                continue;
            }

            // サーバ側で自動的に付与されるキーを無視
            Object rootMapObject = rootMap.get(uuid);
            if (!(rootMapObject instanceof HashMap)) {
                continue;
            }

            // マーカを作成
            @SuppressWarnings("unchecked")
            HashMap<String, String> placeData = (HashMap<String, String>) rootMapObject;
            addUsersMarker(placeData.get("message"), placeData.get("lat"), placeData.get("long"));
        }

        // 古いマーカを削除
        for (Marker marker : deleteMarkerList) {
            marker.remove();
            mMarkerList.remove(marker);
        }

        // マーカを表示
        for (Marker marker : mMarkerList) {
            marker.showInfoWindow();
        }
    }

    /**
     * マーカを表示します。
     *
     * @param title タイトル
     * @param point {@link LatLng}
     */
    private void showMyMarker(final String title, final LatLng point) {
        // 全てのマーカを削除
        for (Marker marker : mMyMarker) {
            marker.remove();
            mMyMarker.remove(marker);
        }
        // 暫定的にマーカを追加
        Marker marker = mMap.addMarker(new MarkerOptions()//
                .title(title)//
                .position(point));//
        mMyMarker.add(marker);
        marker.showInfoWindow();
    }

    /**
     * 自位置のデータを作成します。<br/>
     * 緯度、経度、メッセージを生成します。
     *
     * @return 自位置のデータを表す{@link HashMap}
     */
    private HashMap<String, String> createMyPlaceData() {
        HashMap<String, String> myPlaceMap = new HashMap<>();
        // 緯度・経度
        Marker targetMarker = mMyMarker.get(0);
        LatLng latlng = targetMarker.getPosition();
        myPlaceMap.put("long", String.valueOf(latlng.longitude));
        myPlaceMap.put("lat", String.valueOf(latlng.latitude));
        // メッセージ（メッセージが空の場合はデフォルトメッセージを入れる）
        String message = mMessageText.getText().toString();
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.no_message);
        }
        myPlaceMap.put("message", message);
        return myPlaceMap;
    }

    /**
     * ユーザのマーカを追加します。
     *
     * @param title タイトル
     * @param lat   経度
     * @param lng   緯度
     */
    private void addUsersMarker(String title, String lat, String lng) {
        final LatLng point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        final MarkerOptions options = new MarkerOptions();
        options.title(title);
        options.position(point);
        final Marker marker = mMap.addMarker(options);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMarkerList.add(marker);
    }

    /**
     * エラーメッセージを表示します。
     *
     * @param message メッセージ
     */
    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * サービス接続のチェックを行います。
     *
     * @return サービス接続できたかどうか
     */
    private boolean isServicesAvailable() {
        // GooglePlayサービスが使用可能かチェック
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onConnectionFailed(final ConnectionResult result) {
        // エラーを解決できる場合
        if (result.hasResolution()) {
            try {
                // エラーを解決できるActivityを開始
                result.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                showErrorMessage(e.getLocalizedMessage());
            }
        }
        // エラーを解決できない場合
        else {
            showErrorMessage("onConnectionFailed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected(final Bundle connectionHint) {
        // 最後に取得した位置情報を取得
        final Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng latLng;
        if (location == null) {
            // 東京を表示
            latLng = new LatLng(35.689887, 139.693945);
        } else {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.moveCamera(cameraUpdate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // 必要あれば何か処理を書く
        System.out.println("suspend");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMarkerList = new ArrayList<>();
        mMyMarker = new ArrayList<>();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        final UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(false);
        settings.setMyLocationButtonEnabled(false);
        settings.setMapToolbarEnabled(false);
        execUpdateFlow(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapClick(LatLng point) {
        // 地点名とマーカを表示
        showMarkerWithPlaceName(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMapLongClick(final LatLng point) {
        // 地点名とマーカを表示
        showMarkerWithPlaceName(point);
    }

    /**
     * 地点名を含むマーカを表示します。
     *
     * @param point {@link LatLng}
     */
    private void showMarkerWithPlaceName(final LatLng point) {
        // 既に地点名取得を始めていたら停止
        if (mMarkerAddressTask != null) {
            mMarkerAddressTask.cancel(true);
        }

        showMyMarker(getString(R.string.show_marker_default), point);
        mAddButton.setEnabled(true);

        // 地点名取得の非同期タスク
        mMarkerAddressTask = new AsyncTask<Void, Void, Address>() {
            @Override
            protected Address doInBackground(Void... params) {
                return AddressSearchUtils.searchAddressSync(PickPlaceMapActivity.this, point.longitude, point.latitude);
            }

            @Override
            protected void onPostExecute(Address address) {
                // 検索結果がなければ何もしない
                if (address == null) {
                    return;
                }

                // マーカを表示
                showMyMarker(address.getAddressLine(1), point);
                mAddButton.setEnabled(true);
            }
        }.execute();
    }


}
