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

import java.util.ArrayList;
import java.util.Objects;

public class StarredEvents extends AppCompatActivity implements StarredAdapter.OnStarredEventListener{

    private ActionBar toolbar;
    long backPressedTime;
    Toast backToast;
    DatabaseHelper databaseHelper;
    ArrayList<String> subject;
    StarredAdapter starredAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_events);

        toolbar = getSupportActionBar();
        RecyclerView recyclerView = findViewById((R.id.recyclerView));

        databaseHelper = new DatabaseHelper(this);

        subject = new ArrayList<>();

        storeDataArray();

        starredAdapter = new StarredAdapter(StarredEvents.this,subject,this);
        recyclerView.setAdapter(starredAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(StarredEvents.this));



        toolbar = getSupportActionBar();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_starred);
        toolbar.setTitle("Starred Events");
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
                        return true;
                    case R.id.navigation_week:
                        toolbar.setTitle("This Week's Events");
                        startActivity(new Intent(getApplicationContext(), WeekEvents.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    void storeDataArray(){
        TextView empty_starred_text = findViewById(R.id.empty_starred_text);
        ImageView empty_starred = findViewById(R.id.empty_starred);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        assert account != null;
        String table_name = Objects.requireNonNull(account.getDisplayName()).replaceAll("\\s","");
        Cursor cursor = databaseHelper.readStarredData(table_name);
        if(cursor != null){
            if(cursor.getCount() != 0){
                empty_starred.setVisibility(View.GONE);
                empty_starred_text.setVisibility(View.GONE);
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
            Intent intent = new Intent(StarredEvents.this, Profile.class);
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
    public void onStarredEventClick(int position) {
        Intent intent = new Intent(this, StarredEventDetails.class);
        intent.putExtra("selected_event",position);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult: "+requestCode+resultCode );
        Intent intent = new Intent(StarredEvents.this, StarredEvents.class);
        startActivity(intent);
        finish();
    }


}
