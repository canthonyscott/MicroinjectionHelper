package com.canthonyscott.microinjectioncalc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class RemoveMO extends AppCompatActivity {

    private final ArrayList<Morpholino> moList = new ArrayList<>();
    //    private ArrayAdapter<Morpholino> adapter;
    private Morpholino selectedMO;
    private OligoAdapter adapter;

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_mo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // grab the ListView id
        final ListView listViewMO = (ListView) findViewById(R.id.listViewMO);

        // get an ArrayList of Morpholinos from the database
        DatabaseToArray databaseToArray = new DatabaseToArray();
        databaseToArray.execute();


        adapter = new OligoAdapter(this, R.layout.listview_item_row_mo, moList);


//        View header = getLayoutInflater().inflate(R.layout.listview_header_row_mo, null);
//        listViewMO.addHeaderView(header);
        // TODO: 1/8/2016 See if I can get a header to work without messing up the selection option
        listViewMO.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listViewMO.setAdapter(adapter);


        // get the selected MO from the checked listview item
        listViewMO.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("CLICKED", "Position: " + position);
                adapter.setStoredSelectedItem(position);
                selectedMO = (Morpholino) listViewMO.getItemAtPosition(position);
                Log.d("CLICKED", "SelectedMO: " + selectedMO.toString());
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remove_mo, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.addMO) {
            startActivity(new Intent(this, AddMorpholino.class));
        }

        if (id == R.id.shareMO) {
            // TODO: 4/12/2016 SAVE MO TO NETWORK DATABASE
            boolean result = shareToNetworkDB();
        }

        if (id == R.id.deleteMO) {
            deleteSingleMO();
            // notify adapter of the change
            adapter.setStoredSelectedItem(-1);
            adapter.notifyDataSetChanged();
            Toast.makeText(RemoveMO.this, selectedMO.toString() + " removed.", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.deleteALL) {
//            resetMOdatabase();
            showAlert();
//            Toast.makeText(RemoveMO.this, "Morpholino Database Reset", Toast.LENGTH_LONG).show();
//            finish();
        }

        if (id == R.id.showShared){
            startActivity(new Intent(this, manageSharedOligos.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean shareToNetworkDB() {
        AddOligoToNetworkDB addMO = new AddOligoToNetworkDB(selectedMO.getMolecularWeight(),selectedMO.getGene(),getApplicationContext());
        addMO.execute();
        return true;
    }

    private void showAlert() {
        Log.d("showAlert", "Method Called");
        FragmentManager fm = getFragmentManager();
        DialogFragment newFragment = new ResetWarningFragment();
        newFragment.show(fm, "Tag");
    }

    public class ResetWarningFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This will erase everything, are you sure?")
                    .setTitle("Warning!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user clicked OK
                            resetMOdatabase();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // user clicked cancel
                        }
                    });


            Log.d("Builder", "Builder Completed");
            return builder.create();
        }
    }


    private void resetMOdatabase() {
        Log.d("resetMODatabase", "Method Called");
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(FeedReaderContract.SQL_DELETE_ENTRIES);
        db.execSQL(FeedReaderContract.SQL_CREATE_MO_ENTRIES);
        helper.addToMOTable("AvgMO", 8400.0, db);
        helper.close();
        Toast.makeText(RemoveMO.this, "Morpholino Database Reset", Toast.LENGTH_LONG).show();
        finish();

    }

    private void deleteSingleMO() {
        // delete the selected MO from the database using its unique ID (invisible to the user)
        // open and close database
        int moID = selectedMO.getId();
        DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.removeFromMOTable(db, moID);
        helper.close();
        // remove selected MO from the array
        moList.remove(selectedMO);
    }

    public class DatabaseToArray extends AsyncTask<Void, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(Void... params) {
            databasetoArrayMO();
            return moList;
        }

        @Override
        protected void onPostExecute(ArrayList arrayList) {
            super.onPostExecute(arrayList);
            adapter.notifyDataSetChanged();
            Log.d("AsyncTask", "Background SQL Query Completed, onPostExecute performed");
            Log.d("AsyncTask", arrayList.toString());
        }

        private void databasetoArrayMO() {

            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            SQLiteDatabase db = helper.getReadableDatabase();

            // String to request all columns
            String[] projection = {FeedReaderContract.FeedEntry._ID, FeedReaderContract.FeedEntry.COLUMN_NAME_GENE, FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT};
            // String for the sortOrder command
            String sortOrder = FeedReaderContract.FeedEntry._ID + " DESC";
            //Cursor object to receive the Db query
            // Query the Db to retrieve all rows from the MO Table
            Cursor c = db.query(
                    FeedReaderContract.FeedEntry.MORPHOLINOS_TABLE,
                    projection, null, null, null, null, sortOrder);
            // move the cursor to the beginning of the returned rows
            c.moveToFirst();

            for (int i = 0; i < c.getCount(); i++) {
                String gene = c.getString(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_GENE));
                Double mw = c.getDouble(c.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT));
                Integer id = c.getInt(c.getColumnIndex(FeedReaderContract.FeedEntry._ID));
                moList.add(new Morpholino(mw, gene, id));
                Log.d("ArrayListItem", gene + id.toString());
                c.moveToNext();
            }
            c.close();
            helper.close();
        }


    }


}
