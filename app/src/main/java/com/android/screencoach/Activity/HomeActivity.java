package com.android.achievix.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.achievix.R;
import com.android.achievix.Permission.GetUsageStatsPermissionActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    CardView locationCard, aiCard, trackerCard;
    TextView current_Date;
    TextView greetingTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        locationCard = findViewById(R.id.locationCard);
        aiCard = findViewById(R.id.aiCard);
        trackerCard = findViewById(R.id.trackerCard);
        greetingTextView = findViewById(R.id.greetingTextView); // for current time and greetings

        // Set the greeting message
        String greeting = GreetingHelper.getGreeting();
        greetingTextView.setText(greeting);

        locationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AdaptiveFocusZone.class);
                startActivity(intent);
            }
        });

        aiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AiChatbot.class);
                startActivity(intent);
            }
        });

        trackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, GetUsageStatsPermissionActivity.class);
                startActivity(intent);
            }
        });
    }
}

class GreetingHelper {

    public static String getGreeting() {
        // Get the current time
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        // Determine the appropriate greeting based on the time of day
        String greeting;
        if (hourOfDay >= 0 && hourOfDay < 12) {
            greeting = "Good morning";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }

        return greeting;
    }
}
