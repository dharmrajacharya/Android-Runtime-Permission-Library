package com.lib.permission;

import java.util.List;

/**
 * Created by Dharmraj Acharya on 6/12/18 5:16 PM
 */
public interface PermissionListener {

    void onPermissionGranted();

    void onPermissionDenied(List<String> deniedPermissions);
}
