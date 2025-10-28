/* proxydroid - Global / Individual Proxy App for Android
 * Copyright (C) 2025 Igor Baranov <industrium@gmail.com>
 * Copyright (C) 2011 K's Maze <kafkasmaze@gmail.com>
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
 *
 */
package org.proxydroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * @author KsMaze
 * 
 */
public class Profile implements Serializable {

	private String name;
	private String host;
	private String proxyType;
	private int port;
	private String bypassAddrs;
	private String user;
	private String password;
        private String certificate;
	private String proxyedApps;
	private boolean isGlobalProxy = false;
	private boolean isBypassApps = false;
	private boolean isAuth = false;
	private boolean isNTLM = false;
	private boolean isPAC = false;

	private String domain;

	public Profile() {
		init();
	}

	public void getProfile(SharedPreferences settings) {
		name = settings.getString("profileName", "");

		host = settings.getString("host", "");
		proxyType = settings.getString("proxyType", "http");
		user = settings.getString("user", "");
		password = settings.getString("password", "");
		bypassAddrs = settings.getString("bypassAddrs", "");
		proxyedApps = settings.getString("Proxyed", "");
		domain = settings.getString("domain", "");
                certificate = settings.getString("certificate", "");

		isAuth = settings.getBoolean("isAuth", false);
		isNTLM = settings.getBoolean("isNTLM", false);
		isGlobalProxy = settings.getBoolean("isGlobalProxy", false);
		isBypassApps = settings.getBoolean("isBypassApps", false);
		isPAC = settings.getBoolean("isPAC", false);

		String portText = settings.getString("port", "");

		if (name.equals("")) {
			name = host + ":" + port + "." + proxyType;
		}

		try {
			port = Integer.valueOf(portText);
		} catch (Exception e) {
			port = 3128;
		}
	}

	public void setProfile(SharedPreferences settings) {
		Editor ed = settings.edit();
		ed.putString("profileName", name);
		ed.putString("host", host);
		ed.putString("port", Integer.toString(port));
		ed.putString("bypassAddrs", bypassAddrs);
		ed.putString("Proxyed", proxyedApps);
		ed.putString("user", user);
		ed.putString("password", password);
		ed.putBoolean("isAuth", isAuth);
		ed.putBoolean("isNTLM", isNTLM);
		ed.putString("domain", domain);
		ed.putString("proxyType", proxyType);
		ed.putString("certificate", certificate);
		ed.putBoolean("isGlobalProxy", isGlobalProxy);
		ed.putBoolean("isBypassApps", isBypassApps);
		ed.putBoolean("isPAC", isPAC);
		ed.commit();
	}

	public void init() {
		host = "";
		port = 3128;
		user = "";
		domain = "";
		password = "";
		certificate = "";
		isAuth = false;
		proxyType = "http";
		isNTLM = false;
		bypassAddrs = "";
		proxyedApps = "";
		isPAC = false;
	}

	@Override
	public String toString() {
		return this.encodeJson().toString();
	}

	public JSONObject encodeJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("name", name);
			obj.put("host", host);
			obj.put("proxyType", proxyType);
			obj.put("user", user);
			obj.put("password", password);
			obj.put("domain", domain);
			obj.put("certificate", certificate);
			obj.put("bypassAddrs", bypassAddrs);
			obj.put("Proxyed", proxyedApps);

