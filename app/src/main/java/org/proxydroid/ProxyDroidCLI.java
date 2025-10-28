/* proxydroid - Global / Individual Proxy App for Android
 * Copyright (C) 2025 Igor Baranov <industrium@gmail.com>
 * Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.proxydroid;

import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import org.proxydroid.utils.Utils;

public class ProxyDroidCLI extends BroadcastReceiver {
	private static final String TAG = "ProxyDroidCLI";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: " + intent);

		if (intent == null) return;
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getBoolean("pref_autostart", false)) {
				if (!Utils.isWorking()) {
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

					Profile profile = new Profile();
					profile.getProfile(settings);
					try {
						Intent it = new Intent(context, ProxyDroidService.class);
						Bundle bundle = new Bundle();
						bundle.putString("host", profile.getHost());
						bundle.putString("user", profile.getUser());
						bundle.putString("bypassAddrs", profile.getBypassAddrs());
						bundle.putString("password", profile.getPassword());
						bundle.putString("domain", profile.getDomain());
						bundle.putString("certificate", profile.getCertificate());

						bundle.putString("proxyType", profile.getProxyType());
						bundle.putBoolean("isGlobalProxy", profile.isGlobalProxy());
						bundle.putBoolean("isBypassApps", profile.isBypassApps());
						bundle.putBoolean("isAuth", profile.isAuth());
						bundle.putBoolean("isNTLM", profile.isNTLM());
						bundle.putBoolean("isPAC", profile.isPAC());

						bundle.putInt("port", profile.getPort());

						it.putExtras(bundle);
						if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							context.startForegroundService(it);
						} else {
							context.startService(it);
						}
						setResultData("SUCCESS");

					} catch (Exception ignore) {
						// Nothing
						setResultData("FAILURE");
					}
				}
			} else {
				if (Utils.isWorking() && !Utils.isConnecting()) {
					try {
						context.stopService(new Intent(context, ProxyDroidService.class));
						setResultData("SUCCESS");
					} catch (Exception e) {
						setResultData("FAILURE");
					}
				}
			}
		}
	}
}
