package com.lib.permission;

import android.content.Context;

/**
 * Created by Dharmraj Acharya on 6/12/18 5:22 PM
 */
public class Permission extends PermissionBase {

    static final String TAG = Permission.class.getSimpleName();

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder extends PermissionBuilder<Builder> {

        private Builder(Context context) {
            super(context);
        }

        public void apply() {
            checkPermissions();
        }
    }
}
