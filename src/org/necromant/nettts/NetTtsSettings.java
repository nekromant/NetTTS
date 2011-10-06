package org.necromant.nettts;



import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Locale;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.SharedPreferences;





public class NetTtsSettings extends Activity {
	/** Called when the activity is first created. */
	private TextToSpeech mTts;
	private OnInitListener onInit;
    private static final String TAG = "NetTTS";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button kill = (Button) findViewById(R.id.kill);
		kill.setOnClickListener(killClickListener);
		Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(startClickListener);
		
		LoadPrefs();
	}
	


private Locale Locale(String string) {
		// TODO Auto-generated method stub
		return null;
	}

private void SavePreferences(String key, String value){
    SharedPreferences sharedPreferences = getSharedPreferences("NetTTS", MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(key, value);
    editor.commit();
   }

private void LoadPrefs(){
    SharedPreferences sharedPreferences = getSharedPreferences("NetTTS", MODE_PRIVATE);
    String port = sharedPreferences.getString("port", "8080");
    EditText prt = (EditText) findViewById(R.id.port);
    prt.setText(port);
    
    EditText loc = (EditText) findViewById(R.id.loc);
    loc.setText(sharedPreferences.getString("locale", "ru_RU"));
    
    
    CheckBox auto = (CheckBox) findViewById(R.id.autostart);
    String a = sharedPreferences.getString("autostart", "NO");
    Log.d(TAG, "=======>" + a);
    if (a.equals("YES")) {
    	auto.setChecked(true);
    }
   }

private void SavePrefs()
{
	EditText prt = (EditText) findViewById(R.id.port);
	EditText loc = (EditText) findViewById(R.id.loc);
	SavePreferences("port", prt.getText().toString());
	SavePreferences("locale", loc.getText().toString());
	CheckBox auto = (CheckBox) findViewById(R.id.autostart);
	if (auto.isChecked())
	{
		SavePreferences("autostart", "YES");
	}else
	{
		SavePreferences("autostart", "NO");
	}
}


//////////////////////// CLICK LISTENERS //////////////////
	private OnClickListener startClickListener = new OnClickListener() {
		public void onClick(View v) {
			long id = 0;
			// do something when the button is clicked
			try {
				// helloName = (EditText)findViewById(R.id.helloName);
				Context context = getApplicationContext();
				SavePrefs();
				Intent svc = new Intent();
				svc.setClassName("org.necromant.nettts" ,"org.necromant.nettts.NetTTSService") ;
				startService(svc);
			} catch (Exception ex) {

			}
		}
	};
	
	private OnClickListener killClickListener = new OnClickListener() {
		public void onClick(View v) {
			long id = 0;
			// do something when the button is clicked
			try {
				// helloName = (EditText)findViewById(R.id.helloName);
				Context context = getApplicationContext();
				Intent svc = new Intent();
				svc.setClassName("org.necromant.nettts" ,"org.necromant.nettts.NetTTSService") ;
				stopService(svc);
			} catch (Exception ex) {

			}
		}
	};
}