			obj.put("isAuth", isAuth);
			obj.put("isNTLM", isNTLM);
			obj.put("isGlobalProxy", isGlobalProxy);
			obj.put("isBypassApps", isBypassApps);
			obj.put("isPAC", isPAC);
			obj.put("port", port);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj;
	}

	static class JSONDecoder {
		private final JSONObject obj;

		public JSONDecoder(String values) throws JSONException {
			obj = new JSONObject(values);
		}

		public String getString(String key, String def) {
			return obj.optString(key, def);
		}

		public int getInt(String key, int def) {
			return obj.optInt(key, def);
		}

		public boolean getBoolean(String key, boolean def) {
			if (obj.has(key)) {
				return obj.optBoolean(key, def);
			}
			return def;
		}
	}

	public void decodeJson(String values) {
		JSONDecoder jd;

		try {
			jd = new JSONDecoder(values);
		} catch (JSONException e) {
			return;
		}

		name = jd.getString("name", "");
		host = jd.getString("host", "");
		proxyType = jd.getString("proxyType", "http");
		user = jd.getString("user", "");
		password = jd.getString("password", "");
		domain = jd.getString("domain", "");
		certificate = jd.getString("certificate", "");
		bypassAddrs = jd.getString("bypassAddrs", "");
		proxyedApps = jd.getString("Proxyed", "");

		port = jd.getInt("port", 3128);

		isAuth = jd.getBoolean("isAuth", false);
		isNTLM = jd.getBoolean("isNTLM", false);
		isGlobalProxy = jd.getBoolean("isGlobalProxy", false);
		isBypassApps = jd.getBoolean("isBypassApps", false);
		isPAC = jd.getBoolean("isPAC", false);

	}

	public static String validateAddr(String ia) {

		boolean valid1 = Pattern.matches(
				"[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}/[0-9]{1,2}",
				ia);
		boolean valid2 = Pattern.matches(
				"[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}", ia);

		if (valid1 || valid2) {
			return ia;
		} else {
			String addrString = null;
			try {
				InetAddress addr = InetAddress.getByName(ia);
				addrString = addr.getHostAddress();
			} catch (Exception ignore) {
				addrString = null;
			}
			if (addrString != null) {
				boolean valid3 = Pattern.matches(
						"[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}",
						addrString);
				if (!valid3)
					addrString = null;
			}
			return addrString;
		}
	}

	public static String[] decodeAddrs(String addrs) {
		String[] list = addrs.split("\\|");
		Vector<String> ret = new Vector<String>();
		for (String addr : list) {
			String ta = validateAddr(addr);
			if (ta != null)
				ret.add(ta);
		}
		return ret.toArray(new String[0]);
	}

	public static String encodeAddrs(String[] addrs) {

		if (addrs.length == 0)
			return "";

		StringBuilder sb = new StringBuilder();
		for (String addr : addrs) {
			String ta = validateAddr(addr.trim());
			if (ta != null)
				sb.append(ta).append("|");
		}
		return sb.substring(0, sb.length() - 1);
	}

	// --- getters and setters ---
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }

	public String getProxyType() { return proxyType; }
	public void setProxyType(String proxyType) { this.proxyType = proxyType; }

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }

	public String getBypassAddrs() { return bypassAddrs; }
	public void setBypassAddrs(String bypassAddrs) { this.bypassAddrs = bypassAddrs; }

	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getCertificate() { return certificate; }
	public void setCertificate(String certificate) { this.certificate = certificate; }

	public boolean isGlobalProxy() { return isGlobalProxy; }
	public void setGlobalProxy(boolean isGlobalProxy) { this.isGlobalProxy = isGlobalProxy; }

	public boolean isBypassApps() { return isBypassApps; }
	public void setBypassApps(boolean isBypassApps) { this.isBypassApps = isBypassApps; }

	public boolean isAuth() { return isAuth; }
	public void setAuth(boolean isAuth) { this.isAuth = isAuth; }

	public boolean isNTLM() { return isNTLM; }
	public void setNTLM(boolean isNTLM) { this.isNTLM = isNTLM; }

	public String getDomain() { return domain; }
	public void setDomain(String domain) { this.domain = domain; }

	public boolean isPAC() { return isPAC; }
	public void setPAC(boolean isPAC) { this.isPAC = isPAC; }

	public String getProxyedApps() { return proxyedApps; }
	public void setProxyedApps(String proxyedApps) { this.proxyedApps = proxyedApps; }

	// --- ProfileUtils ---
	public static class ProfileUtils {
		private static final String TAG = "ProfileUtils";

		public static void renameProfile(String profile, String name, Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

			if (name == null) return;
			name = name.replace("|", "");

			Editor ed = settings.edit();
			ed.putString("profile" + profile, name);
			ed.commit();

			String[] profileEntries = settings.getString("profileEntries", "").split("\\|");
			String[] profileValues = settings.getString("profileValues", "").split("\\|");

			StringBuilder profileEntriesBuffer = new StringBuilder();
			StringBuilder profileValuesBuffer = new StringBuilder();

			for (int i = 0; i < profileValues.length - 1; i++) {
				if (profileValues[i].equals(profile)) {
					profileEntriesBuffer.append(getProfileName(profile, context)).append("|");
				} else {
					profileEntriesBuffer.append(profileEntries[i]).append("|");
				}
				profileValuesBuffer.append(profileValues[i]).append("|");
			}

			profileEntriesBuffer.append(context.getString(R.string.profile_new));
			profileValuesBuffer.append("0");

			ed = settings.edit();
			ed.putString("profileEntries", profileEntriesBuffer.toString());
			ed.putString("profileValues", profileValuesBuffer.toString());

			ed.commit();
		}

		public static String getProfileName(String profile, Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			return settings.getString("profile" + profile,
					context.getString(R.string.profile_base) + " " + profile);
		}

		public static void delProfile(String profile, Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

			String[] profileEntries = settings.getString("profileEntries", "").split("\\|");
			String[] profileValues = settings.getString("profileValues", "").split("\\|");

			Log.d(TAG, "Profile :" + profile);
			if (profileEntries.length > 2) {
				StringBuilder profileEntriesBuffer = new StringBuilder();
				StringBuilder profileValuesBuffer = new StringBuilder();

				String newProfileValue = "1";

				for (int i = 0; i < profileValues.length - 1; i++) {
					if (!profile.equals(profileValues[i])) {
						profileEntriesBuffer.append(profileEntries[i]).append("|");
						profileValuesBuffer.append(profileValues[i]).append("|");
						newProfileValue = profileValues[i];
					}
				}
				profileEntriesBuffer.append(context.getString(R.string.profile_new));
				profileValuesBuffer.append("0");

				Editor ed = settings.edit();
				ed.putString("profileEntries", profileEntriesBuffer.toString());
				ed.putString("profileValues", profileValuesBuffer.toString());
				ed.putString("profile", newProfileValue);
				ed.remove(profile);
				ed.remove("profile" + profile);
				ed.commit();
			}
		}

		public static String[] getProfileEntries(Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String[] profileEntries = settings.getString("profileEntries", "").split("\\|");
			if (profileEntries.length == 0 || profileEntries[0].isEmpty()) {
				profileEntries = new String[]{context.getString(R.string.profile_new)};
			}
			return profileEntries;
		}

		public static String[] getProfileValues(Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			String[] profileValues = settings.getString("profileValues", "").split("\\|");
			if (profileValues.length == 0 || profileValues[0].isEmpty()) {
				profileValues = new String[]{"0"};
			}
			return profileValues;
		}

		public static boolean switchProfile(String oldProfileId, String profileId, Context context) {
			if(profileId.equals(oldProfileId)) {
				return true;
			}

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			Editor ed = settings.edit();

			Profile profile = new Profile();
			profile.getProfile(settings);
			if(profile.toString().equals(settings.getString(profileId, ""))) {
				// this function may have been called before by ProxyDroidCLI's logic.
				// If not, it still doesn't matter because it's the same configuration.
				return true;
			}
			ed.putString(oldProfileId, profile.toString());
			ed.commit();

			String profileStr = settings.getString(profileId, "");
			if ("".equals(profileStr)) {
				profile.init();
				profile.setName(getProfileName(profileStr, context));
			} else {
				profile.decodeJson(profileStr);
			}


			profile.setProfile(settings);
			ed.commit();
			return true;
		}

		public static void addProfile(Context context) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			Editor ed = settings.edit();

			String[] profileEntries = settings.getString("profileEntries", "").split("\\|");
			String[] profileValues = settings.getString("profileValues", "").split("\\|");
			int newProfileValue = Integer.valueOf(profileValues[profileValues.length - 2]) + 1;

			StringBuilder profileEntriesBuffer = new StringBuilder();
			StringBuilder profileValuesBuffer = new StringBuilder();

			for (int i = 0; i < profileValues.length - 1; i++) {
				profileEntriesBuffer.append(profileEntries[i]).append("|");
				profileValuesBuffer.append(profileValues[i]).append("|");
			}
			profileEntriesBuffer.append(getProfileName(Integer.toString(newProfileValue), context)).append("|");
			profileValuesBuffer.append(newProfileValue).append("|");
			profileEntriesBuffer.append(context.getString(R.string.profile_new));
			profileValuesBuffer.append("0");

			ed.putString("profileEntries", profileEntriesBuffer.toString());
			ed.putString("profileValues", profileValuesBuffer.toString());
			ed.putString("profile", Integer.toString(newProfileValue));
			ed.commit();
		}
	}

}
