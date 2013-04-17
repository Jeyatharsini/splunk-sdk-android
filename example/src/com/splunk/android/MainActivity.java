package com.splunk.android;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.splunk.android.R;
import com.splunk.sdk.ResultsReader;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.view.View.OnClickListener;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity {

	ImageView userImage;
	TextView userName;
	TextView msg;
	ProgressBar progress;
	SearchTask searchtask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		setContentView(R.layout.activity_main);

		userImage = (ImageView) findViewById(R.id.twitter_profile_pic);
		userName = (TextView) findViewById(R.id.twitter_profile_user);
		msg = (TextView) findViewById(R.id.twitter_msg);
		progress = (ProgressBar) findViewById(R.id.searchProgress);
		progress.setVisibility(ProgressBar.INVISIBLE);

		userImage.setImageResource(R.drawable.splunkpowered);
		userName.setText(R.string.splunk_twitter_user_name);
		msg.setText(R.string.rest_wicked);

		Splunk.setHost("yourhost.com");
		Splunk.setPassword("password");
		Splunk.setPort(8089);
		Splunk.setUsername("admin");

		addListenerOnButton();

		startService(new Intent(this, LoggingService.class));

	}

	public void addListenerOnButton() {

		Button button = (Button) findViewById(R.id.splunkbutton);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View button) {

				if (searchtask == null) {

					searchtask = new SearchTask();

					searchtask
							.execute("index=spring sourcetype=twitter-feed | fields + from_user,imageURL,text");
					((Button) button).setText(R.string.splunk_button_stop);
					progress.setVisibility(ProgressBar.VISIBLE);
				} else {
					searchtask.cancel(true);
					((Button) button).setText(R.string.splunk_button_start);
					searchtask = null;

					userImage.setImageResource(R.drawable.splunkpowered);
					msg.setText(R.string.rest_wicked);
					userName.setText(R.string.splunk_twitter_user_name);
					progress.setVisibility(ProgressBar.INVISIBLE);

				}

			}

		});

	}

	public class User {

		Drawable drawable;
		String name;
		String msg;

	}

	class SearchTask extends AsyncTask<String, User, Object> implements
			SplunkResultsListener {

		List<User> users;

		protected void onProgressUpdate(User... u) {

			try {

				userImage.setImageDrawable(u[0].drawable);
				userName.setText(u[0].name);
				msg.setText(u[0].msg);
			} catch (Exception e) {

			}

		}

		public void processResults(ResultsReader resultsReader) {

			try {
				HashMap<String, String> map;
				List<User> items = new ArrayList<User>();
				while ((map = resultsReader.getNextEvent()) != null) {

					try {
						User u = new User();

						try {
							u.drawable = drawableFromUrl(map.get("imageURL"));
						} catch (Exception e) {

						}
						u.name = map.get("from_user");
						u.msg = map.get("text");

						items.add(0, u);

					} catch (Exception e) {

					}

				}
				users.addAll(0, items);
				resultsReader.close();
			} catch (Exception e) {

			}
		}

		class Poster extends Thread {

			public void run() {

				while (searchtask != null) {
					if (!users.isEmpty()) {

						publishProgress(users.remove(0));
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {

						}
					}
				}
			}
		}

		@Override
		protected Object doInBackground(String... params) {

			users = new ArrayList<User>();
			new Poster().start();

			Splunk.getInstance().realtimeSearch(params[0], this);

			return null;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private Drawable drawableFromUrl(String url) throws Exception {

		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		connection.setConnectTimeout(5000);
		connection.connect();

		InputStream input = connection.getInputStream();

		Bitmap bitmap = BitmapFactory.decodeStream(input);
		return new BitmapDrawable(getResources(), bitmap);
	}

}
