package com.canthonyscott.microinjectioncalc;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class RNAInjection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rnainjection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button calculate = (Button) findViewById(R.id.calculate);
        final EditText numberOfPumps = (EditText) findViewById(R.id.numberOfPumps);
        final EditText numberOfMm = (EditText) findViewById(R.id.numberOfMillimeters);
        final EditText concentrationOfRNA = (EditText) findViewById(R.id.concentration);
        final TextView injectionQuantity = (TextView) findViewById(R.id.injectionQuantity);
        final TextView injectionQuantityPg = (TextView) findViewById(R.id.injectionQuantityPg);
        final TextView injectionVolumeUI = (TextView) findViewById(R.id.injectionVolume);


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set listeners for bg color changes
        concentrationOfRNA.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(concentrationOfRNA, numberOfPumps, numberOfMm);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        numberOfPumps.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(concentrationOfRNA, numberOfPumps, numberOfMm);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        numberOfMm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(concentrationOfRNA, numberOfPumps, numberOfMm);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });

        assert calculate != null;
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // minimize keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(numberOfPumps.getWindowToken(),0);

                Double injectionVolume;
                Double concentration;
                Double nanogramsInjected;
                Double picogramsInjected;

                // try catch to prevent users from not inputting values
                try {
                    // verify user didnt input zeros
                    if (numberOfMm.getText().toString().equalsIgnoreCase("0") || numberOfPumps.getText().toString().equalsIgnoreCase("0")) {
                        Toast.makeText(RNAInjection.this, "These values cannot be zero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    concentration = Double.parseDouble(concentrationOfRNA.getText().toString());
                    // calculate injection volume
                    injectionVolume = ((Double.parseDouble(numberOfMm.getText().toString())) * 1000) /
                            ((Double.parseDouble(numberOfPumps.getText().toString())) * 32);
                    // round to two decimals
                    injectionVolume = Double.parseDouble(new DecimalFormat("####.##").format(injectionVolume));
                    nanogramsInjected = concentration * injectionVolume / 1000;
                    nanogramsInjected = Double.parseDouble(new DecimalFormat("#####.###").format(nanogramsInjected));
                    picogramsInjected = nanogramsInjected * 1000;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(RNAInjection.this, "Please enter your own values", Toast.LENGTH_SHORT).show();
                    return;
                }
                // output results to UI
                injectionVolumeUI.setText(injectionVolume.toString());
                injectionQuantity.setText(nanogramsInjected.toString());
                injectionQuantityPg.setText(picogramsInjected.toString());
                // log history to database
            }
        });
    }

    private void resetLayoutColors(EditText concentration, EditText pumps, EditText mm){
        concentration.setBackgroundColor(Color.WHITE);
        pumps.setBackgroundColor(Color.WHITE);
        mm.setBackgroundColor(Color.WHITE);
    }


}
