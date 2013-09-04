package com.mygym;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.LoginButton.OnErrorListener;
import android.app.Activity;
import android.view.Menu;

public class MainActivity extends Activity {

	 private String TAG = "MainActivity";
	 private TextView lblEmail;
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.activity_main);
	  lblEmail = (TextView) findViewById(R.id.lblEmail);
	  
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
	                               lblEmail.setText(user.asMap().get("email").toString());
	                              }
	                          }
	                      });
	          }
	    
	   }
	  });
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
