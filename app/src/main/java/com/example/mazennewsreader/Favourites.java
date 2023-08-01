package com.example.mazennewsreader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Favourites extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DbOpener dbOpener;
    SQLiteDatabase db;
    Cursor results;
    TextView title, desc;
    private MyListAdapter myAdapter;
    ArrayList<News> favouritesList;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;

    public Favourites() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // The following loads the activity_favourites.xml, finding and displaying the buttons
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        TextView textView = findViewById(R.id.titleFavs);
        textView.setVisibility(View.VISIBLE);
        ListView listView = findViewById(R.id.listView2);

        // The following loads the toolbar from toolbar.xml and adds a navigation drawer.
        // activity_favourites.xml had a toolbar element added, so it was expecting a toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = setupDrawerToggle();
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();
        mDrawer.addDrawerListener(drawerToggle);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);

        // The following will load the data from the database. If it doesn't work, the try-catch statement enables the app to keep running
        // A message will appear in the LogCat for the programmer if the loadFromDatabase() doesn't work.
        try {
            loadDataFromDatabase();
        } catch (Exception e) {
            Toast.makeText(this, "error loading DB", Toast.LENGTH_LONG).show();
        }
        // The Cursor will print to show the programmer what items were loaded from the database
        try {
            printCursor();
        } catch (Exception e) {
            Toast.makeText(this, "error loading Cursor", Toast.LENGTH_LONG).show();
        }
        // Set listView to adapter
        listView.setAdapter(myAdapter = new MyListAdapter());

        // Set listener for all items of the listView when the user long clicks
        listView.setOnItemLongClickListener((p, b, pos, id) -> {
            News selectedItem = favouritesList.get(pos);
            String date = selectedItem.getPubDate();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.article_pubdate) + " " + '\n' + date)
                    .setMessage(getResources().getString(R.string.delete_message))
                    .setPositiveButton(getResources().getString(R.string.delete), (click, arg) -> {
                        deleteItem(selectedItem);
                        favouritesList.remove(pos);
                        myAdapter.notifyDataSetChanged();
                    })
                    .setNegativeButton(getResources().getString(R.string.back_to_favs), (click, arg) -> {
                    })
                    .create().show();
            return true;
        });

        // The following click listener will send the user to the BBC link where the article resides
        listView.setOnItemClickListener((p, b, pos, id) -> {
            News selectedItem = favouritesList.get(pos);
            Uri uri = Uri.parse(selectedItem.getLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    // This method is called in the onLongClickListener when the user decides to delete an article from their Favs
    protected void deleteItem(News x) {
        db.delete(DbOpener.TABLE_NAME, DbOpener.COL_ID + "= ?", new String[]{Long.toString(x.getId())});
    }

    private void loadDataFromDatabase() {
        // Called earlier in the try-catch, this method will load items from the database
        dbOpener = new DbOpener(Favourites.this);
        db = dbOpener.getReadableDatabase();
        String[] columns = {DbOpener.COL_ID, DbOpener.TITLE, DbOpener.DESC, DbOpener.DATE, DbOpener.URL};
        results = db.query(false, DbOpener.TABLE_NAME, columns, null, null, null,
                null, null, null);
        favouritesList = new ArrayList<>();
        int colIndex = results.getColumnIndex(DbOpener.COL_ID);
        int titleIndex = results.getColumnIndex(DbOpener.TITLE);
        int descIndex = results.getColumnIndex(DbOpener.DESC);
        int dateIndex = results.getColumnIndex(DbOpener.DATE);
        int urlIndex = results.getColumnIndex(DbOpener.URL);
        while (results.moveToNext()) {
            long id = results.getInt(colIndex);
            String title = results.getString(titleIndex);
            String desc = results.getString(descIndex);
            String date = results.getString(dateIndex);
            String url = results.getString(urlIndex);
            // Adds the retrieved contents from database columns and adds them to an ArrayList populated with News objects
            // See News.java for constructor, etc.
            favouritesList.add(new News(id, title, desc, date, url));
        }
    }

    private void printCursor() {
        // Method for cursor function. This is for the programmer and can be useful for debugging if the database doesn't load & display properly
        int version = db.getVersion();
        int numColumns = results.getColumnCount();
        String columnNames = Arrays.toString(results.getColumnNames());
        int numResults = results.getCount();
        Log.v("PC ", "Database version " + version);
        Log.v("PC ", "Column Count: " + numColumns);
        Log.v("PC ", "Column Names: " + columnNames);
        Log.v("PC ", "Number of Results: " + numResults);
        if (results.moveToFirst()) {
            do {
                StringBuilder sb = new StringBuilder();
                for (int idx = 0; idx < numColumns; ++idx) {
                    sb.append(results.getString(idx));
                    if (idx < numColumns - 1)
                        sb.append("; ");
                }
                Log.v("PC ", String.format("Row: %d, Values: %s", results.getPosition(),
                        sb));
            } while (results.moveToNext());
        }
    }

    protected class MyListAdapter extends BaseAdapter {

        // The following adapter populates the listView with elements from favouritesList

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View old, ViewGroup parent) {
            News favourite = favouritesList.get(position);
            LayoutInflater inflater = getLayoutInflater();
            View newView;
            newView = inflater.inflate(R.layout.row_layout, null);
            title = newView.findViewById(R.id.title);
            title.setText(favourite.getTitle());
            desc = newView.findViewById(R.id.desc);
            desc.setText(favourite.getDescription());
            return newView;
        }

        @Override
        public int getCount() {
            return favouritesList.size();
        }

        @Override
        public Object getItem(int position) {
            return favouritesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 1;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        int itemId = item.getItemId();

        if (itemId == R.id.newsroom) {
            intent = new Intent(this, NewsRoom.class);
        } else if (itemId == R.id.newsroom2) {
            intent = new Intent(this, TopStories.class);
        } else if (itemId == R.id.favourites) {
            intent = new Intent(this, Favourites.class);
        } else if (itemId == R.id.appMap) {
            intent = new Intent(this, AppMap.class);
        } else if (itemId == R.id.signout) {
            intent = new Intent(this, MainActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
        return false;
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open, R.string.close);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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
            snackbar = getResources().getString(R.string.help_news);
            Snackbar.make(snackbarText, snackbar, Snackbar.LENGTH_LONG)
                    .setAction(getResources().getString(R.string.close), view -> {
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
