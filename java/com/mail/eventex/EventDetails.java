package com.mail.eventex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class EventDetails extends AppCompatActivity {

    Toolbar toolbar;
    DatabaseHelper databaseHelper;
    ArrayList<String> msg_id;
    ArrayList<String> subject;
    ArrayList<String> date;
    ArrayList<String> time;
    ArrayList<String> venue;
    ArrayList<String> link;
    ArrayList<String> starred;
    ArrayList<String> calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);


        databaseHelper = new DatabaseHelper(this);
        final CheckBox starred_icon = findViewById(R.id.starred_button);
        final int position = getIntent().getIntExtra("selected_event",0);

        msg_id = new ArrayList<>();
        subject = new ArrayList<>();
        date = new ArrayList<>();
        time = new ArrayList<>();
        venue = new ArrayList<>();
        link = new ArrayList<>();
        starred = new ArrayList<>();
        calendar = new ArrayList<>();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        assert account != null;
        final String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
        Cursor cursor = databaseHelper.readData(table_name);
        if(cursor.getCount() != 0){
            while(cursor.moveToNext()){
                msg_id.add(cursor.getString(0));
                subject.add(cursor.getString(1));
                date.add(cursor.getString(2));
                time.add(cursor.getString(3));
                venue.add(cursor.getString(4));
                link.add(cursor.getString(5));
                starred.add(cursor.getString(6));
                calendar.add(cursor.getString(7));
            }
        }

        toolbar = findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.icon_back);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            @Override
            public void onClick(View v) {
                if(starred_icon.isChecked()){
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put("starred","true");
//                    db.update(table_name,contentValues,"msg_id =?",new String[]{msg_id.get(position)});
                    Log.e("Starred message", "onClick: ");
                }
                else{
//                    ContentValues contentValues = new ContentValues();
//                    contentValues.put("starred","false");
//                    db.update(table_name,contentValues,"msg_id =?",new String[]{msg_id.get(position)});
                    Log.e("Not starred anymore", "onClick: ");
                }
                finish();
            }
        });

        TextView event_subject = findViewById(R.id.event_subject);
        TextView event_date = findViewById(R.id.event_date);
        TextView event_time = findViewById(R.id.event_time);
        TextView event_venue = findViewById(R.id.event_venue);
        TextView event_link = findViewById(R.id.event_link);
        View rel_link = findViewById(R.id.rel_link);

        TextView google_calendar_text = findViewById(R.id.google_calendar_text);

        if(starred.get(position).equals("true")) {
            starred_icon.setChecked(true);
        }

        if(!calendar.get(position).equals("false")){
            google_calendar_text.setText("Open Calendar");
            google_calendar_text.setTextSize(TypedValue.COMPLEX_UNIT_SP,(float) 23.25);

        }

        event_subject.setText(String.valueOf(subject.get(position)));
        event_date.setText(String.valueOf(date.get(position)));
        event_time.setText(String.valueOf(time.get(position)));
        event_venue.setText(String.valueOf(venue.get(position)));
        if(link.get(position).length() != 0){
            event_link.setText(String.valueOf(link.get(position)));
            rel_link.setVisibility(View.VISIBLE);
        }

        View gmail = findViewById(R.id.rel_gmail);
        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gmail_Client = new Intent(Intent.ACTION_VIEW);
                gmail_Client.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
                startActivity(gmail_Client);
            }
        });

        View google_calendar = findViewById(R.id.rel_google_calendar);
        google_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(calendar.get(position).equals("false")){
                    BackgroundTask backgroundTask = new BackgroundTask();
                    backgroundTask.execute();
                }
                else{
                    Intent google_calendar_client = new Intent(Intent.ACTION_VIEW, Uri.parse(calendar.get(position)));
//                    google_calendar_client.setClassName("com.google.android.calendar", "com.android.calendar.LaunchActivity");
                    startActivity(google_calendar_client);
                }
            }
        });

        starred_icon.setOnClickListener(new View.OnClickListener() {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            @Override
            public void onClick(View v) {
                if(starred_icon.isChecked()){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("starred","true");
                    db.update(table_name,contentValues,"msg_id = ?",new String[]{msg_id.get(position)});
                    db.close();
                    Log.e("Starred message", "onClick: ");
                }
                else{
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("starred","false");
                    db.update(table_name,contentValues,"msg_id = ?",new String[]{msg_id.get(position)});
                    db.close();
                    Log.e("Not starred anymore", "onClick: ");
                }
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        int position = getIntent().getIntExtra("selected_event",0);
        @Override
        protected Void doInBackground(Void... voids) {

            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(EventDetails.this);
            assert account != null;
            String email_id = account.getEmail();
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * from Authorization WHERE email_id = ?", new String[]{email_id});
            cursor.moveToFirst();
            String accessToken = cursor.getString(1);
            db.close();
            Log.e("TAG", "doInBackground: " + accessToken);
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            Calendar service = new Calendar.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential).build();
            Log.e("Google Calendar: ", "Building service successful");

            try {
                Event event = new Event()
                        .setSummary(subject.get(position))
                        .setLocation(venue.get(position));

                SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date eventDate = inputFormat.parse(date.get(position));
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                String startDate = outputFormat.format(eventDate);

                DateTime startDateTime = new DateTime((startDate));
                EventDateTime start = new EventDateTime()
                        .setDate(startDateTime);
                event.setStart(start);
                event.setEnd(start);
                if (!time.get(position).equals("Not Specified")) {
                    event.setDescription("This event is scheduled to begin at "+time.get(position));
                }
                else {
                    event.setDescription("This event is either a full day event or does not have a scheduled start time.");
                }
                EventReminder[] reminderOverrides = new EventReminder[]{
                        new EventReminder().setMethod("email").setMinutes(24 * 60),
                        new EventReminder().setMethod("popup").setMinutes(12 * 60)
                };
                Event.Reminders reminders = new Event.Reminders()
                        .setUseDefault(false)
                        .setOverrides(Arrays.asList(reminderOverrides));
                event.setReminders(reminders);

                if(!link.get(position).equals("") && !link.get(position).equals("The link is in your mail")){
                    Event.Source source = new Event.Source()
                            .setUrl(link.get(position));
                    event.setSource(source);
                }


                Log.e("TAG", "Event JSON: "+event );
                String calendarId = "primary";
                event = service.events().insert(calendarId, event).execute();
                Log.e("Google Calendar ", "Event link: "+event.getHtmlLink() );
                db = databaseHelper.getWritableDatabase();
                String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
                ContentValues contentValues = new ContentValues();
                contentValues.put("calendar",event.getHtmlLink());
                db.update(table_name,contentValues,"msg_id = ?",new String[]{msg_id.get(position)});
                db.close();
            }
            catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            Toast.makeText(EventDetails.this, "Event added to Google Calendar", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete_button, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_icon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EventDetails.this);
            builder.setTitle(R.string.app_name);
            builder.setMessage("You cannot undo your deletion");
            builder.setIcon(R.mipmap.ic_launcher_round);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(EventDetails.this);
                    assert account != null;
                    String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s", "");
                    databaseHelper = new DatabaseHelper(EventDetails.this);
                    SQLiteDatabase db = databaseHelper.getWritableDatabase();
                    int position = getIntent().getIntExtra("selected_event", 0);

                    db.delete(table_name, "msg_id = ?", new String[]{msg_id.get(position)});
                    finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onOptionsItemSelected(item);
    }
}
