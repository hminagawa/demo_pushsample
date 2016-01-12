package com.appiaries.pushsample;

/**
 * Listener to insert custome HTML content.
 */
public interface HtmlInjectionListener {

    /**
     * Called when the insertion is completed.
     *
     * @param newHtml HTML content already inserted.
     */
    void onInjectionFinish(String newHtml);
}
