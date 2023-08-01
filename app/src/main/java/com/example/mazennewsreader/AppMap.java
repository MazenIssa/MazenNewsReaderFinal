package com.example.mazennewsreader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class AppMap extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ACTIVITY_NAME = "Application Map";
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_map);
        ImageButton newsButton = findViewById(R.id.newsButton);
        Button favButton = findViewById(R.id.favButton);
        ImageButton topStories = findViewById(R.id.topStoriesButton);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open, R.string.close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        mDrawer.addDrawerListener(drawerToggle);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        Intent newsPage = new Intent(this, NewsRoom.class);
        Intent favPage = new Intent(this, Favourites.class);
        Intent topStoriesPage = new Intent(this, TopStories.class);
        newsButton.setOnClickListener(click -> startActivity(newsPage));
        favButton.setOnClickListener(click -> startActivity(favPage));
        topStories.setOnClickListener(click -> startActivity(topStoriesPage));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String message;
        String snackbar;
        View snackbarText = findViewById(R.id.snackbar);

        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            mDrawer.openDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.about) {
            message = getResources().getString(R.string.newsreader_version);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.help) {
            snackbar = getResources().getString(R.string.select_the_page);
            Snackbar.make(snackbarText, snackbar, Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.close), view -> {
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(ACTIVITY_NAME, "In onStart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(ACTIVITY_NAME, "In onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(ACTIVITY_NAME, "In onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(ACTIVITY_NAME, "In onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(ACTIVITY_NAME, "In onDestroy()");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation item clicks here
        return false;
    }
}
