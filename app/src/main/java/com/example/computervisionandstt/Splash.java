package com.example.computervisionandstt;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mStartTimeMillis = System.currentTimeMillis();

        /**
         * On a post-Android 6.0 devices, check if the required permissions have
         * been granted.
         */
        if (Build.VERSION.SDK_INT >= 23) {
            CheckPermissions();
        } else {
            StartNextActivity();
        }

    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

    /*
     * ---------------------------------------------
     *
     * Private Fields
     *
     * ---------------------------------------------
     */
    /**
     * The time that the splash screen will be on the screen in milliseconds.
     */
    private int mTimeoutMillis = 1000;

    /** The time when this {@link Activity} was created. */
    private long mStartTimeMillis = 0;

    /** The code used when requesting permissions */
    private static final int mPERMISSIONS_REQUEST = 1234;

    /*
     * ---------------------------------------------
     *
     * Getters
     *
     * ---------------------------------------------
     */
    /**
     * Get the time (in milliseconds) that the splash screen will be on the
     * screen before starting the {@link Activity} who's class is returned by
     * {@link #GetNextActivityClass()}.
     */
    public int getTimeoutMillis() {
        return mTimeoutMillis;
    }

    /** Get the {@link Activity} to start when the splash screen times out. */
    @SuppressWarnings("rawtypes")
    public Class GetNextActivityClass() {
        return MainActivity.class;
    };

    /**
     * Get the list of required permissions by searching the manifest. If you
     * don't think the default behavior is working, then you could try
     * overriding this function to return something like:
     *
     * <pre>
     * <code>
     * return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
     * </code>
     * </pre>
     */
    public String[] GetRequiredPermissions() {
        String[] mPermissions = null;
        try {
            mPermissions = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (mPermissions == null) {
            return new String[0];
        } else {
            return mPermissions.clone();
        }
    }

    /*
     * ---------------------------------------------
     *
     * Activity Methods
     *
     * ---------------------------------------------
     */

    private boolean checkPermissionFromDevice(){
        int write_external_storage_resuly= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result=ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO);
        return write_external_storage_resuly== PackageManager.PERMISSION_GRANTED && record_audio_result==PackageManager.PERMISSION_GRANTED;
    }

    /*
     * ---------------------------------------------
     *
     * Activity Methods
     *
     * ---------------------------------------------
     */

    /**
     * See if we now have all of the required dangerous permissions. Otherwise,
     * tell the user that they cannot continue without granting the permissions,
     * and then request the permissions again.
     */
    @TargetApi(23)

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == mPERMISSIONS_REQUEST) {
            CheckPermissions();
        }
    }

    /*
     * ---------------------------------------------
     *
     * Other Methods
     *
     * ---------------------------------------------
     */
    /**
     * After the timeout, start the {@link Activity} as specified by
     * {@link #GetNextActivityClass()}, and remove the splash screen from the
     * backstack. Also, we can change the message shown to the user to tell them
     * we now have the requisite permissions.
     */
    private void StartNextActivity() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
            }
        });
        long mDelayMillis = getTimeoutMillis() - (System.currentTimeMillis() - mStartTimeMillis);
        if (mDelayMillis < 0) {
            mDelayMillis = 0;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Splash.this, GetNextActivityClass()));
                finish();
            }
        }, mDelayMillis);
    }

    /**
     * Check if the required permissions have been granted, and
     * {@link #StartNextActivity()} if they have. Otherwise
     * {@link #requestPermissions(String[], int)}.
     */
    private void CheckPermissions() {
        String[] ungrantedPermissions = RequiredPermissionsStillNeeded();
        if (ungrantedPermissions.length == 0) {
            StartNextActivity();
        } else {
            requestPermissions(ungrantedPermissions, mPERMISSIONS_REQUEST);
        }
    }

    @TargetApi(23)
    private String[] RequiredPermissionsStillNeeded() {

        Set<String> permissions = new HashSet<String>();
        for (String permission : GetRequiredPermissions()) {
            permissions.add(permission);
        }
        for (Iterator<String> i = permissions.iterator(); i.hasNext();) {
            String permission = i.next();
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(Splash.class.getSimpleName(),
                        "Permission: " + permission + " already granted.");
                i.remove();
            } else {
                Log.d(Splash.class.getSimpleName(),
                        "Permission: " + permission + " not yet granted.");
            }
        }
        return permissions.toArray(new String[permissions.size()]);
    }


}
