package com.heroessoftware.geotag.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.heroessoftware.geotag.Gestori.AdapterPosizione;
import com.heroessoftware.geotag.Gestori.GestoreDatabase;
import com.heroessoftware.geotag.Gestori.ProcessoEsportazione;
import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.Posizione;
import com.heroessoftware.geotag.R;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PosizioniActivity extends AppCompatActivity {

    public static final int REQUEST_WRITE_STORAGE = 112;
    private Percorso percorsoAttivo;
    private TextView categoria, autista, mezzo, note;
    private AdapterPosizione adapter;
    private GestoreDatabase database = new GestoreDatabase(this);
    private boolean isModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_posizioni);

        percorsoAttivo = getIntent().getExtras().getParcelable(MainActivity.KEY_PERCORSO);

        categoria = (TextView) findViewById(R.id.categoria);
        autista = (TextView) findViewById(R.id.autista);
        mezzo = (TextView) findViewById(R.id.mezzo);
        note = (TextView) findViewById(R.id.note);
        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //informazioni secondarie
        refreshToolbar();

        //crea la RecyclerView
        RecyclerView recList = (RecyclerView) findViewById(R.id.recycler_view);
        sort();
        adapter = new AdapterPosizione(percorsoAttivo.getPosizioni());
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(adapter);
        recList.setHasFixedSize(true);


        //SWIPE AND DRAG
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                adapter.notifyDataSetChanged();
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                adapter.move(fromPos, toPos);
                isModified = true;
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                final Posizione posizione = percorsoAttivo.getPosizioni().get(pos);
                adapter.remove(viewHolder.getAdapterPosition());
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Posizione cancellata", Snackbar.LENGTH_LONG)
                        .setAction(R.string.annulla, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.add(pos, posizione);
                            }
                        }).setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                                if (event != DISMISS_EVENT_ACTION) {
                                    try {
                                        database.openToWrite();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    database.deletePosizione(posizione);
                                    database.close();
                                    isModified = true;
                                }
                            }
                        });
                snackbar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recList);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissionAndExport();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshToolbar();
        refreshLista();
        sort();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateOrdine();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_posizioni, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_elimina: {
                // TODO: 28/10/2016 aggiungere richiesta conferma
                if (deletePercorso(percorsoAttivo)) {
                    finish();
                }
                break;
                // TODO: 28/10/2016 possibile errore in riapertura main
            }
            case R.id.action_modifica: {
                Intent intent = new Intent(this, ModificaPercorsoActivity.class);
                Bundle extra = new Bundle();
                extra.putParcelable(ModificaPercorsoActivity.KEY_PERCORSO_MODIFICARE, percorsoAttivo);
                intent.putExtras(extra);
                startActivity(intent);
                break;
            }
            case R.id.action_settings: {// TODO: 03/11/2016 impostazioni
                Toast.makeText(getApplicationContext(), getString(R.string.work_in_progress),
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ordina la lista
     */
    private void sort() {
        Collections.sort(percorsoAttivo.getPosizioni(), new Comparator<Posizione>() {
            @Override
            public int compare(Posizione o1, Posizione o2) {
                return o2.compareTo(o1);
            }
        });
    }

    /**
     * cancella un percorso dal database
     *
     * @param percorso
     * @return true se eliminato, false altrimenti
     */
    private boolean deletePercorso(Percorso percorso) {
        try {
            database.openToWrite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        boolean eliminato = database.deletePercorso(percorso);
        database.close();
        String message;
        if (eliminato)
            message = getString(R.string.percorso_eliminato);
        else
            message = getString(R.string.percorso_non_eliminato);
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
        return eliminato;
    }

    /**
     * refresh della toolbar
     */
    private void refreshToolbar() {
        try {
            database.openToRead();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        percorsoAttivo = database.getPercorsoCompleto(percorsoAttivo.getId());
        database.close();
        getSupportActionBar().setTitle(percorsoAttivo.getNome());
        categoria.setText(percorsoAttivo.getCategoria());
        autista.setText(percorsoAttivo.getAutista());
        mezzo.setText(percorsoAttivo.getMezzo());
        note.setText(percorsoAttivo.getNote());
    }

    private void refreshLista() {
        adapter.setLista(percorsoAttivo.getPosizioni());
    }

    //AGGIORNA LE POSIZIONI NEL DATABASE
    public void updateOrdine() {
        if (!isModified)
            return;
        adapter.setNumberPosizioni();
        List<Posizione> posiziones = adapter.getLista();
        try {
            database.openToWrite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Posizione pos : posiziones) {
            database.updateNumeroPosizione(pos);
        }
        database.close();
    }

    //PERMESSI ESPORTAZIONE
    private void requestPermissionAndExport() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            ProcessoEsportazione salvatore = new ProcessoEsportazione(this);
            salvatore.execute(percorsoAttivo);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ProcessoEsportazione salvatore = new ProcessoEsportazione(this);
                    salvatore.execute(percorsoAttivo);
                } else {
                    Toast.makeText(this, getString(R.string.permesso_memoria_negato), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}


