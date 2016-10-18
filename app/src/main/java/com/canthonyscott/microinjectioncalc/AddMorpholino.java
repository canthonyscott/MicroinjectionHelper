package com.canthonyscott.microinjectioncalc;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class AddMorpholino extends AppCompatActivity {

    private static final String LOG_TAG = "AddMorpholino";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_morpholino);
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

        Button addMO = (Button) findViewById(R.id.addMO);
        final EditText geneName = (EditText) findViewById(R.id.EditGeneName);
        final EditText molecularWeight = (EditText) findViewById(R.id.EditMolecularWeight);
        final Switch saveToNetwork = (Switch) findViewById(R.id.saveToNetwork);



        addMO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // provide a way to quickly populate a large test database for testing purposes
                if (geneName.getText().toString().equals("GETATESTDB")){
                    Log.d(LOG_TAG,"GetATestDB value recognized, populating a test database of MOs");
                    populateTestDatabase();
                    return;
                }
                Double mw;

                // get the writeable database
                Log.v(LOG_TAG, "SQLite connection open");
                DatabaseHelper mDbHelper = new DatabaseHelper(getApplicationContext());
                final SQLiteDatabase db = mDbHelper.getWritableDatabase();
                // get the info from the UI inputs
                String gene = geneName.getText().toString();

                if (gene.matches("")){
                    Toast.makeText(AddMorpholino.this, "You must enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // check if user added a molecular weight, if not default to 8400
                if (molecularWeight.getText().toString().matches("")){
                    mw = 8400.0;
                } else {
                    mw = Double.parseDouble(molecularWeight.getText().toString());
                }
                // make sure user did not input 0 as a mw
                if (mw == 0){
                    Toast.makeText(AddMorpholino.this, "This will not work with a weight of 0", Toast.LENGTH_LONG).show();
                    return;
                }
                // add new MO to database
                mDbHelper.addToMOTable(gene, mw, db);
                Log.v(LOG_TAG, "MO Added to db");
                mDbHelper.close();
                Log.v(LOG_TAG, "SQLite connection closed");

                // Add to network database if working connection exists
                if ((saveToNetwork.isChecked() == true)){
                    AddOligoToNetworkDB addOligoToNetworkDB = new AddOligoToNetworkDB(mw,gene,getApplicationContext());
                    addOligoToNetworkDB.execute();
                }

                // create Toast message to show user addition was successful
                String message = gene + " successfully added to the database!";
                Toast.makeText(AddMorpholino.this, message, Toast.LENGTH_SHORT).show();

                // clear the screen
                clearFields();

                // mostly used for testing, can be removed upon release
                parseDb();

            }

        });
    }

    private void populateTestDatabase(){
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.addToMOTable("Axin", 1234.1, db);
        helper.addToMOTable("Nkd1", 4321.2, db);
        helper.addToMOTable("BBS6", 6664.3, db);
        helper.addToMOTable("Dvl3", 4333.2, db);
        helper.addToMOTable("MyoD", 8857.4, db);
        helper.addToMOTable("smarcc1a", 9838.3, db);
        helper.addToMOTable("Autumn", 1111.2, db);
        helper.addToMOTable("Anthony", 2222.3, db);
        helper.addToMOTable("GFP", 5564.2, db);
        db.close();
        helper.close();
        Toast.makeText(AddMorpholino.this, "Test DB added, Enjoy your testing", Toast.LENGTH_SHORT).show();
        clearFields();
    }

    private void clearFields(){
        final EditText geneName = (EditText) findViewById(R.id.EditGeneName);
        final EditText molecularWeight = (EditText) findViewById(R.id.EditMolecularWeight);

        geneName.setText("");
        molecularWeight.setText("");
    }

    // this method is used for testing code in progress
    private void parseDb(){

        DatabaseHelper dbHelp = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = dbHelp.getReadableDatabase();
        Cursor c = dbHelp.getAllRowsFromMOTable(db);
        c.moveToFirst();

        // iterate through the db extracting into and logging just to demonstrate how
        for (int i = 0; i < c.getCount(); i++){
            String gene = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_GENE));
            Double mw = c.getDouble(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT));
            Integer id = c.getInt(c.getColumnIndex(FeedReaderContract.FeedEntry._ID));
            Log.d(LOG_TAG, "_id: " + id.toString());
            Log.d(LOG_TAG,"gene: "+gene);
            Log.d(LOG_TAG,"mol.wt: "+ mw.toString());
            c.moveToNext();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mo, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.remove_mo) {
            Intent intent = new Intent(this, RemoveMO.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
