package org.ncrmnt.nettts;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
	private String authtag = "";
	SharedPreferences prefMgr;

	public class TCPServer implements Runnable, TextToSpeech.OnInitListener {
		public int SERVERPORT = 4444;
		public TextToSpeech mTts;
		boolean running = true;

		// private OnInitListener onInit;

		public void setLanguage() {
			Locale loc = new Locale(locale);
			int result = srv.mTts.setLanguage(loc);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available");
			} else
				Log.d(TAG, "language switch successful");
		}

		public void onInit(int status) {
			Log.d(TAG, "onInit: " + locale);
			// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
			if (status == TextToSpeech.SUCCESS) {
				setLanguage();
			} else {
				// Initialization failed.
				throw new RuntimeException("Could not initialize TextToSpeech.");
			}
		}

		public void run() {
			ServerSocket serverSocket = null;
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
						// Timeout on accept
					}
					if (client != null) {
						try {
							int mode = TextToSpeech.QUEUE_ADD;
							int i = 0;

							BufferedReader in = new BufferedReader(
									new InputStreamReader(
											client.getInputStream()));
							String str = in.readLine();
							/* check for auth tag */
							if (!authtag.equals("")) {
								if (authtag.equals(str))
									str = in.readLine();
								else
									str = "";
							}

							Log.d(TAG, "Got text: '" + str + "'");
							String[] tokens = str.split(" ");
							if (tokens[i].equals("/volume") && tokens.length >= 2) {
								Log.d(TAG, "Requested a volume change to: "
										+ tokens[i + 1]);
								int volume = Integer.parseInt(tokens[i + 1]);
								AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
								int amStreamMusicMaxVol = am.getStreamMaxVolume(am.STREAM_MUSIC);
								am.setStreamVolume(am.STREAM_MUSIC, amStreamMusicMaxVol * volume / 100, 0);
								str = in.readLine();
								tokens = str.split(" ");
							}
							if (tokens[i].equals("/tone") && tokens.length >= 4) {
								Log.d(TAG, "Requested a tone.");
								int volume = Integer.parseInt(tokens[i + 1]);
								int tone = Integer.parseInt(tokens[i + 2]);
								int duration = Integer.parseInt(tokens[i + 3]);
								ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);
								tg.startTone(tone, duration);
								str = "";
							}
							if (tokens[i].equals("/setlang")) {
								Log.d(TAG, "Requested a language switch to: "
										+ tokens[i + 1]);
								locale = tokens[i + 1];
								setLanguage();
								str="";
							} else if (tokens[i].equals("/shutup")) {
								mode = TextToSpeech.QUEUE_FLUSH;
								str = str.replace("/shutup", "");
							}
							
							if (!str.equals(""))
								srv.mTts.speak(str, mode, null);

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
				mTts.shutdown();
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

		Context c = getApplicationContext();
		prefMgr = c.getSharedPreferences("NetTTS", MODE_PRIVATE);
		srv.SERVERPORT = Integer.parseInt(prefMgr.getString("port", "4444"));
		this.locale = prefMgr.getString("locale", "ru_RU");
		this.authtag = prefMgr.getString("authtag", "");
		Log.d(TAG, "Creating service, locale is: " + this.locale);
		srv.mTts = new TextToSpeech(this, (OnInitListener) this.srv);
	}


	@Override
	public void onDestroy() {
		Toast.makeText(this, "Stopping server", Toast.LENGTH_LONG).show();
		Log.d(TAG, "Stopping service");
		try {
			srv.running = false;
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
