package com.mygym;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUp extends Activity {

	private String TAG = "SignUp";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);

		TextView txt = (TextView) findViewById(R.id.button1);  
		Typeface font = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");  
		txt.setTypeface(font);  

		GetKeyHash();

		LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
		authButton.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(FacebookException error) {
				Log.i(TAG, "Error " + error.getMessage());
			}
		});
		// set permission list, Don't foeget to add email
		authButton.setReadPermissions(Arrays.asList("basic_info","email"));
		// session state call back event
		authButton.setSessionStatusCallback(new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (session.isOpened()) {
					Log.i(TAG,"Access Token"+ session.getAccessToken());
					Request.executeMeRequestAsync(session,
							new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user,Response response) {
							if (user != null) { 
								Log.i(TAG,"User ID "+ user.getId());
								Log.i(TAG,"Email "+ user.asMap().get("email"));
								//lblEmail.setText(user.asMap().get("email").toString());
							}
						}
					});
				}
			}
		});	
	}

	private final static String SERVICE_URI = "http://192.168.0.4/MyGymWcfService/service.svc";

	private class HttpGetService extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {
				DefaultHttpClient client = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse execute = client.execute(httpGet);
					InputStream content = execute.getEntity().getContent();

					BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
					String s = "";
					while ((s = buffer.readLine()) != null) {
						response += s;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}

		protected void onPreExecute(){
			//progressDialog = ProgressDialog.show(context, "Requisição", "Esperando resposta do servidor...", false, false);
		}

		@Override
		protected void onPostExecute(String result) {

			Type collectionType = new TypeToken<Collection<User>>(){}.getType();
			Collection<User> obj =  new Gson().fromJson(result, collectionType);

			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, obj.iterator().next().Name , duration);
			toast.show();
		}
	}

	private class HttpPostService extends AsyncTask<String, Void, String> {

		public String convertStreamToString(InputStream is) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			 
			String line = null;
			try {
			  while ((line = reader.readLine()) != null) {
			  sb.append(line + "\n");
			  }
			} catch (IOException e) {
			  e.printStackTrace();
			} finally {
			  try {
			   is.close();
			  } catch (IOException e) {
			   e.printStackTrace();
			  }
			}
			return sb.toString();
			}
		
		String postRequest(String url) throws Exception
		{
			HttpPost request = new HttpPost(url);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");

			Authentication auth = new Authentication();
			auth.Login = ((EditText)findViewById(R.id.txtEmailAddress)).getText().toString();
			auth.Email = ((EditText)findViewById(R.id.txtEmailAddress)).getText().toString();
			auth.Password = ((EditText)findViewById(R.id.txtPassword)).getText().toString();

			Configuration configuration = new Configuration();
			configuration.PublicProfile = false;
			configuration.FriendlyMessages = false;
			configuration.PublicRoutine = false;

			User user = new User();
			user.Authentications = new ArrayList<Authentication>();
			user.Authentications.add(auth);	
			user.Configuration = configuration;

			Gson gson = new Gson();
			//converte objeto para json
			String json = gson.toJson(user);
			StringEntity entity = new StringEntity(json);
			//inclui entidade na requisicao
			request.setEntity(entity);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse response = httpClient.execute(request);
			HttpEntity responseEntity = response.getEntity();
			
			Log.d("WebInvoke", "Saving : " + response.getStatusLine().getStatusCode());
			
			InputStream content = response.getEntity().getContent();

			String r = "";
			
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			while ((s = buffer.readLine()) != null) {
				r += s;}

			User user2 =  new Gson().fromJson(r, User.class);
			
			return responseEntity.toString();
		}
		
		

		@Override
		protected String doInBackground(String... urls) {
			String response = "";
			for (String url : urls) {

				try {
					response = postRequest(url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			return response;
		}

		protected void onPreExecute(){
			//progressDialog = ProgressDialog.show(context, "Requisição", "Esperando resposta do servidor...", false, false);
		}

		@Override
		protected void onPostExecute(String result) {			
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, result , duration);
			toast.show();
		}
	}

	public void onClick(View view) {
		HttpPostService task = new HttpPostService();
		task.execute(new String[] { SERVICE_URI + "/SaveUser" });
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void GetKeyHash() {
		// this code get key hash to facebook enable
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.mygym", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.e("MY KEY HASH:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}  
}
