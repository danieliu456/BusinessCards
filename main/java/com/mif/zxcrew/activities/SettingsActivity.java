package com.mif.zxcrew.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mif.zxcrew.ocrcards.R;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Sets up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" SettingsActivity");
        toolbar.setLogo(R.drawable.settings);

        //Sets up Bottom menu_navigation menu
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigationView);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId())
                {
                    case R.id.navigation_contacts:
                        Intent intent = new Intent(SettingsActivity.this, ContactViewActivity.class);
                        startActivity(intent);
                        return true;

                    case R.id.navigation_capture:
                        Intent intentMain = new Intent(SettingsActivity.this, CameraActivity.class);
                        startActivity(intentMain);
                        return true;

                    case R.id.navigation_settings:

                }
                return false;
            }
        });





    }
}
