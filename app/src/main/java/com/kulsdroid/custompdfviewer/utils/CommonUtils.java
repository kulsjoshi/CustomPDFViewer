package com.kulsdroid.custompdfviewer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.kulsdroid.custompdfviewer.application.MyApplication;

import java.io.IOException;

/**
 * Created by KulsDroid on 9/15/2017.
 */

public class CommonUtils {

    /**
     * This function will remove white space from String from URLs.
     * @param url
     * @return
     */
    public static String removeWhiteSpaceFromURL(String url) {
        return url.replaceAll(" ", "%20");
    }

    /**
     * This function will check internet connection is available or not.
     * @return
     */
    public static boolean isInternetAvailable() {

        boolean result = false;

        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * This function return string from string.xml file
     * @param stringId
     * @return
     */
    public static String getString(int stringId) {
        return MyApplication.getContext().getResources().getString(stringId);
    }

    /**
     * This function will return color id from color.xml file
     * @param colorId
     * @return
     */
    public static int getColor(int colorId) {
        return MyApplication.getContext().getResources().getColor(colorId);
    }

    /**
     * This function will find percentage from given data and will return it.
     * @param currentProgress
     * @param totalProgress
     * @return
     */
    public static int findPercentage(long currentProgress, long totalProgress) {
        return (int) ((currentProgress * 100) / totalProgress);
    }

    /**
     * This function will show toast in the application
     * @param message
     */
    public static void showToast(String message) {
        if (!message.isEmpty()) {
            Toast.makeText(MyApplication.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * It will return bitmap from your Uri
     * @param uri
     * @return
     * @throws IOException
     */
    public static Bitmap getBitmapFromUri(Uri uri) throws IOException {
        Bitmap mBitmap = null;

        if(uri!=null){
            mBitmap = MediaStore.Images.Media.getBitmap(
                    MyApplication.getContext().getContentResolver(), uri);
        }
        return mBitmap;
    }

    /**
     * This will hide the soft keyboard input
     */
    public static void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) MyApplication.getContext()
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /**
     * This function will check email validation
     * @param target
     * @return
     */
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
