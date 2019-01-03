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

         findViewById(R.id.btnPermission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Permission.with(MainActivity.this).setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(MainActivity.this, "Permission Granted!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(MainActivity.this, "Permission Denied!!!", Toast.LENGTH_SHORT).show();
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
        });

    }
}
