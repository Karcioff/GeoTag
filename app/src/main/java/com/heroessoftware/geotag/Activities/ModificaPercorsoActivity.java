package com.heroessoftware.geotag.Activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.heroessoftware.geotag.Gestori.GestoreDatabase;
import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.R;

import java.sql.SQLException;

public class ModificaPercorsoActivity extends AppCompatActivity {
    public static final String KEY_PERCORSO_MODIFICARE = "PercorsoDaModificare";
    private EditText nomeText, categoriaText, autistaText, mezzoText, noteText;
    private Percorso percorso;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_percorso);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TEXT ELEMENTS
        nomeText = (EditText) findViewById(R.id.input_nome);
        categoriaText = (EditText) findViewById(R.id.input_categoria);
        autistaText = (EditText) findViewById(R.id.input_autista);
        mezzoText = (EditText) findViewById(R.id.input_mezzo);
        noteText = (EditText) findViewById(R.id.input_note);

        percorso = getIntent().getExtras().getParcelable(KEY_PERCORSO_MODIFICARE);
        //RECEIVE PERCORSO AND SET TEXT
        if (percorso.getId()!=0) {
            getSupportActionBar().setTitle(getString(R.string.modifica_percorso));
            nomeText.setText(percorso.getNome());
            categoriaText.setText(percorso.getCategoria());
            autistaText.setText(percorso.getAutista());
            mezzoText.setText(percorso.getMezzo());
            noteText.setText(percorso.getNote());
        } else
            getSupportActionBar().setTitle(getString(R.string.nuovo_percorso));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_modifica_percorso, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (!verifyInputs())
                return true;
            if (addPercorso())
                finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * aggiunge un percorso al database
     *
     * @return true se aggiunto, false altrimenti
     */
    private boolean addPercorso() {
        GestoreDatabase database = new GestoreDatabase(this);
        updatePercorso();
        try {
            database.openToWrite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (percorso.getId()== 0) {
            if (database.addPercorso(percorso) == -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.percorso_non_aggiunto),
                        Toast.LENGTH_SHORT).show();
                database.close();
                return false;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.percorso_aggiunto),
                    Toast.LENGTH_SHORT).show();
            database.close();
            return true;
        } else {
            if (database.updatePercorso(percorso) == 0) {
                Toast.makeText(getApplicationContext(), getString(R.string.percorso_non_modificato),
                        Toast.LENGTH_SHORT).show();
                database.close();
                return false;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.percorso_modificato),
                    Toast.LENGTH_SHORT).show();
            database.close();
            return true;
        }
    }

    /**
     * aggiorna il percorso dagli input dell'utente
     *
     */
    private void updatePercorso() {// TODO: 26/10/2016 aggiungere altri dati oggetto
        String nome = nomeText.getText().toString();
        String categoria= categoriaText.getText().toString();
        String autista =autistaText.getText().toString();
        String mezzo=mezzoText.getText().toString();
        String note= noteText.getText().toString();
        percorso.setNome(nome);
        percorso.setCategoria(categoria);
        percorso.setMezzo(mezzo);
        percorso.setAutista(autista);
        percorso.setNote(note);
    }

    /**
     * verifica che gli input siano corretti
     * controlla che venga assegnato almeno un nome
     *
     * @return true se gli input sono ok, false altrimenti
     */
    private boolean verifyInputs() {
        if (nomeText.getText().toString().equals("")) {
            TextInputLayout nomeInput=(TextInputLayout) findViewById(R.id.input_layout_nome);
            nomeInput.setError(getString(R.string.error_insert_nome));
            return false;
        }

        // TODO: 28/10/2016 aggiungere altri controlli
        return true;
    }
}
