# Android-Runtime-Permission-Library
A simple library to get android runtime permission

[![](https://jitpack.io/v/dharmrajacharya/Android-Runtime-Permission-Library.svg)](https://jitpack.io/#dharmrajacharya/Android-Runtime-Permission-Library)

 Step 1. Add the JitPack repository to your build file 
 
 allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  

Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.dharmrajacharya:Android-Runtime-Permission-Library:Tag'
	}
	
Step 3. Example

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

