package org.proxydroid.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import org.proxydroid.ProxyDroidService;
import org.proxydroid.R;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.Shell.Result;

public class Utils {

	public static final String TAG = "ProxyDroid";

	private static boolean initialized = false;
	private static boolean isConnecting = false;
	private static int hasRedirectSupport = -1;
	static {
//        Shell.enableVerboseLogging = BuildConfig.DEBUG;
		Shell.setDefaultBuilder(
			Shell.Builder.create()
				.setFlags(Shell.FLAG_MOUNT_MASTER)
				.setTimeout(10)
		);
	}
	public static synchronized void ensureRootInitialized()
	{
		if (initialized) return;
		try {
			Shell shell = Shell.getShell(); // блокирующий вызов, ждёт готовности рута
			if (shell.isRoot()) {
				Log.i("Utils", "Root shell initialized");
				initialized = true;
			} else {
				Log.e("Utils", "Shell is not root!");
			}
		} catch (Exception e) {
			Log.e("Utils", "Error initializing root", e);
		}
	}

	// -----------------------
	// Совместимость старого кода
	// -----------------------
	public static String getRootShell() {
		return "su"; // для старого кода, libsu использует su
	}

	public static String getIptables() {
		return "iptables"; // старый код
	}

	public static boolean isRoot() {
		ensureRootInitialized();
		return Shell.isAppGrantedRoot();
	}

	public static int runRootCommand(String command) {
		try {
			Result result = Shell.cmd(command).exec();
			if (result.isSuccess()) {
				return 0;
			} else {
				Integer code = result.getCode(); // libsu 6.x
				return code != null ? code : -1;
			}
		} catch (Exception e) {
			Log.e(TAG, "runRootCommand failed", e);
			return -1;
		}
	}

	public static String runRootCommandResult(String command) {
		try {
			Result result = Shell.cmd(command).exec();
			List<String> out = result.getOut();
			return String.join("\n", out);
		} catch (Exception e) {
			Log.e(TAG, "runRootCommandResult failed", e);
			return null;
		}
	}

	public static String preserve(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '$' || c == '`' || c == '"') sb.append('\\');
			sb.append(c);
		}
		return sb.toString();
	}

	public static void initHasRedirectSupported() {
		if (!isRoot()) return;
		String cmd = getIptables() + " -t nat -A OUTPUT -p udp --dport 54 -j REDIRECT --to 8154";
		int code = runRootCommand(cmd);
		// flush
		runRootCommand(cmd.replace("-A", "-D"));
		hasRedirectSupport = (code == 0) ? 1 : 0;
	}

	public static boolean getHasRedirectSupport() {
		if (hasRedirectSupport == -1) initHasRedirectSupported();
		return hasRedirectSupport == 1;
	}

	public static boolean isConnecting() {
		return isConnecting;
	}

	public static void setConnecting(boolean value) {
		isConnecting = value;
	}

	public static boolean isInitialized() {
		if (initialized)
			return true;
		else {
			initialized = true;
			return false;
		}
	}

	public static boolean isWorking() {
		return ProxyDroidService.isServiceStarted();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (; ; ) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ignored) {
		}
	}

	public static Drawable getAppIcon(Context c, int uid) {
		PackageManager pm = c.getPackageManager();
		Drawable appIcon = c.getResources().getDrawable(R.drawable.sym_def_app_icon);
		String[] packages = pm.getPackagesForUid(uid);

		if (packages != null && packages.length == 1) {
			try {
				ApplicationInfo appInfo = pm.getApplicationInfo(packages[0], 0);
				appIcon = pm.getApplicationIcon(appInfo);
			} catch (PackageManager.NameNotFoundException e) {
				Log.e(c.getPackageName(), "No package found matching with the uid " + uid);
			}
		} else {
			Log.e(c.getPackageName(), "Package not found for uid " + uid);
		}

		return appIcon;
	}

	private static String data_path = null;

	public static String getDataPath(Context ctx) {
		if (data_path == null) {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				data_path = Environment.getExternalStorageDirectory().getAbsolutePath();
			} else {
				data_path = "/sdcard";
			}
			Log.d(TAG, "Data Path: " + data_path);
		}
		return data_path;
	}
}
