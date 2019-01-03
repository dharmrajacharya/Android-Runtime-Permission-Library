package com.draj.org.permissionlib;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lib.permission.Permission;
import com.lib.permission.PermissionListener;

import java.util.List;
//import com.lib.permission.Permission;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Permission.with(this).setPermissionListener(new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //enterPhoneNumber();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                //enterPhoneNumber();
            }})
                .setRationaleTitle(getString(R.string.permission_required_phone))
                .setRationaleMessage(getString(R.string.permission_phone_description))
                .setRationaleConfirmText(getString(R.string.accept))
                .setDeniedTitle(getString(R.string.permission_denied))
                .setDeniedMessage(getString(R.string.permission_denied_message))
                .setGotoSettingButtonText(getString(R.string.permission_setting))
                .setPermissions(Manifest.permission.READ_PHONE_STATE)
                .apply();

    }
}
