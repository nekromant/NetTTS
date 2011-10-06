package org.necromant.nettts;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Boolean;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Toast;

public class NetTTSService extends Service {
	private static final String TAG = "NetTTS";
	private boolean started = false;
	private TCPServer srv = new TCPServer();
	private Thread t = new Thread(srv);
	private String locale;
	SharedPreferences prefMgr;

	public class TCPServer implements Runnable {
		public int SERVERPORT = 4444;
		public TextToSpeech mTts;
		boolean running = true;

		private OnInitListener onInit;

		public void onInit(int status) {
			// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
			if (status == TextToSpeech.SUCCESS) {
				// Set preferred language to US english.
				// Note that a language may not be available, and the result
				// will indicate this.
				Locale loc = new Locale(locale);
				int result = srv.mTts.setLanguage(loc);
				// Try this someday for some interesting results.
				// int result mTts.setLanguage(Locale.FRANCE);
				if (result == TextToSpeech.LANG_MISSING_DATA
						|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
					// Lanuage data is missing or the language is not supported.
					Log.e(TAG, "Language is not available.");
				} else {
					// Check the documentation for other possible result codes.
					// For example, the language may be available for the
					// locale,
					// but not for the specified country and variant.

					// The TTS engine has been successfully initialized.
					// Allow the user to press the button for the app to speak
					// again.
					// mAgainButton.setEnabled(true);
					// Greet the user.

				}
			} else {
				// Initialization failed.
				Log.e(TAG, "Could not initialize TextToSpeech.");
			}
		}

		public void run() {
			ServerSocket serverSocket=null;
			try {
				Log.d(TAG, "Server is up");
				serverSocket = new ServerSocket(SERVERPORT);
				serverSocket.setSoTimeout(1000);
				
				while (running) {
					Socket client = null;
					try {
					client = serverSocket.accept();
					Log.d(TAG, "Incoming connection.");
					} catch (Exception e) {
						//Timeout on accept
					}
					if (client != null)
					{
					try {
						BufferedReader in = new BufferedReader(
								new InputStreamReader(client.getInputStream()));
						String str = in.readLine();
						Log.d(TAG, "Got text: '" + str + "'");
						srv.mTts.speak(str, TextToSpeech.QUEUE_ADD, // Add text
																	// to queue.
								null);
					} catch (Exception e) {
						Log.e(TAG, "Got exception: ", e);
					} finally {
						client.close();
						Log.d(TAG, "Connection closed");
					}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Server error: ", e);
				return;
			}
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Toast.makeText(this, "NetTTS service created", Toast.LENGTH_LONG)
				.show();
		Log.d(TAG, "onCreate");
		
		Context c = getApplicationContext();
		prefMgr = c.getSharedPreferences("NetTTS", MODE_PRIVATE);
		srv.SERVERPORT = Integer.parseInt(prefMgr.getString("port", "8080"));
		this.locale = prefMgr.getString("locale", "ru_RU");
		srv.mTts = new TextToSpeech(this, (OnInitListener) this.srv.onInit);
		// player = MediaPlayer.create(this, R.raw.braincandy);
		// player.setLooping(false); // Set looping
	}

	public void say(String text) {
		srv.mTts.speak(text, TextToSpeech.QUEUE_FLUSH, // Drop all pending
														// entries in the
														// playback queue.
				null);
	}

	public void shutup() {
		srv.mTts.speak("Все, молчу.", TextToSpeech.QUEUE_FLUSH, // Drop all
																// pending
																// entries in
																// the playback
																// queue.
				null);
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "Stopping server", Toast.LENGTH_LONG)
				.show();
		Log.d(TAG, "Stopping service");
		try {
			srv.running=false;
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Thread doesn't want to join");
			e.printStackTrace();
		}
		Log.d(TAG, "Service stopped, ok");	
	}

	@Override
	public void onStart(Intent intent, int startid) {

		Log.d(TAG, "Starting Necromant's NetTTS service");
		if (started) {
			Log.d(TAG, "Looks like we have already been started.");
			return;
		} else {
			t.start();
			Toast.makeText(this, "NetTTS service started", Toast.LENGTH_LONG)
					.show();
			started = true;
		}

		// player.start();
	}
}
