package com.heroessoftware.geotag.Activities;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.heroessoftware.geotag.Fragment.TimePickerFragment;
import com.heroessoftware.geotag.Gestori.GestoreDatabase;
import com.heroessoftware.geotag.Posizione;
import com.heroessoftware.geotag.R;

import java.sql.SQLException;
import java.util.Calendar;

public class ModificaPosizioneActivity extends AppCompatActivity implements TimePickerFragment.OnSelectHourListener {
    private Posizione posizione;
    public static String KEY_POSOZIONE_MODIFICARE = "PosizioneDaModificare";
    public static String KEY_NUMERO_POSIZIONI_PERCORSO = "numeroposizionipercorso";
    private EditText etichettaText, indirizzoText, utenteText; // TODO: 30/10/2016 sistemare le ultime due
    private TextView orarioText,numeroText;
    private Switch orarioActive;

    @Override
    public void onHourSelected(String hour) {
        posizione.setOrario(hour);
        orarioText.setText(posizione.getOrario());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_posizione);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ELEMENTS
        etichettaText = (EditText) findViewById(R.id.input_etichetta);
        indirizzoText = (EditText) findViewById(R.id.input_indirizzo);
        utenteText = (EditText) findViewById(R.id.input_utente);
        orarioActive = (Switch) findViewById(R.id.switch_orario);
        orarioText = (TextView) findViewById(R.id.input_orario);
        numeroText = (TextView) findViewById(R.id.numeroText);

        //RECEIVE POSIZIONE END SET ELEMENTS
        posizione = getIntent().getExtras().getParcelable(KEY_POSOZIONE_MODIFICARE);
        int numeroPosizioniPercorso= getIntent().getExtras().getInt(KEY_NUMERO_POSIZIONI_PERCORSO);
        if (posizione.getIdPosizione() != 0) {
            getSupportActionBar().setTitle("");
            etichettaText.setText(posizione.getEtichetta());
            indirizzoText.setText(posizione.getIndirizzo());
            utenteText.setText(posizione.getUtente());
            numeroText.setText(getString(R.string.posizione_numero)+" "+posizione.getNumero());
            if (!posizione.getOrario().equalsIgnoreCase("")) {
                orarioText.setVisibility(View.VISIBLE);
                orarioText.setText(posizione.getOrario());
                orarioActive.setChecked(false);
            }
            // TODO: 30/10/2016 aggiungere gli altri
        } else {
            getSupportActionBar().setTitle("");
            orarioActive.setChecked(false);
            orarioText.setVisibility(View.GONE);
            numeroText.setText(getString(R.string.posizione_numero)+" "+ (numeroPosizioniPercorso+1));
            posizione.setNumero(numeroPosizioniPercorso+1);
        }
        orarioActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    orarioText.setVisibility(View.VISIBLE);
                    if (posizione.getOrario().equalsIgnoreCase("")) {
                        final Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        posizione.setOrario(hour + ":" + minute);
                    }
                    orarioText.setText(posizione.getOrario());
                } else
                    orarioText.setVisibility(View.GONE);
            }
        });
        orarioText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_modifica_posizione, menu);
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
            if (addPosizione())
                finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TIME PICKER
    public void showTimePickerDialog() {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.initialize(posizione);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /**
     * aggiunge uns posizione al database
     *
     * @return true se aggiunta, false altrimenti
     */
    private boolean addPosizione() {
        GestoreDatabase database = new GestoreDatabase(this);
        try {
            database.openToWrite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updatePosizione();
        if (posizione.getIdPosizione() == 0) {
            if (database.addPosizione(posizione) == -1) {
                Toast.makeText(getApplicationContext(), getString(R.string.posizione_non_memorizzata),
                        Toast.LENGTH_SHORT).show();
                database.close();
                return false;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.posizione_memorizzata),
                    Toast.LENGTH_SHORT).show();
            database.close();
            return true;
        } else {
            if (database.updatePosizione(posizione)) {
                Toast.makeText(getApplicationContext(), getString(R.string.posizione_modificata),
                        Toast.LENGTH_SHORT).show();
                database.close();
                return true;
            }
            Toast.makeText(getApplicationContext(), getString(R.string.posizione_non_modificata),
                    Toast.LENGTH_SHORT).show();
            database.close();
            return false;
        }
    }

    /**
     * aggiorna la posizione con gli input dell'utente
     */
    private void updatePosizione() {// TODO: 26/10/2016 aggiungere altri dati oggetto
        String etichetta = etichettaText.getText().toString();
        String indirizzo = indirizzoText.getText().toString();
        String utente = utenteText.getText().toString();
        String orario = orarioText.getText().toString();
        posizione.setEtichetta(etichetta);
        posizione.setIndirizzo(indirizzo);
        posizione.setUtente(utente);
        if (orarioActive.isChecked())
            posizione.setOrario(orario);
        else
            posizione.setOrario("");

    }

    /**
     * verifica che gli input siano corretti
     * controlla che venga assegnato almeno una etichetta
     *
     * @return true se gli input sono ok, false altrimenti
     */
    private boolean verifyInputs() {
        if (etichettaText.getText().toString().equals("")) {
            TextInputLayout nomeInput = (TextInputLayout) findViewById(R.id.input_layout_etichetta);
            nomeInput.setError(getString(R.string.error_insert_etichetta));
            return false;
        }

        // TODO: 28/10/2016 aggiungere altri controlli
        return true;
    }
}


