package com.mail.eventex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class WeekEvents extends AppCompatActivity implements WeekAdapter.OnWeekEventListener{

    private ActionBar toolbar;
    long backPressedTime;
    Toast backToast;
    DatabaseHelper databaseHelper;
    ArrayList<String> subject;
    ArrayList<String> day;
    WeekAdapter weekAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_week);

        toolbar = getSupportActionBar();

        RecyclerView recyclerView = findViewById((R.id.recyclerView));

        databaseHelper = new DatabaseHelper(this);

        subject = new ArrayList<>();
        day = new ArrayList<>();

        try {
            storeDataArray();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        weekAdapter = new WeekAdapter(WeekEvents.this,subject,day,this);
        recyclerView.setAdapter(weekAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(WeekEvents.this));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_week);
        toolbar.setTitle("This Week's Events");
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_events:
                        toolbar.setTitle("Events Collection");
                        startActivity(new Intent(getApplicationContext(), Events.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.navigation_starred:
                        toolbar.setTitle("Starred Events");
                        startActivity(new Intent(getApplicationContext(), StarredEvents.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                    case R.id.navigation_week:
                        toolbar.setTitle("This Week's Events");
                        return true;
                }
                return false;
            }
        });
    }

    void storeDataArray() throws ParseException {
        TextView empty_starred_text = findViewById(R.id.empty_event_text);
        ImageView empty_starred = findViewById(R.id.empty_event);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        assert account != null;
        String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
        Cursor cursor = databaseHelper.readDateSortedData(table_name);
        if(cursor != null){
            if(cursor.getCount() != 0){
                while(cursor.moveToNext()){
                    if(thisWeekOrNot(cursor.getString(2))){
                        SimpleDateFormat inFormat = new SimpleDateFormat("dd-MM-yyyy");
                        Date date = inFormat.parse(cursor.getString((2)));
                        SimpleDateFormat outFormat = new SimpleDateFormat("EEEE");
                        String goal = outFormat.format(date);
                        subject.add(cursor.getString(1));
                        day.add(goal.substring(0,3));
                    }
                }
                if(subject.size()!=0){
                    empty_starred.setVisibility(View.GONE);
                    empty_starred_text.setVisibility(View.GONE);
                }
            }
        }
    }
    boolean thisWeekOrNot(String event_date) throws ParseException {
//        Calendar c=Calendar.getInstance();
//        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
//        DateFormat df=new SimpleDateFormat("dd-MM-yyyy");
//        String last_monday = df.format(c.getTime());
//        c.add(Calendar.DATE,7);
//        String next_monday = df.format(c.getTime());
//        if (last_monday.compareTo(event_date) <= 0 && event_date.compareTo(next_monday) < 0){
//            return true;
//        }
//        return false;
        Calendar currentCalendar = Calendar.getInstance();
        int week = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int year = currentCalendar.get(Calendar.YEAR);
        Calendar targetCalendar = Calendar.getInstance();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date inputDate = inputFormat.parse(event_date);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        String outputDate = outputFormat.format(inputDate);
        Date final_date = outputFormat.parse(outputDate);
        targetCalendar.setTime(final_date);
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);
        long millis = System.currentTimeMillis();
        java.sql.Date current_date = new java.sql.Date(millis);
        if(week==targetWeek && year==targetYear && (final_date.after(current_date) || final_date.after(current_date)==final_date.before(current_date))){
            return true;
        }
        return false;
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
            Intent intent = new Intent(WeekEvents.this, Profile.class);
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
    public void onWeekEventClick(int position) {
        Intent intent = new Intent(this, WeekEventDetails.class);
        intent.putExtra("selected_event",position);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult: "+requestCode+resultCode );
        Intent intent = new Intent(WeekEvents.this, WeekEvents.class);
        startActivity(intent);
        finish();
    }

}
