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

public class MakeAnInjectionMix extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_an_injection_mix);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get variables for layout views
        final EditText stockConcentration = (EditText) findViewById(R.id.stockConcentration);
        final EditText desiredVolume = (EditText) findViewById(R.id.desiredVolume);
        final EditText desiredConcentration = (EditText) findViewById(R.id.desiredConcentration);
        final TextView resultRNA = (TextView) findViewById(R.id.result_RNA);
        final TextView resultPhenolRed = (TextView) findViewById(R.id.resultPhenolRed);
        final TextView resultWater = (TextView) findViewById(R.id.resultWater);
        final Button calculate = (Button) findViewById(R.id.calculate);
        Button clear = (Button) findViewById(R.id.clear);


        calculate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // minimize the soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(calculate.getWindowToken(),0);
            }
        });

        stockConcentration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(stockConcentration, desiredVolume, desiredConcentration);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        desiredVolume.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(stockConcentration, desiredVolume, desiredConcentration);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });
        desiredConcentration.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                resetLayoutColors(stockConcentration, desiredVolume, desiredConcentration);
                int color = ContextCompat.getColor(getApplicationContext(), R.color.primary_light);
                v.setBackgroundColor(color);
            }
        });


        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double volume;
                double concentration;
                double stock;

                // minimize the soft keyboard
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(calculate.getWindowToken(),0);

                resetLayoutColors(stockConcentration, desiredVolume, desiredConcentration);


                // sanitize all inputs and calculate
                try {
                    volume = Double.parseDouble(desiredVolume.getText().toString());
                    concentration = Double.parseDouble(desiredConcentration.getText().toString());
                    stock = Double.parseDouble(stockConcentration.getText().toString());
                    if (volume == 0 | concentration == 0 | stock == 0){
                        Toast.makeText(MakeAnInjectionMix.this, "Values cannot be zero", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MakeAnInjectionMix.this, "Please enter all values", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }

                // calculate the volumes needed
                Double phenolRed = volume/5;
                Double rnaVol = (concentration*volume)/stock;
                Double water = volume - phenolRed - rnaVol;

                //round results
                phenolRed = Double.parseDouble(new DecimalFormat("####.##").format(phenolRed));
                rnaVol = Double.parseDouble(new DecimalFormat("####.##").format(rnaVol));
                water = Double.parseDouble(new DecimalFormat("####.##").format(water));
                
                // send result to UI
                resultRNA.setText(rnaVol.toString());
                resultPhenolRed.setText(phenolRed.toString());
                resultWater.setText(water.toString());

            }
        });
        
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFields();
            }
        });

    }
    
    private void clearAllFields (){
        EditText stockConcentration = (EditText) findViewById(R.id.stockConcentration);
        EditText desiredVolume = (EditText) findViewById(R.id.desiredVolume);
        EditText desiredConcentration = (EditText) findViewById(R.id.desiredConcentration);
        TextView resultRNA = (TextView) findViewById(R.id.result_RNA);
        TextView resultPhenolRed = (TextView) findViewById(R.id.resultPhenolRed);
        TextView resultWater = (TextView) findViewById(R.id.resultWater);
        
        final String CLEARED = "";
        
        stockConcentration.setText(CLEARED);
        desiredVolume.setText(CLEARED);
        desiredConcentration.setText(CLEARED);
        resultRNA.setText(CLEARED);
        resultPhenolRed.setText(CLEARED);
        resultWater.setText(CLEARED);

    }

    private void resetLayoutColors(EditText stockConc, EditText desiredVol, EditText desiredConc){
        stockConc.setBackgroundColor(Color.WHITE);
        desiredVol.setBackgroundColor(Color.WHITE);
        desiredConc.setBackgroundColor(Color.WHITE);
    }


}
