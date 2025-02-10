package com.android.achievix.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EnterPasswordActivityy extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        Intent intent = getIntent();
        int password = intent.getIntExtra("password", 0);
        String invokedFrom = intent.getStringExtra("invokedFrom");


        assert invokedFrom != null;
        if (invokedFrom.equals("main") || invokedFrom.equals("newProfile")) {

        } else {

        }

    }
}

