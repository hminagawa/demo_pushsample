package com.appiaries.pushsample;

public class Config {

    /** Splash Duration Time (in milli-seconds) */
    public static final long SPLASH_TIME_MILLIS = 2000L;

    /** Website URL */
    public static final String BASE_URL = "http://www.appiaries.com/en/";

    /** Top Page */
    public static final String TAB_URL_HOME = BASE_URL + "";

    /** Pricing Page */
    public static final String TAB_URL_PRICING = BASE_URL + "pricing/";

    /** Control Panel Login */
    public static final String TAB_URL_LOGIN = "https://admin.appiaries.com/?lang=en";

    /** Docs */
    public static final String TAB_URL_DOCS = "http://docs.appiaries.com/?lang=en";

    /** FAQ */
    public static final String TAB_URL_FAQ = BASE_URL + "faq/";

    /** Login URL */
    public static final String[] LOGIN_TARGET_URLS = {BASE_URL + "mypage/login", TAB_URL_PRICING};

    /** Registration ID Key */
    public static final String PROPERTY_REG_ID = "registration_id";

    /** App Version Key */
    public static final String PROPERTY_APP_VERSION = "appVersion";

    /** Preference key for storing Registration ID */
    public static final String GCM_PREFERENCE = "pushsample";

    /**
     * Project Number (Sender ID)
     *   Project Name: appiaries info
     *   API Key: AIzaSyAc8vc8qf1KsKMLq6QmOG771fS_vJnFT_k
     */
    public static final String SENDER_ID = "458852476803";

    /** Datastore ID */
    public static final String DATASTORE_ID = "appiaries_sample";

    /** App ID */
    public static final String APPLICATION_ID = "push";

    /** App Token */
    public static final String APPLICATION_TOKEN = "app83b8c28a67937c7876fef91508";

    /** Custom Key 1 : TITLE */
    public static final String NOTIFICATION_KEY_TITLE = "title";

    /** Custom Key 1 : MESSAGE */
    public static final String NOTIFICATION_KEY_MESSAGE = "message";

    /** Key for Opened-Push Notification */
    public static final String KEY_PUSH_ID = "pushId";
}
