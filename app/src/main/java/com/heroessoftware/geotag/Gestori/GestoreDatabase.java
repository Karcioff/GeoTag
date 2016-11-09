package com.heroessoftware.geotag.Gestori;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.Posizione;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto on 17/10/2016.
 */

public class GestoreDatabase {
    //stringhe posizioni
    public static final String DATABASE_TABLE_POSIZIONE = "Posizioni";
    public static final String KEY_ETICHETTA = "etichetta";
    public static final String KEY_INDIRIZZO = "indirizzo";
    public static final String KEY_COORDINATE = "coordinate";
    public static final String KEY_UTENTE = "utente";
    public static final String KEY_ORARIO = "orario";
    public static final String KEY_NUMERO = "numero";
    public static final String KEY_ID_POSIZIONE = "posizioneId";
    public static final String KEY_POSIZIONE_PERCORSOID = "percorsoId";
    //stringhe percorsi
    public static final String DATABASE_TABLE_PERCORSO = "Percorsi";
    public static final String KEY_NOME_PERCORSO = "nome";
    public static final String KEY_CATEGORIA_PERCORSO = "categoria";
    public static final String KEY_AUTISTA = "autista";
    public static final String KEY_MEZZO = "mezzo";
    public static final String KEY_NOTE = "note";
    public static final String KEY_ID_PERCORSO = "percorsoID";

    @SuppressWarnings("unused")
    private static final String LOG_TAG = GestoreDatabase.class.getSimpleName();

    private SQLiteDatabase database;
    private DbHelper dbHelper;


