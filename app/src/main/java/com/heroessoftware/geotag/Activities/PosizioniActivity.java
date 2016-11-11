package com.heroessoftware.geotag.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.heroessoftware.geotag.Gestori.Utils;
import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.Posizione;
import com.heroessoftware.geotag.R;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;

public class PosizioniActivity extends AppCompatActivity {

    public static final int REQUEST_WRITE_STORAGE = 112;
    private Percorso percorsoAttivo;
    private TextView categoria, autista, mezzo, note;
    private AdapterPosizione adapter;
    private GestoreDatabase database = new GestoreDatabase(this);
    private boolean isOrderModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posizioni);

        percorsoAttivo = getIntent().getExtras().getParcelable(MainActivity.KEY_PERCORSO);

        categoria = (TextView) findViewById(R.id.categoria);
        autista = (TextView) findViewById(R.id.autista);
        mezzo = (TextView) findViewById(R.id.mezzo);
        note = (TextView) findViewById(R.id.note);

        //TOOLBAR
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
                refreshVisualOrder();
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = target.getAdapterPosition();
                adapter.move(fromPos, toPos);
                isOrderModified = true;
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final int pos = viewHolder.getAdapterPosition();
                final Posizione posizione = percorsoAttivo.getPosizioni().get(pos);
                adapter.remove(viewHolder.getAdapterPosition());
                CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, getString(R.string.posizione_eliminata), Snackbar.LENGTH_LONG)
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
                                    new DatatbaseInteraction(Utils.DELETE_POSITION).execute(posizione);
                                    isOrderModified = true;
                                }
                                refreshVisualOrder();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveOrder();
        if (isOrderModified) {
            Intent intent = new Intent(Utils.CHANGED_POSITION_ORDER);
            Bundle extra = new Bundle();
            extra.putParcelable(Utils.KEY_PERCORSO_MODIFICATO, percorsoAttivo);
            intent.putExtras(extra);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
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
                // TODO: 28/10/2016 implementare
                /*if (deletePercorso(percorsoAttivo)) {
                    finish();
                }*/
                Toast.makeText(getApplicationContext(), getString(R.string.work_in_progress),
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.action_modifica: {
                Intent intent = new Intent(this, ModificaPercorsoActivity.class);
                Bundle extra = new Bundle();
                extra.putParcelable(Utils.KEY_PERCORSO_MODIFICARE, percorsoAttivo);
                intent.putExtras(extra);
                startActivityForResult(intent, Utils.MODIFY_PERCORSO);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Utils.MODIFY_PERCORSO) {
            if (resultCode == RESULT_OK) {
                percorsoAttivo = data.getExtras().getParcelable(Utils.KEY_PERCORSO_MODIFICATO);
                refreshToolbar();
                new DatatbaseInteraction(Utils.UPDATE_PERCORSO_INFO).execute();
                Intent intent = new Intent(Utils.PERCORSO_MODIFIED);
                Bundle extra = new Bundle();
                extra.putParcelable(Utils.KEY_PERCORSO_MODIFICATO, percorsoAttivo);
                intent.putExtras(extra);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
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
     * refresh della toolbar, non aggiorna dal database
     */
    private void refreshToolbar() {
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        ctl.setTitle(percorsoAttivo.getNome());
        categoria.setText(percorsoAttivo.getCategoria());
        autista.setText(percorsoAttivo.getAutista());
        mezzo.setText(percorsoAttivo.getMezzo());
        note.setText(percorsoAttivo.getNote());
    }

    /**
     * riordina le posizioni dell'adapter in base ai cambiamenti.
     * chiamare se è necessario aggiornare la lista
     * NON modifica il database
     */
    private void refreshVisualOrder() {
        if (!isOrderModified)
            return;
        adapter.setNumberPosizioni();
        adapter.notifyDataSetChanged();
    }

    /**
     * salva il nuovo ordine sul database
     */
    private void saveOrder() {
        if (!isOrderModified)
            return;
        Posizione[] daModificare = new Posizione[percorsoAttivo.getPosizioni().size()];
        for (int i = 0; i < percorsoAttivo.getPosizioni().size(); i++) {// TODO: 10/11/2016 possibile errore ciclo
            daModificare[i] = percorsoAttivo.getPosizioni().get(i);
        }
        new DatatbaseInteraction(Utils.UPDATE_POSITION_ORDER).execute(daModificare);
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

    //CLASSE PER GESTIRE IN BACKGROUND L'INTERAZIONE CON IL DATABASE
    private class DatatbaseInteraction extends AsyncTask<Posizione, Void, Boolean> {
        private int operation;

        /**
         * crea l'istanza per l'interazione con il database
         *
         * @param operation costante che indica l'operazione da esegure (vedi classe Utils)
         */
        public DatatbaseInteraction(int operation) {
            super();
            this.operation = operation;
        }

        @Override
        protected Boolean doInBackground(Posizione[] posizioni) {
            boolean result = false;
            try {
                database.openToWrite();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            switch (operation) {
                case Utils.DELETE_POSITION: {
                    result = (-1 != database.deletePosizione(posizioni[0]));
                    break;
                }
                case Utils.UPDATE_POSITION: {
                    result = database.updatePosizione(posizioni[0]);
                    break;
                }
                case Utils.UPDATE_POSITION_ORDER: {
                    for (Posizione aPosizioni : posizioni)
                        database.updateNumeroPosizione(aPosizioni);
                    result = true;
                    break;
                }
                case Utils.UPDATE_ALL_POSITIONS: {
                    for (Posizione aPosizioni : posizioni)
                        database.updatePosizione(aPosizioni);
                    result = true;
                    break;
                }
                case Utils.GET_PERCORSO_COMPLETO: {
                    percorsoAttivo = database.getPercorsoCompleto(percorsoAttivo.getId());
                    result = true;
                    break;
                }
                case Utils.UPDATE_PERCORSO_INFO:{
                    database.updatePercorso(percorsoAttivo);
                    result =true;
                    break;
                }
            }
            database.close();
            return result;
        }
    }
}


