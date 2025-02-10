package com.android.achievix.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.achievix.R;

public class Suggestions extends AppCompatActivity {
    ImageView btnback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        btnback = (ImageView) findViewById(R.id.btnback);

        btnback.setOnClickListener(new View.OnClickListener(){  //backbutton
            @Override
            public void onClick (View view){
                Intent intent = new Intent(Suggestions.this, com.android.achievix.Activity.HomeActivity.class);
                startActivity(intent);
            }
        });
    }

}