package com.example.mazennewsreader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class NewsRoom extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressBar progressBar;
    private ListView listView;
    private ArrayList<News> newsList;
    private static News currentItem;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_room);
        listView = findViewById(R.id.listView);
        TextView textView = findViewById(R.id.titleCanUS);
        textView.setVisibility(View.VISIBLE);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        newsList = new ArrayList<>();
        NewsQuery req = new NewsQuery(this);
        req.execute("http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

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

        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            News selectedItem = newsList.get(i);
            Uri uri = Uri.parse(selectedItem.getLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            News selectedItem = newsList.get(i);
            String pubDate = selectedItem.getPubDate();
            String helpMessage = getResources().getString(R.string.help_news);
            String moreInfoMessage = getResources().getString(R.string.more_info);
            String addToFavsMessage = getResources().getString(R.string.add_to_favs);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewsRoom.this);
            alertDialogBuilder.setTitle(moreInfoMessage)
                    .setMessage(helpMessage + "\n\n" + pubDate + "\n\n" + addToFavsMessage)
                    .setPositiveButton(getResources().getString(R.string.yes), (click, arg) -> {
                        try {
                            DbOpener dbOpener = new DbOpener(this);
                            SQLiteDatabase db = dbOpener.getWritableDatabase();
                            String title = selectedItem.getTitle();
                            String desc = selectedItem.getDescription();
                            String link = selectedItem.getLink();
                            ContentValues newFavourite = new ContentValues();
                            newFavourite.put(DbOpener.TITLE, title);
                            newFavourite.put(DbOpener.DESC, desc);
                            newFavourite.put(DbOpener.DATE, pubDate);
                            newFavourite.put(DbOpener.URL, link);
                            db.insert(DbOpener.TABLE_NAME, null, newFavourite);
                            db.close();
                            Log.d("Database", " Success" + title + desc + pubDate + link);
                        } catch (Exception e) {
                            Log.d("Database", "Failed to save");
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), (click, arg) -> {
                    })
                    .create().show();
            return true;
        });
    }

    private void updateNewsList(ArrayList<News> newsList) {
        this.newsList = newsList;
        ArrayAdapter<News> adapter = new ItemAdapter(NewsRoom.this, R.layout.row_layout, newsList);
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private static class NewsQuery extends AsyncTask<String, Integer, ArrayList<News>> {

        private final WeakReference<NewsRoom> activityReference;

        NewsQuery(NewsRoom context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<News> doInBackground(String... args) {
            ArrayList<News> newsList = new ArrayList<>();
            try {
                URL url = new URL("http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF_8");

                boolean insideItem = false;

                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            currentItem = new News();
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                currentItem.setTitle(xpp.nextText());
                                publishProgress(25);
                            }
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                currentItem.setLink(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem) {
                                currentItem.setDescription(xpp.nextText());
                                publishProgress(50);
                            }
                        } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            if (insideItem) {
                                currentItem.setPubDate(xpp.nextText());
                                publishProgress(75);
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                        newsList.add(currentItem);
                    }
                    Log.d("Parser", String.valueOf(newsList));
                    eventType = xpp.next();
                }
                publishProgress(100);
            } catch (XmlPullParserException | IOException e) {
                Log.e("Error with Reader", e.getMessage());
            }
            return newsList;
        }

        @Override
        protected void onProgressUpdate(Integer... args) {
            NewsRoom activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            ProgressBar progressBar = activity.progressBar;
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(args[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<News> result) {
            NewsRoom activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            activity.updateNewsList(result);
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

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}