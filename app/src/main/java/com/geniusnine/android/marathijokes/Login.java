package com.geniusnine.android.marathijokes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.Arrays;

public class Login extends AppCompatActivity {

    //Firebase and facebook variables
    private LoginButton facebookLoginButton;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;

    //User for Facebook data
    private User user ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);
        startAuthentication();
    }
    private void startAuthentication(){
        callbackManager = CallbackManager.Factory.create();
        firebaseAuth = FirebaseAuth.getInstance();
        facebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_friends", "user_birthday", "user_location"));
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {


                Log.e("LoginActivity: ", "call back success found");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.e("LoginActivity: ", "Graph method called inside register call back");
                                Log.e("response: ", response + "");
                                try {
                                    
                                    } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                //Toast.makeText(Login.this, "Welcome " + user.name, Toast.LENGTH_LONG).show();


                            }

                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.e("LoginActivity: ", "Serious Error token seems not valid " );
                Log.e("Important --------", error.toString());

            }

        });


    }
    private void handleFacebookAccessToken(AccessToken token){
        Log.d("FB:", "handleFacebookAccessToken:" + token);
        Log.e("LoginActivity:", "Handle token process statted");
        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {




                if (!task.isSuccessful()) {



                    Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }

                else {
                    //updateUserProfile();
                    Log.e("LoginActivity:", "Logged in and directing to main activity");
                    Intent loginIntent = new Intent(Login.this, MainActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();



                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("LoginActivity:", "On activity result called");
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void updateUserProfile()
    {
        try{
            String user_id = firebaseAuth.getCurrentUser().getUid();


            //current_user_db.child("FacebookId").setValue(user.facebookID);
            //current_user_db.child("Email").setValue(user.email);
            //current_user_db.child("Name").setValue(user.userName);
            //current_user_db.child("Gender").setValue(user.gender);

            //SyncContacts();


        }
        catch(Exception ex)
        {
            Log.e("Userprofile------", ex.toString());
        }

    }
    public class User {


        public String userName;

        public String email;

        public String facebookID;

        public String gender;

        public String ageRange;
        public String link;
        public String userLocation;
        public String locale;


    }

}