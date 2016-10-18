package com.canthonyscott.microinjectioncalc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

        assert calculate != null;
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

}
