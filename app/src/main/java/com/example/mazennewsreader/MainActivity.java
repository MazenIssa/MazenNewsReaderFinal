package com.example.mazennewsreader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText emailText, pwdText;
    SharedPreferences sp = null;
    Button button;
    public static String emailStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        saveSharedPrefs(emailStr);
        emailText = findViewById(R.id.email);
        emailText.setText(emailStr);
        pwdText = findViewById(R.id.pwd);
        button = findViewById(R.id.loginButton);

        Intent appMap = new Intent(this, AppMap.class);
        button.setOnClickListener(click -> startActivity(appMap));

    }

    private void saveSharedPrefs(String stringToSave) {
        sp = getSharedPreferences("FileName", Context.MODE_PRIVATE);
        emailStr = sp.getString("TypedText", "");
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("TypedText", stringToSave);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPrefs((emailText.getText().toString()));
    }
}