package org.proxydroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ProxyDroidActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        1
                );
            }
        }
    }

    // add ProxyDroid Fragment
    @Override
    public void onStart() {
        super.onStart();
        if (getSupportFragmentManager().findFragmentByTag("proxydroid_fragment") == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ProxyDroid(), "proxydroid_fragment")
                .commit();
        }
    }
}
