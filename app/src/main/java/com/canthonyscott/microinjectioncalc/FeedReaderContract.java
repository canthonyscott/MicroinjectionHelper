package com.canthonyscott.microinjectioncalc;

import android.provider.BaseColumns;

final class FeedReaderContract {

    public FeedReaderContract(){}

    public static abstract class FeedEntry implements BaseColumns{

        // variables used to construct MO table
        public static final String MORPHOLINOS_TABLE = "MorpholinosTable";
        public static final String COLUMN_NAME_GENE = "GeneName";
        public static final String COLUMN_NAME_MOLWT = "MolecularWeight";

        // variables used to construct RNA table
        public static final String RNA_TABLE = "RNATable";
        public static final String COLUMN_NAME_RNA = "ConstructName";
        public static final String COLUMN_NAME_CONCENTRATION = "StockConcentration";

    }

    // variables for general construction
    private static final String TEXT_TYPE = " TEXT";
    private static final String NUM_TYPE = " REAL";
    private static final String COMMA_SEP = ",";


    // string to create the MO table
    public static final String SQL_CREATE_MO_ENTRIES = "CREATE TABLE " + FeedEntry.MORPHOLINOS_TABLE +
            " (" + FeedEntry._ID + " INTEGER PRIMARY KEY," + FeedEntry.COLUMN_NAME_GENE + TEXT_TYPE +
            COMMA_SEP + FeedEntry.COLUMN_NAME_MOLWT + NUM_TYPE + " )";

    // string to create the RNA table
    public static final String SQL_CREATE_RNA_ENTRIES = "CREATE TABLE " + FeedEntry.RNA_TABLE +
            " (" + FeedEntry._ID + " INTEGER PRIMARY KEY," + FeedEntry.COLUMN_NAME_RNA + TEXT_TYPE +
            COMMA_SEP + FeedEntry.COLUMN_NAME_CONCENTRATION + NUM_TYPE + " )";

    // string to destroy the MO table
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + FeedEntry.MORPHOLINOS_TABLE;



}
