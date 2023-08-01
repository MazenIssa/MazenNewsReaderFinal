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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class TopStories extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ProgressBar progressBar;
    ListView listView;
    ArrayList<News> newsList2;
    News currentItem;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_stories);
        TextView textView = findViewById(R.id.topStories);
        textView.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        NewsQuery req = new NewsQuery();
        req.execute("http://http://feeds.bbci.co.uk/news/video_and_audio/news_front_page/rss.xml?edition=uk");

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
            News selectedItem = newsList2.get(i);
            Uri uri = Uri.parse(selectedItem.getLink());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            News selectedItem = newsList2.get(i);
            String pubDate = selectedItem.getPubDate();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TopStories.this);
            alertDialogBuilder.setTitle(getResources().getString(R.string.more_info))
                    .setMessage(getResources().getString(R.string.article_pubdate) + " " + '\n' + '\n' + pubDate + '\n' + '\n' + getResources().getString(R.string.add_to_favs))
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

    @SuppressLint("StaticFieldLeak")
    public class NewsQuery extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                URL url = new URL("http://feeds.bbci.co.uk/news/video_and_audio/news_front_page/rss.xml?edition=uk");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream response = urlConnection.getInputStream();

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(response, "UTF_8");

                boolean insideItem = false;

                int eventType = xpp.getEventType(); //The parser is currently at START_DOCUMENT
                newsList2 = new ArrayList<>();
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
                        newsList2.add(currentItem);
                    }
                    Log.d("Parser", String.valueOf(newsList2));
                    eventType = xpp.next(); //move to the next xml event and store it in a variable
                }
                publishProgress(100);
            } catch (XmlPullParserException | IOException e) {
                Log.e("Error with Reader", e.getMessage());
            }
            return "done";
        }

        public void onProgressUpdate(Integer... args) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(args[0]);
        }

        public void onPostExecute(String fromDoInBackground) {
            Log.i("HTTP", fromDoInBackground);
            ArrayAdapter<News> adapter = new ItemAdapter(TopStories.this, R.layout.row_layout, newsList2);
            listView.setAdapter(adapter);
            progressBar.setVisibility(View.INVISIBLE);
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