    //METODI BASE
    public GestoreDatabase(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void openToWrite() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void openToRead() throws SQLException {
        database = dbHelper.getReadableDatabase();
    }


    public void close() {
        dbHelper.close();
    }

//METODI POSIZIONI

    /**
     * crea un valore posizione da inserire nel database
     *
     * @param pos
     * @return valore senza id
     */
    private ContentValues createContentValuesInsert(Posizione pos) {
        ContentValues values = new ContentValues();
        values.put(KEY_COORDINATE, pos.getStringCoordinate());
        values.put(KEY_INDIRIZZO, pos.getIndirizzo());
        values.put(KEY_ETICHETTA, pos.getEtichetta());
        values.put(KEY_UTENTE, pos.getUtente());
        values.put(KEY_ORARIO, pos.getOrario());
        values.put(KEY_NUMERO, pos.getNumero());
        values.put(KEY_POSIZIONE_PERCORSOID, pos.getIdPercorso());

        return values;
    }

    /**
     * crea un valore posizione per interagire con il database
     *
     * @param pos
     * @return valore con id
     */
    private ContentValues createContentValues(Posizione pos) {
        ContentValues values = new ContentValues();
        values.put(KEY_COORDINATE, pos.getStringCoordinate());
        values.put(KEY_INDIRIZZO, pos.getIndirizzo());
        values.put(KEY_ETICHETTA, pos.getEtichetta());
        values.put(KEY_UTENTE, pos.getUtente());
        values.put(KEY_ORARIO, pos.getOrario());
        values.put(KEY_NUMERO, pos.getNumero());
        values.put(KEY_POSIZIONE_PERCORSOID, pos.getIdPercorso());
        values.put(KEY_ID_POSIZIONE, pos.getIdPosizione());

        return values;
    }

    /**
     * restituisce una posizione aggiornata
     * @param posizione
     * @return posizione aggiornata
     */
    public Posizione getPosizione(Posizione posizione) {
        long id = posizione.getIdPosizione();
        Cursor cursor = database.query(DATABASE_TABLE_POSIZIONE, new String[]{KEY_ETICHETTA, KEY_INDIRIZZO, KEY_COORDINATE, KEY_UTENTE, KEY_ORARIO, KEY_NUMERO, KEY_ID_POSIZIONE, KEY_POSIZIONE_PERCORSOID}, KEY_ID_POSIZIONE + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        String etichetta = cursor.getString(cursor.getColumnIndex(KEY_ETICHETTA));
        String indirizzo = cursor.getString(cursor.getColumnIndex(KEY_INDIRIZZO));
        LatLng coordinate = Posizione.stringToCoordinate(cursor.getString(cursor.getColumnIndex(KEY_COORDINATE)));
        String utente = cursor.getString(cursor.getColumnIndex(KEY_UTENTE));
        String orario = cursor.getString(cursor.getColumnIndex(KEY_ORARIO));
        int numero = cursor.getInt(cursor.getColumnIndex(KEY_NUMERO));
        long idpos = cursor.getLong(cursor.getColumnIndex(KEY_ID_POSIZIONE));
        long idPercorso = cursor.getLong(cursor.getColumnIndex(KEY_POSIZIONE_PERCORSOID));
        Posizione pos = new Posizione(etichetta, indirizzo, coordinate, utente, orario, numero, idPercorso, idpos);
        return pos;
    }

    /**
     * aggiunge una posizione al database
     *
     * @param pos
     * @return indice id POSIZIONE inserita (-1 se non è stata inserita)
     */
    public long addPosizione(Posizione pos) {
        ContentValues initialValues = createContentValuesInsert(pos);
        return database.insertOrThrow(DATABASE_TABLE_POSIZIONE, null, initialValues);
    }

    /**
     * modifica una posizione nel database
     *
     * @param pos
     * @return true, false nessuna modifica
     */
    public boolean updatePosizione(Posizione pos) {
        ContentValues updateValues = createContentValues(pos);
        return database.update(DATABASE_TABLE_POSIZIONE, updateValues, KEY_ID_POSIZIONE + "=" + pos.getIdPosizione(), null) > 0;
    }

    /**
     * modifica il numero di una posizione nel database
     *
     * @param pos
     * @return true, false nessuna modifica
     */
    public boolean updateNumeroPosizione(Posizione pos) {
        ContentValues update = new ContentValues();
        update.put(KEY_NUMERO, pos.getNumero());
        update.put(KEY_ID_POSIZIONE, pos.getIdPosizione());
        return database.update(DATABASE_TABLE_POSIZIONE, update, KEY_ID_POSIZIONE + "=" + pos.getIdPosizione(), null) > 0;
    }

    /**
     * cancella una posizione dal database
     *
     * @param pos
     * @return id posizione cancellata, -1 nessuna posizione cancellata
     */
    public long deletePosizione(Posizione pos) {
        return database.delete(DATABASE_TABLE_POSIZIONE, KEY_ID_POSIZIONE + "=" + pos.getIdPosizione(), null);
    }

    /**
     * restituisce tutte le posizioni nel database
     *
     * @return cursor con posizioni
     */
    private Cursor getAllPosizoni() {
        return database.query(DATABASE_TABLE_POSIZIONE, new String[]{KEY_ETICHETTA, KEY_INDIRIZZO, KEY_COORDINATE, KEY_UTENTE, KEY_ORARIO, KEY_NUMERO, KEY_ID_POSIZIONE, KEY_POSIZIONE_PERCORSOID}, null, null, null, null, null);
    }

    /**
     * restituisce tutta la lista di posizioni
     *
     * @return lista posizioni
     */
    public List<Posizione> getListaposizioni() {

        Cursor cursor = getAllPosizoni();

        List<Posizione> lista = new ArrayList<Posizione>();
        while (cursor.moveToNext()) {
            String etichetta = cursor.getString(cursor.getColumnIndex(KEY_ETICHETTA));
            String indirizzo = cursor.getString(cursor.getColumnIndex(KEY_INDIRIZZO));
            LatLng coordinate = Posizione.stringToCoordinate(cursor.getString(cursor.getColumnIndex(KEY_COORDINATE)));
            String utente = cursor.getString(cursor.getColumnIndex(KEY_UTENTE));
            String orario = cursor.getString(cursor.getColumnIndex(KEY_ORARIO));
            int numero = cursor.getInt(cursor.getColumnIndex(KEY_NUMERO));
            long idpos = cursor.getLong(cursor.getColumnIndex(KEY_ID_POSIZIONE));
            long idPercorso = cursor.getLong(cursor.getColumnIndex(KEY_POSIZIONE_PERCORSOID));
            Posizione pos = new Posizione(etichetta, indirizzo, coordinate, utente, orario, numero, idPercorso, idpos);
            lista.add(pos);
        }
        cursor.close();
        return lista;
    }

    /**
     * restituisce tutte le posizioni di un percorso
     *
     * @param percorsoId
     * @return cursor con posizioni di un percorso
     */
    private Cursor getPosizioniPercorso(long percorsoId) {
        return database.query(DATABASE_TABLE_POSIZIONE, new String[]{KEY_ETICHETTA, KEY_INDIRIZZO, KEY_COORDINATE, KEY_UTENTE, KEY_ORARIO, KEY_NUMERO, KEY_ID_POSIZIONE, KEY_POSIZIONE_PERCORSOID}, KEY_POSIZIONE_PERCORSOID + "=" + percorsoId, null, null, null, null);
    }

    /**
     * restituisce la lista di posizioni di un percorso
     *
     * @param percorsoId
     * @return lista
     */
    public ArrayList<Posizione> getListaPosizioniPercorso(long percorsoId) {

        Cursor cursor = getPosizioniPercorso(percorsoId);

        ArrayList<Posizione> lista = new ArrayList<Posizione>();
        while (cursor.moveToNext()) {
            String etichetta = cursor.getString(cursor.getColumnIndex(KEY_ETICHETTA));
            String indirizzo = cursor.getString(cursor.getColumnIndex(KEY_INDIRIZZO));
            LatLng coordinate = Posizione.stringToCoordinate(cursor.getString(cursor.getColumnIndex(KEY_COORDINATE)));
            String utente = cursor.getString(cursor.getColumnIndex(KEY_UTENTE));
            String orario = cursor.getString(cursor.getColumnIndex(KEY_ORARIO));
            int numero = cursor.getInt(cursor.getColumnIndex(KEY_NUMERO));
            long idpos = cursor.getLong(cursor.getColumnIndex(KEY_ID_POSIZIONE));
            long idPercorso = cursor.getLong(cursor.getColumnIndex(KEY_POSIZIONE_PERCORSOID));
            Posizione pos = new Posizione(etichetta, indirizzo, coordinate, utente, orario, numero, idPercorso, idpos);
            lista.add(pos);
        }
        cursor.close();
        return lista;
    }


    //fetch contacts filter by a string

    /**
     * public Cursor fetchContactsByFilter(String filter) {
     * Cursor mCursor = database.query(true, DATABASE_TABLE, new String[]{
     * KEY_CONTACTID, KEY_NAME, KEY_SURNAME, KEY_SEX, KEY_BIRTH_DATE},
     * KEY_NAME + " like '%" + filter + "%'", null, null, null, null, null);
     * <p/>
     * return mCursor;
     */


    //PERCORSO

    /**
     * crea un valore percorso da inserire nel database
     *
     * @param percorso
     * @return value Percorso
     */
    private ContentValues createContentValuesInsert(Percorso percorso) {
        ContentValues values = new ContentValues();
        values.put(KEY_NOME_PERCORSO, percorso.getNome());
        values.put(KEY_CATEGORIA_PERCORSO, percorso.getCategoria());
        values.put(KEY_AUTISTA, percorso.getAutista());
        values.put(KEY_MEZZO, percorso.getMezzo());
        values.put(KEY_NOTE, percorso.getNote());

        return values;
    }

    /**
     * crea un valore percorso per interagire con il database
     *
     * @param percorso
     * @return value Percorso
     */
    private ContentValues createContentValues(Percorso percorso) {
        ContentValues values = new ContentValues();
        values.put(KEY_NOME_PERCORSO, percorso.getNome());
        values.put(KEY_CATEGORIA_PERCORSO, percorso.getCategoria());
        values.put(KEY_AUTISTA, percorso.getAutista());
        values.put(KEY_MEZZO, percorso.getMezzo());
        values.put(KEY_NOTE, percorso.getNote());
        values.put(KEY_ID_PERCORSO, percorso.getId());

        return values;
    }

    /**
     * aggiunge un percorso al database
     *
     * @param percorso
     * @return la riga dove è stato inserito il percorso (-1 se non è stata inserita)
     */
    public long addPercorso(Percorso percorso) {
        ContentValues initialValues = createContentValuesInsert(percorso);
        return database.insertOrThrow(DATABASE_TABLE_PERCORSO, null, initialValues);
    }

    /**
     * restituisce un Percorso senza lista posizioni
     *
     * @param id
     * @return Percorso corrispondente all'id
     */
    public Percorso getPercorsoDisplay(long id) {
        Cursor cursor = database.query(DATABASE_TABLE_PERCORSO, new String[]{KEY_NOME_PERCORSO, KEY_CATEGORIA_PERCORSO, KEY_AUTISTA, KEY_MEZZO, KEY_NOTE, KEY_ID_PERCORSO}, KEY_ID_PERCORSO + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        String nomen = cursor.getString(cursor.getColumnIndex(KEY_NOME_PERCORSO));
        String categoria = cursor.getString(cursor.getColumnIndex(KEY_CATEGORIA_PERCORSO));
        String autista = cursor.getString(cursor.getColumnIndex(KEY_AUTISTA));
        String mezzo = cursor.getString(cursor.getColumnIndex(KEY_MEZZO));
        String note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
        Percorso percorso = new Percorso(nomen, categoria, autista, mezzo, note, id);
        cursor.close();
        return percorso;
    }

    /**
     * restituisce un Percorso completo, con lista Posizioni
     *
     * @param id
     * @return Percorso completo
     */
    public Percorso getPercorsoCompleto(long id) {
        Percorso percorso = getPercorsoDisplay(id);
        ArrayList<Posizione> lista = getListaPosizioniPercorso(id);
        percorso.setPosizioni(lista);
        return percorso;
    }

    /**
     * restituisce un cursor contenente i percorsi nel database
     *
     * @return cursor percorsi
     */
    private Cursor getPercorsi() {
        return database.query(DATABASE_TABLE_PERCORSO, new String[]{KEY_NOME_PERCORSO, KEY_CATEGORIA_PERCORSO, KEY_AUTISTA, KEY_MEZZO, KEY_NOTE, KEY_ID_PERCORSO}, null, null, null, null, null);
    }

    /**
     * restituisce la lista di percorsi nel database
     * senza liste di posizioni
     *
     * @return lista percorsi
     */
    public ArrayList<Percorso> getListaPercorsi() {
        ArrayList<Percorso> lista1 = new ArrayList<Percorso>();
        Cursor cursor = getPercorsi();
        while (cursor.moveToNext()) {
            String nomen = cursor.getString(cursor.getColumnIndex(KEY_NOME_PERCORSO));
            String categoria = cursor.getString(cursor.getColumnIndex(KEY_CATEGORIA_PERCORSO));
            String autista = cursor.getString(cursor.getColumnIndex(KEY_AUTISTA));
            String mezzo = cursor.getString(cursor.getColumnIndex(KEY_MEZZO));
            String note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
            long id = cursor.getLong(cursor.getColumnIndex(KEY_ID_PERCORSO));
            Percorso percorso = new Percorso(nomen, categoria, autista, mezzo, note, id);
            lista1.add(percorso);
        }
        cursor.close();
        return lista1;
    }

    /**
     * elimina un percorso
     *
     * @param percorso
     * @return true se eliminato, false altrimenti
     */
    public boolean deletePercorso(Percorso percorso) {
        return database.delete(DATABASE_TABLE_PERCORSO, KEY_ID_PERCORSO + "=" + percorso.getId(), null) > 0;
    }

    /**
     * modifica un percorso nel database
     *
     * @param percorso da modificare
     * @return numero righe modificate
     */
    public long updatePercorso(Percorso percorso) {
        ContentValues update = new ContentValues();
        update.put(KEY_NOME_PERCORSO, percorso.getNome());
        update.put(KEY_CATEGORIA_PERCORSO, percorso.getCategoria());
        update.put(KEY_AUTISTA, percorso.getAutista());
        update.put(KEY_MEZZO, percorso.getMezzo());
        update.put(KEY_NOTE, percorso.getNote());
        return database.update(DATABASE_TABLE_PERCORSO, update, KEY_ID_PERCORSO + "=" + percorso.getId(), null);
    }

    /**
     * cancella tutte le posizioni
     *
     * @param percorso
     * @return numero di posizioni eliminate
     */
    public int clearPercorso(Percorso percorso) {
        return database.delete(DATABASE_TABLE_POSIZIONE, KEY_POSIZIONE_PERCORSOID + "=" + percorso.getId(), null);
    }
}
