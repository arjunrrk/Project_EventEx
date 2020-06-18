package com.mail.eventex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Events extends AppCompatActivity implements CustomAdapter.OnEventListener{

    private ActionBar toolbar;
    long backPressedTime;
    Toast backToast;
    DatabaseHelper databaseHelper;
    ArrayList<String> subject;
    CustomAdapter customAdapter;
    ProgressDialog progressDialog;
    int queue_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        toolbar = getSupportActionBar();
        RecyclerView recyclerView = findViewById((R.id.recyclerView));

        databaseHelper = new DatabaseHelper(this);
        subject = new ArrayList<>();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading.....");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        HistorySetup historySetup = new HistorySetup();
        historySetup.execute();

        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }


        storeDataArray();

        customAdapter = new CustomAdapter(Events.this,subject,this);
        recyclerView.setAdapter(customAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(Events.this));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_events);
        toolbar.setTitle("Events Collection");
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_events:
                        toolbar.setTitle("Events Collection");
                        return true;
                    case R.id.navigation_starred:
                        toolbar.setTitle("Starred Events");
                        startActivity(new Intent(getApplicationContext(), StarredEvents.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.navigation_week:
                        toolbar.setTitle("Today's Events");
                        startActivity(new Intent(getApplicationContext(), WeekEvents.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private class HistorySetup extends AsyncTask <Void,Void,Void>{
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Events.this);
        @Override
        protected Void doInBackground(Void... voids) {

            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            assert account != null;
            Cursor historyCursor = db.rawQuery("SELECT * from Authorization WHERE email_id = ?", new String[]{account.getEmail()});
            historyCursor.moveToFirst();
            String history_id_str = historyCursor.getString(3);
            if(!history_id_str.equals("null")){
                String accessToken = historyCursor.getString(1);
                String refreshToken = historyCursor.getString(2);

                Log.e("TAG", "doInBackground: "+accessToken+refreshToken );

                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(new NetHttpTransport())
                        .setJsonFactory(JacksonFactory.getDefaultInstance())
                        .setClientSecrets("1008084494477-unr2r7s2q8sfefqluar4674ohftjjf3q.apps.googleusercontent.com","e9QtThZa_JzaG1UiEmCMEpq7")
                        .build();
                credential.setAccessToken(accessToken);
                credential.setExpiresInSeconds(3600L);
                credential.setRefreshToken(refreshToken);

                Gmail service = new Gmail.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
                BigInteger history_id = new BigInteger(history_id_str);

                Log.e("Gmail", "History ID: "+history_id.toString());

                List<History> histories = new ArrayList<History>();
                try {
                    List<String> labelIds = new ArrayList<>();
                    labelIds.add("INBOX");
                    ListHistoryResponse response = service.users().history().list("me").setLabelId("INBOX").setStartHistoryId(history_id).execute();
                    ListMessagesResponse id_list = service.users().messages().list("me").setLabelIds(labelIds).setQ("category:primary").execute();
                    List<Message> messages = new ArrayList<>();
                    List<String> msgIds = new ArrayList<>();
                    if (id_list.getMessages() != null) {
                        messages.addAll(id_list.getMessages());
                        for (Message mssage : messages){
                            msgIds.add(mssage.getId());
                        }
                    }

                    ContentValues historyUpdate = new ContentValues();
                    historyUpdate.put("history_id",response.getHistoryId().toString());
                    db = databaseHelper.getWritableDatabase();
                    db.update("Authorization",historyUpdate,"email_id = ?",new String[]{account.getEmail()});
                    db.close();
                    while (response.getHistory() != null) {
                        histories.addAll(response.getHistory());
                        if (response.getNextPageToken() != null) {
                            String pageToken = response.getNextPageToken();
                            response = service.users().history().list("me").setPageToken(pageToken).setStartHistoryId(history_id).setLabelId("INBOX").execute();
                        }
                        else {
                            break;
                        }
                    }
                    if(!histories.isEmpty()){
                        for (History history : histories) {
                            if(history.getMessagesAdded()!=null){
                                for (final HistoryMessageAdded history_message : history.getMessagesAdded()){
                                    if(msgIds.contains(history_message.getMessage().getId())){
                                        Message message = service.users().messages().get("me", history_message.getMessage().getId()).execute();

                                        JSONObject body = new JSONObject(message.toString());
                                        RequestQueue requestQueue = Volley.newRequestQueue(Events.this);
                                        String URL = "http://10.0.2.2:5000/predict";

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
                                                                String message_ID = history_message.getMessage().getId();
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
                            }
                        }
                    }

                }
                catch (IOException | JSONException e) {
                    Log.e("History API", "Error");
                    progressDialog.dismiss();
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    void storeDataArray(){
        TextView empty_mail_text = findViewById(R.id.empty_mail_text);
        ImageView empty_mail = findViewById(R.id.empty_mail);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        assert account != null;
        String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
        Cursor cursor = databaseHelper.readData(table_name);
        if(cursor != null){
            if(cursor.getCount() != 0){
                empty_mail.setVisibility(View.GONE);
                empty_mail_text.setVisibility(View.GONE);
                while(cursor.moveToNext()){
                    subject.add(cursor.getString(1));
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile_icon) {
            Intent intent = new Intent(Events.this, Profile.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(backPressedTime + 3000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }
        else{
            backToast = Toast.makeText(getBaseContext(), "Press again to exit the app", Toast.LENGTH_SHORT);
            View view = backToast.getView();
            view.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN);
            TextView text = view.findViewById(android.R.id.message);
            text.setTextColor(Color.WHITE);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }



    @Override
    public void onEventClick(int position) {
        Intent intent = new Intent(this, EventDetails.class);
        intent.putExtra("selected_event",position);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult: "+requestCode+resultCode );
        Intent intent = new Intent(Events.this, Events.class);
        startActivity(intent);
        finish();
    }
}
