package com.lib.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.lib.permission.utils.ObjectUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Created by Dharmraj Acharya on 6/12/18 5:25 PM
 */
public class PermissionActivity extends AppCompatActivity {

    private static Deque<PermissionListener> permissionListenerStack;

    private CharSequence rationaleTitle;
    private CharSequence rationale_message;
    private CharSequence denyTitle;
    private CharSequence denyMessage;

    private String[] permissions;
    private String packageName;
    private String settingButtonText;
    private String deniedCloseButtonText;
    private String rationaleConfirmText;

    private boolean isShownRationaleDialog;
    private boolean hasSettingButton;
    private int requestedOrientation;

    public static void startActivity(Context context, Intent intent, PermissionListener listener) {
        if (permissionListenerStack == null) {
            permissionListenerStack = new ArrayDeque<>();
        }
        permissionListenerStack.push(listener);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setupFromSavedInstanceState(savedInstanceState);

        // check windows
        if (needWindowPermission()) {
            requestWindowPermission();
        } else {
            checkPermissions(false);
        }

        setRequestedOrientation(requestedOrientation);
    }


    private void setupFromSavedInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray(Constant.EXTRA_PERMISSIONS);
            rationaleTitle = savedInstanceState.getCharSequence(Constant.EXTRA_RATIONALE_TITLE);
            rationale_message = savedInstanceState.getCharSequence(Constant.EXTRA_RATIONALE_MESSAGE);
            denyTitle = savedInstanceState.getCharSequence(Constant.EXTRA_DENY_TITLE);
            denyMessage = savedInstanceState.getCharSequence(Constant.EXTRA_DENY_MESSAGE);
            packageName = savedInstanceState.getString(Constant.EXTRA_PACKAGE_NAME);
            hasSettingButton = savedInstanceState.getBoolean(Constant.EXTRA_SETTING_BUTTON, true);
            rationaleConfirmText = savedInstanceState.getString(Constant.EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = savedInstanceState.getString(Constant.EXTRA_DENIED_DIALOG_CLOSE_TEXT);
            settingButtonText = savedInstanceState.getString(Constant.EXTRA_SETTING_BUTTON_TEXT);
            requestedOrientation = savedInstanceState.getInt(Constant.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            Intent intent = getIntent();
            permissions = intent.getStringArrayExtra(Constant.EXTRA_PERMISSIONS);
            rationaleTitle = intent.getCharSequenceExtra(Constant.EXTRA_RATIONALE_TITLE);
            rationale_message = intent.getCharSequenceExtra(Constant.EXTRA_RATIONALE_MESSAGE);
            denyTitle = intent.getCharSequenceExtra(Constant.EXTRA_DENY_TITLE);
            denyMessage = intent.getCharSequenceExtra(Constant.EXTRA_DENY_MESSAGE);
            packageName = intent.getStringExtra(Constant.EXTRA_PACKAGE_NAME);
            hasSettingButton = intent.getBooleanExtra(Constant.EXTRA_SETTING_BUTTON, true);
            rationaleConfirmText = intent.getStringExtra(Constant.EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = intent.getStringExtra(Constant.EXTRA_DENIED_DIALOG_CLOSE_TEXT);
            settingButtonText = intent.getStringExtra(Constant.EXTRA_SETTING_BUTTON_TEXT);
            requestedOrientation = intent.getIntExtra(Constant.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private boolean needWindowPermission() {
        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                return !hasWindowPermission();
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasWindowPermission() {
        return Settings.canDrawOverlays(getApplicationContext());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestWindowPermission() {
        Uri uri = Uri.fromParts("package", packageName, null);
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);

        if (!TextUtils.isEmpty(rationale_message)) {
            new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setMessage(rationale_message)
                    .setCancelable(false)

                    .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivityForResult(intent, Constant.REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
                        }
                    })
                    .show();
            isShownRationaleDialog = true;
        } else {
            startActivityForResult(intent, Constant.REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST);
        }
    }

    private void checkPermissions(boolean fromOnActivityResult) {

        List<String> needPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                if (!hasWindowPermission()) {
                    needPermissions.add(permission);
                }
            } else {
                if (PermissionBase.isDenied(this, permission)) {
                    needPermissions.add(permission);
                }
            }
        }

        if (needPermissions.isEmpty()) {
            permissionResult(null);
        } else if (fromOnActivityResult) { //From Setting Activity
            permissionResult(needPermissions);
        } else if (needPermissions.size() == 1 && needPermissions
                .contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {   // window permission deny
            permissionResult(needPermissions);
        } else if (!isShownRationaleDialog && !TextUtils.isEmpty(rationale_message)) { // //Need Show Rationale
            showRationaleDialog(needPermissions);
        } else { // //Need Request Permissions
            requestPermissions(needPermissions);
        }
    }

    private void permissionResult(List<String> deniedPermissions) {
        Log.v(Permission.TAG, "permissionResult(): " + deniedPermissions);

        finish();
        overridePendingTransition(0, 0);

        if (permissionListenerStack != null) {
            PermissionListener listener = permissionListenerStack.pop();

            if (ObjectUtils.isEmpty(deniedPermissions)) {
                listener.onPermissionGranted();
            } else {
                listener.onPermissionDenied(deniedPermissions);
            }
            if (permissionListenerStack.size() == 0) {
                permissionListenerStack = null;
            }
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void showRationaleDialog(final List<String> needPermissions) {

        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle(rationaleTitle)
                .setMessage(rationale_message)
                .setCancelable(false)
                .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(needPermissions);
                    }
                }).show();
        isShownRationaleDialog = true;
    }

    public void requestPermissions(List<String> needPermissions) {
        ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]),
                Constant.REQ_CODE_PERMISSION_REQUEST);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(Constant.EXTRA_PERMISSIONS, permissions);
        outState.putCharSequence(Constant.EXTRA_RATIONALE_TITLE, rationaleTitle);
        outState.putCharSequence(Constant.EXTRA_RATIONALE_MESSAGE, rationale_message);
        outState.putCharSequence(Constant.EXTRA_DENY_TITLE, denyTitle);
        outState.putCharSequence(Constant.EXTRA_DENY_MESSAGE, denyMessage);
        outState.putString(Constant.EXTRA_PACKAGE_NAME, packageName);
        outState.putBoolean(Constant.EXTRA_SETTING_BUTTON, hasSettingButton);
        outState.putString(Constant.EXTRA_DENIED_DIALOG_CLOSE_TEXT, deniedCloseButtonText);
        outState.putString(Constant.EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);
        outState.putString(Constant.EXTRA_SETTING_BUTTON_TEXT, settingButtonText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        List<String> deniedPermissions = PermissionBase.getDeniedPermissions(this, permissions);

        if (deniedPermissions.isEmpty()) {
            permissionResult(null);
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }

    public void showPermissionDenyDialog(final List<String> deniedPermissions) {

        if (TextUtils.isEmpty(denyMessage)) {
            // denyMessage
            permissionResult(deniedPermissions);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);

        builder.setTitle(denyTitle)
                .setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionResult(deniedPermissions);
                    }
                });

        if (hasSettingButton) {

            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.permission_setting);
            }

            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PermissionBase.startSettingActivityForResult(PermissionActivity.this);

                }
            });

        }
        builder.show();
    }

    public boolean shouldShowRequestPermissionRationale(List<String> needPermissions) {

        if (needPermissions == null) {
            return false;
        }

        for (String permission : needPermissions) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this, permission)) {
                return false;
            }
        }

        return true;

    }

    public void showWindowPermissionDenyDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermissions(false);
                    }
                });

        if (hasSettingButton) {
            if (TextUtils.isEmpty(settingButtonText)) {
                settingButtonText = getString(R.string.permission_setting);
            }

            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Uri uri = Uri.fromParts("package", packageName, null);
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                    startActivityForResult(intent, Constant.REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING);
                }
            });

        }
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case PermissionBase.REQ_CODE_REQUEST_SETTING:
                checkPermissions(true);
                break;
            case Constant.REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST:
                if (!hasWindowPermission() && !TextUtils.isEmpty(denyMessage)) {
                    showWindowPermissionDenyDialog();
                } else {
                    checkPermissions(false);
                }
                break;
            case Constant.REQ_CODE_SYSTEM_ALERT_WINDOW_PERMISSION_REQUEST_SETTING:
                checkPermissions(false);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
