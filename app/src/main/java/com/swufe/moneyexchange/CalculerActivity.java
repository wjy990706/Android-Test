package com.swufe.moneyexchange;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class CalculerActivity extends AppCompatActivity {
    float detail=0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculer);
        float rate=getIntent().getFloatExtra("detail",0f);
        String title=getIntent().getStringExtra("title");

        Log.i("Calculer", "onCreate:rate= "+rate);
        Log.i("Calculer", "onCreate:title= "+title);

        ((TextView)findViewById(R.id.title2)).setText(title);

    }
}