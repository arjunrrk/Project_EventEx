package com.mail.eventex;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.services.gmail.GmailScopes;

public class Profile extends AppCompatActivity {

    private ActionBar toolbar;
    ImageView imageView;
    TextView name, email;
    Button signOut;

    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = getSupportActionBar();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        toolbar.setTitle("My Profile");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(GmailScopes.GMAIL_READONLY))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        imageView = findViewById(R.id.userPhoto);
        name = findViewById(R.id.userName);
        email = findViewById(R.id.userMail);
        signOut = findViewById(R.id.signOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.signOut) {
                    signOut();
                }
            }
        });

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null){
            String userName = acct.getDisplayName();
            String userEmail = acct.getEmail();
            Uri userPhoto = acct.getPhotoUrl();


            name.setText(userName);
            email.setText(userEmail);
            if(userPhoto!=null){
                Glide.with(this).load(String.valueOf(userPhoto)).into(imageView);
            }

        }


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
                        startActivity(new Intent(getApplicationContext(), WeekEvents.class));
                        overridePendingTransition(0,0);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }
    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast toast = Toast.makeText(Profile.this,"Signed out successfully!",Toast.LENGTH_LONG);
                        View view = toast.getView();
                        view.getBackground().setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        toast.show();

                        Intent i = new Intent(Profile.this, MainActivity.class);        // Specify any activity here e.g. home or splash or login etc
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("EXIT", true);
                        startActivity(i);
                        finish();
                    }
                });
    }
}
