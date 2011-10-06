package org.necromant.nettts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BcastRecv extends BroadcastReceiver {
	SharedPreferences mPrefs;

	@Override
	public void onReceive(Context context, Intent intent) {

		//mPrefs = context.getSharedPreferences("nettts", 0);
		PreferenceManager.getDefaultSharedPreferences(context);
		
		Log.d("NetTTS", "AUTOSTART: " + mPrefs.getString("autostart", "NO") + "PORT: " + mPrefs.getString("port", "????") );
		
		if (mPrefs.getString("autostart", "NO").equals("YES")) {
			if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
				Intent i = new Intent();
				i.setAction("org.necromant.nettts.NetTTSService");
				context.startService(i);
				Log.d("!", "Poooh!");
			}
		};

	}

}
