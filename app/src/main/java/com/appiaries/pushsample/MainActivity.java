/**
 * Push Notification Sample App for Appiaries v.0.0.1
 * Updated: 2016.1.7
 *
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Appiaries Co.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.appiaries.pushsample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * Activity for the Web View.
 * (you may see the previous page if you switch tabs before the page is completely loaded)
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, HtmlInjectionListener, WebViewLoadingListener {

    /** Web View */
    private WebView mWebView;

    /** Notification */
    private View mNotificationInfoView;

    /** Progress Indicator (show how far the page is loaded) */
    private View mProgressBar;

    /** Custom View */
    private CustomWebViewClient mWebViewClient;

    /** Tag for logs */
    private static final String TAG = "AppiariesReg";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	// Currently, the toolbar is not visible. To make it visible, uncomment the following.
        // setSupportActionBar(toolbar);

        // Action Bar Settings
        // final ActionBar actionBar = getSupportActionBar();
        // actionBar.setDisplayShowTitleEnabled(false);
        // actionBar.setDisplayHomeAsUpEnabled(true);

        // Tabs Settings
        findViewById(R.id.tab_home).setOnClickListener(this);
        findViewById(R.id.tab_pricing).setOnClickListener(this);
        findViewById(R.id.tab_login).setOnClickListener(this);
        findViewById(R.id.tab_docs).setOnClickListener(this);
        findViewById(R.id.tab_faq).setOnClickListener(this);

        // Notification View
        mNotificationInfoView = findViewById(R.id.notification_info);

        // Progress Indicator
        mProgressBar = findViewById(R.id.progress);

        // Web View Settings
        mWebView = (WebView) findViewById(R.id.webview);
        final WebSettings webSettings = mWebView.getSettings();
        // WebView Initialization
        webSettings.setJavaScriptEnabled(true);
        mWebViewClient = new CustomWebViewClient(this);
        mWebView.setWebViewClient(mWebViewClient);
        // RegistrationID
        final SharedPreferences preferences = getSharedPreferences(Config.GCM_PREFERENCE, Context.MODE_PRIVATE);
        final String regId = preferences.getString(Config.PROPERTY_REG_ID, "");
        mWebView.addJavascriptInterface(new JavaScriptInterface(regId, this), "HtmlViewer");

	// Upon receiving the notification, show the content of the notification.
        final Intent intent = getIntent();
        Log.i(TAG, "intent action: " + intent.getAction());
        if (intent != null && PushBroadcastReceiver.ACTION_NOTIFICATION_OPEN.equals(intent.getAction())) {
            Log.i(TAG, "Notification tapped.");
            mWebView.setVisibility(View.GONE);
            mNotificationInfoView.setVisibility(View.VISIBLE);
            final String title = intent.getStringExtra(Config.NOTIFICATION_KEY_TITLE);
            final String message = intent.getStringExtra(Config.NOTIFICATION_KEY_MESSAGE);
            ((TextView) findViewById(R.id.notification_title)).setText(title);
            ((TextView) findViewById(R.id.notification_message)).setText(message);
        } else {
            Log.i(TAG, "WebView becoming visible.");
            mWebView.loadUrl(Config.TAB_URL_HOME);
            mWebView.setVisibility(View.VISIBLE);
            mNotificationInfoView.setVisibility(View.GONE);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        // HOME for the action bar.
        if (id == android.R.id.home) {
            onBack();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        final int id = v.getId();
        mWebView.setVisibility(View.VISIBLE);
        mNotificationInfoView.setVisibility(View.GONE);
        // Top Page
        if (id == R.id.tab_home) {
            mWebView.loadUrl(Config.TAB_URL_HOME);
        }
        // Pricing Page
        else if (id == R.id.tab_pricing) {
            mWebView.loadUrl(Config.TAB_URL_PRICING);
        }
        // Control Panel Login
        else if (id == R.id.tab_login) {
            mWebView.loadUrl(Config.TAB_URL_LOGIN);
        }
        // Official Docs
        else if (id == R.id.tab_docs) {
            mWebView.loadUrl(Config.TAB_URL_DOCS);
        }
        // FAQ
        else if (id == R.id.tab_faq) {
            mWebView.loadUrl(Config.TAB_URL_FAQ);
        }

        // Delete history when the page is completely loaded.
        mWebViewClient.setClearHistory(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        onBack();
    }

    /**
     * Hardware BACK button.
     */
    private void onBack() {
        // When web view is visible and has a history.
        if (mWebView.getVisibility() == View.VISIBLE && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    /**
     * {@inheritDoc}
     */
    @JavascriptInterface
    @Override
    public void onInjectionFinish(final String newHtml) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.loadDataWithBaseURL(Config.BASE_URL, newHtml, "text/html; charset=utf-8;", "utf-8",null);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFinishLoading() {
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * Customized Web View Client
     */
    static class CustomWebViewClient extends WebViewClient {

        /**
         * For clearing the page history.
         */
        private boolean mClearHistory;

        /**
         * Weak reference for progress bar.
         */
        private final WebViewLoadingListener mListener;

        /**
         * Constructor
         * @param listener WebViewLoadingListener
         */
        CustomWebViewClient(WebViewLoadingListener listener) {
            mListener = listener;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mListener.onStartLoading();
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mClearHistory) {
                view.clearHistory();
                mClearHistory = false;
            }

	    // This is not in use. Forget about this.
	    /*
            for (String targetUrl : Config.LOGIN_TARGET_URLS) {
                if (url.startsWith(targetUrl)) {
                    view.loadUrl("javascript:HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                    break;
                }
            }
	    */

            mListener.onFinishLoading();
        }

        /**
         * Switching Delete flag for page history.
         *
         * @param clearHistory "true" for deleting page history.
         */
        public void setClearHistory(boolean clearHistory) {
            mClearHistory = clearHistory;
        }

    }

    /**
     * JavaScript (this is not used for this app... forget the rest)
     */
    static class JavaScriptInterface {

        /**
         * RegistrationID
         */
        private final String mRegId;

        /**
         * HtmlInjectionListener
         */
        private final HtmlInjectionListener mListener;

        /**
         * Constructor
         *
         * @param regId    RegistrationID
         * @param listener HtmlInjectionListener
         */
        JavaScriptInterface(String regId, HtmlInjectionListener listener) {
            mRegId = regId;
            mListener = listener;
        }

        @JavascriptInterface
        public void showHTML(String html) {
            // Creating HTML to insert.
            String insertStr = "<input type=\"hidden\" name=\"login_device_id\" value=\"" + mRegId + "\"/>";
            insertStr += "<input type=\"hidden\" name=\"login_os\" value=\"android\">";

	    // Simple HTML analysis (inserted right after the specific word)
            final String targetText = "<form name=\"login_mypage\"";
            final int index = html.indexOf(targetText);

            // Found!
            if (index != -1) {
                // Find the closing tag.
                final String closeTag = "</form>";
                final int closeTagIndex = html.indexOf(closeTag, index);
                final StringBuilder builder = new StringBuilder(html);
                builder.insert(closeTagIndex - closeTag.length(), insertStr);
                // Notify it is completed.
                mListener.onInjectionFinish(builder.toString());
            }
        }
    }
}
