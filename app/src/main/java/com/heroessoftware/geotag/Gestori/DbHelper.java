package com.heroessoftware.geotag.Gestori;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * fornisce metodi base per il GestoreDatabase
 * Created by Roberto on 24/08/2016.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GeoTag.db";
    private static final int DATABASE_VERSION = 2;

    // Lo statement SQL di creazione del database
    private static final String DATABASE_CREATE_POSIZIONI = "CREATE TABLE " +
            GestoreDatabase.DATABASE_TABLE_POSIZIONE + "(" +
            GestoreDatabase.KEY_ID_POSIZIONE + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            GestoreDatabase.KEY_ETICHETTA + " TEXT," +
            GestoreDatabase.KEY_INDIRIZZO + " TEXT," +
            GestoreDatabase.KEY_COORDINATE + " TEXT," +
            GestoreDatabase.KEY_UTENTE + " TEXT, " +
            GestoreDatabase.KEY_ORARIO + " TEXT, " +
            GestoreDatabase.KEY_NUMERO + " INTEGER," +
            GestoreDatabase.KEY_POSIZIONE_PERCORSOID + " INTEGER NOT NULL);";

    private static final String DATABASE_CREATE_PERCORSI = "CREATE TABLE " +
            GestoreDatabase.DATABASE_TABLE_PERCORSO + "(" +
            GestoreDatabase.KEY_ID_PERCORSO + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            GestoreDatabase.KEY_NOME_PERCORSO + " TEXT, " +
            GestoreDatabase.KEY_CATEGORIA_PERCORSO + " TEXT, " +
            GestoreDatabase.KEY_AUTISTA + " TEXT, " +
            GestoreDatabase.KEY_MEZZO + " TEXT, " +
            GestoreDatabase.KEY_NOTE + " TEXT);";

    private static final String PERCORSO_DEFAULT = "INSERT INTO " +
            GestoreDatabase.DATABASE_TABLE_PERCORSO + "(" +
            GestoreDatabase.KEY_NOME_PERCORSO + ") VALUES ('Giro della felicità');";
    private static final String PERCORSO_DEFAULT1 = "INSERT INTO " +
            GestoreDatabase.DATABASE_TABLE_PERCORSO + "(" +
            GestoreDatabase.KEY_NOME_PERCORSO + ") VALUES ('Giro allegro');";

    // Costruttore
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Questo metodo viene chiamato durante la creazione del database
    @Override
    public void onCreate(SQLiteDatabase database) {

        database.execSQL(DATABASE_CREATE_POSIZIONI);
        database.execSQL(DATABASE_CREATE_PERCORSI);
        database.execSQL(PERCORSO_DEFAULT);
        database.execSQL(PERCORSO_DEFAULT1);
    }

    // Questo metodo viene chiamato durante l'upgrade del database, ad esempio quando viene incrementato il numero di versione
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE " + GestoreDatabase.DATABASE_TABLE_POSIZIONE + ";");
        database.execSQL("DROP TABLE " + GestoreDatabase.DATABASE_TABLE_PERCORSO + ";");

        onCreate(database);
    }
}
