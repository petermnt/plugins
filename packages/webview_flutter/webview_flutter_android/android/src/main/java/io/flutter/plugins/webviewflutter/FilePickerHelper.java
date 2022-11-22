package io.flutter.plugins.webviewflutter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import android.net.Uri;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.webkit.ValueCallback;

import android.webkit.WebChromeClient;
import org.jetbrains.annotations.NotNull;

class FilePickerHelper {

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final int FILE_CHOOSER_RESULT_CODE = 10000;
    public final int RESULT_OK = -1;

    private static FilePickerHelper single_instance = null;
    private Activity activity;

    private FilePickerHelper() {
    }

    public static FilePickerHelper getInstance() {
        if (single_instance == null) {
            single_instance = new FilePickerHelper();
        }
        return single_instance;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean onShowFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        uploadMessageAboveL = filePathCallback;
        if (activity == null) {
            return false;
        }
        Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);
        getContentIntent.setType("*/*");
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, activity.getString(R.string.select_file));
        chooserIntent.putExtra(Intent.EXTRA_INTENT, getContentIntent);
        activity.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }

    public boolean activityResult(int requestCode, int resultCode, Intent data) {
        if (null == uploadMessage && null == uploadMessageAboveL) {
            return false;
        }
        Uri result = null;
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            result = data == null || resultCode != RESULT_OK ? null : data.getData();
        }
        if (uploadMessageAboveL != null) {
            onActivityResultAboveL(requestCode, resultCode, data);
        }
        else if (uploadMessage != null && result != null) {
            uploadMessage.onReceiveValue(result);
            uploadMessage = null;
        }
        return false;
    }
}