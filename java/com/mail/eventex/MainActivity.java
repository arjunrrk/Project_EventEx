package com.mail.eventex;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    SignInButton signin;
    int RC_SIGN_IN = 0;
    long backPressedTime;
    Toast backToast;
    DatabaseHelper databaseHelper;
    GoogleSignInClient mGoogleSignInClient;
    private ProgressDialog progressDialog;
    int queue_count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_main);

        signin = findViewById(R.id.sign_in_button);
        signin.setSize(SignInButton.SIZE_WIDE);
        signin.setColorScheme(SignInButton.COLOR_DARK);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                    // ...
                }

            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(GmailScopes.GMAIL_READONLY),new Scope(CalendarScopes.CALENDAR))
                .requestServerAuthCode("1008084494477-unr2r7s2q8sfefqluar4674ohftjjf3q.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Initial account setup");
            progressDialog.setMessage("Please wait.....loading");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.show();

            BackgroundTask backgroundTask = new BackgroundTask();
            backgroundTask.execute();

            // Signed in successfully, show authenticated UI.
//            Intent intent = new Intent(MainActivity.this, Mails.class);
//            startActivity(intent);
        }
        catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Network connection failed", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class BackgroundTask extends AsyncTask<Void, Void, Void>{
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);


        @Override
        protected Void doInBackground(Void... voids) {

            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
            db.execSQL("CREATE TABLE IF NOT EXISTS "+table_name+" (msg_id TEXT PRIMARY KEY, subject TEXT, date TEXT, time TEXT, venue TEXT, link TEXT, starred TEXT DEFAULT 'false', calendar TEXT DEFAULT 'false')");
            final long count = DatabaseUtils.queryNumEntries(db,table_name);
            db.close();
            if (count == 0){
                String authCode = account.getServerAuthCode();
                GoogleTokenResponse tokenResponse = null;
                try {
                    tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                            new NetHttpTransport(),
                            JacksonFactory.getDefaultInstance(),
                            "https://oauth2.googleapis.com/token",
                            "1008084494477-unr2r7s2q8sfefqluar4674ohftjjf3q.apps.googleusercontent.com",
                            "e9QtThZa_JzaG1UiEmCMEpq7",
                            authCode,
                            "http://localhost:50659/")
                            .execute();
                } catch (IOException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
                assert tokenResponse != null;
                String accessToken = tokenResponse.getAccessToken();
                String refreshToken = tokenResponse.getRefreshToken();

                Log.e("Refresh Token", "Response: "+tokenResponse.getRefreshToken());

                db = databaseHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put("email_id",account.getEmail());
                contentValues.put("access_token",accessToken);
                contentValues.put("refresh_token",refreshToken);
                db.insertWithOnConflict("Authorization", null, contentValues,SQLiteDatabase.CONFLICT_IGNORE);
                db.close();


                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(new NetHttpTransport())
                        .setJsonFactory(JacksonFactory.getDefaultInstance())
                        .setClientSecrets("1008084494477-unr2r7s2q8sfefqluar4674ohftjjf3q.apps.googleusercontent.com","e9QtThZa_JzaG1UiEmCMEpq7")
                        .build();
                credential.setAccessToken(accessToken);
                credential.setExpiresInSeconds(3600L);
                credential.setRefreshToken(refreshToken);

                Gmail service = new Gmail.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
                List<String> labelIds = new ArrayList<>();
                labelIds.add("INBOX");
                try {
                    ListMessagesResponse id_list = service.users().messages().list("me").setLabelIds(labelIds).setQ("category:primary").execute();
                    RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                    int counter = 0;
                    for (final Message msg : id_list.getMessages()){
                        counter++;
                        if(counter == 51){
                            break;
                        }

                        Message message = service.users().messages().get("me", msg.getId()).execute();
                        JSONObject body = new JSONObject(message.toString());

                        if(counter==1){
                            SQLiteDatabase dbase = databaseHelper.getWritableDatabase();
                            ContentValues history = new ContentValues();
                            history.put("history_id",message.getHistoryId().toString());
                            dbase.update("Authorization",history,"email_id = ?",new String[]{account.getEmail()});
                            dbase.close();
                        }

                        String URL = "http://10.0.2.2:5000/predict";
//                        String URL = "http://192.168.0.104:5000/predict";
                        JsonObjectRequest request = new JsonObjectRequest(
                                Request.Method.POST,
                                URL,
                                body,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        queue_count--;
                                        Log.e("TAG", "onResponse: "+queue_count);
                                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
                                        try {
                                            int prediction = response.getInt("prediction");
                                            if (prediction == 1) {

                                                String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
                                                String message_ID = msg.getId();
                                                String subject = response.getString("subject");
                                                String date = response.getString("date");
                                                String time = response.getString("time");
                                                String venue = response.getString("venue");
                                                String link = response.getString("link");

                                                ContentValues contentValues = new ContentValues();
                                                contentValues.put("msg_id",message_ID);
                                                contentValues.put("subject",subject);
                                                contentValues.put("date",date);
                                                contentValues.put("time",time);
                                                contentValues.put("venue",venue);
                                                contentValues.put("link",link);
                                                db.insertWithOnConflict(table_name, null, contentValues,SQLiteDatabase.CONFLICT_IGNORE);
                                            }
                                        }
                                        catch (JSONException e) {
                                            db.close();
                                            e.printStackTrace();
                                        }

                                        if(queue_count == 0 && progressDialog.isShowing()) {
                                            db.close();
                                            progressDialog.dismiss();
                                            Intent intent = new Intent(MainActivity.this, Events.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("TAG", "onErrorResponse: ");
                                        error.printStackTrace();
                                        if(queue_count != 0 && progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                            Toast.makeText(MainActivity.this, "Database connectivity has failed", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(MainActivity.this, Events.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                }
                        );
                        request.setRetryPolicy(new DefaultRetryPolicy(0,DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        requestQueue.add(request);
                        queue_count++;
                        Log.e("TAG", "doInBackground: "+queue_count);
                    }
                }
                catch (IOException | JSONException e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
             }
            else{
                progressDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, Events.class);
                startActivity(intent);
                finish();
            }
            return null;
        }
    }






    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            Intent intent = new Intent(MainActivity.this, Events.class);
            startActivity(intent);
            finish();
        }


    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 3000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        else{
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            View view = backToast.getView();
            view.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
            TextView text = view.findViewById(android.R.id.message);
            text.setTextColor(Color.WHITE);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
