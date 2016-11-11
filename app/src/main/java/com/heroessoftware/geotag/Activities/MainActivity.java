package com.heroessoftware.geotag.Activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.heroessoftware.geotag.Gestori.GestoreDatabase;
import com.heroessoftware.geotag.Gestori.Utils;
import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.Posizione;
import com.heroessoftware.geotag.R;

import java.sql.SQLException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback, PopupMenu.OnMenuItemClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabAddFinished;
    private Percorso percorsoAttivo;
    private ArrayList<Percorso> percorsi;
    private Location mLastLocation;
    private boolean positionUpdate;
    private GestoreDatabase database = new GestoreDatabase(this);

    public static String KEY_PERCORSO = "percorsoAttivo";

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(Utils.PERCORSO_MODIFIED)) {
                percorsoAttivo = intent.getExtras().getParcelable(Utils.KEY_PERCORSO_MODIFICATO);
            } else if (action.equalsIgnoreCase(Utils.CHANGED_POSITION_ORDER)) {
                percorsoAttivo = intent.getExtras().getParcelable(Utils.KEY_PERCORSO_MODIFICATO);
            }
        }
    };

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        positionUpdate = false;
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(
                mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !positionUpdate) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            positionUpdate = true;
        }
        refreshInterface();
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Utils.CHANGED_POSITION_ORDER));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(Utils.DELETE_POSITION_INTENT));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        refreshComplete();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //BOTTONi
        fabAdd = (FloatingActionButton) findViewById(R.id.fab);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPercorsoAttivo())
                    return;
                showPopup(v);
            }
        });
        fabAddFinished = (FloatingActionButton) findViewById(R.id.fab_fine_aggiunta);
        showButtonAddFinish(false);
        fabAddFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStandardMarkerListener();
                showButtonAdd(true); // TODO: 22/10/2016 transition
                showButtonAddFinish(false);
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(getApplicationContext(), getString(R.string.warning_no_connection),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng));
            }
        });
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    //sets standard Marker click
    private void setStandardMarkerListener() {
        mMap.setOnMarkerClickListener(this);
    }

    //BUTTONS METHODS

    /**
     * mostra il primo bottone
     *
     * @param show
     */
    public void showButtonAdd(Boolean show) {
        if (show) // TODO: 22/10/2016 animation
            fabAdd.setVisibility(View.VISIBLE);
        else
            fabAdd.setVisibility(View.GONE);
    }

    /**
     * mostra il secondo bottone
     *
     * @param show
     */
    private void showButtonAddFinish(Boolean show) {
        if (show) // TODO: 22/10/2016 animation
            fabAddFinished.setVisibility(View.VISIBLE);
        else
            fabAddFinished.setVisibility(View.GONE);
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.actions, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_indirizzo:
                Toast.makeText(getApplicationContext(), "work in progress, la vostra pazienza sarà ricompensata",
                        Toast.LENGTH_SHORT).show();
                return true;

            case R.id.add_marker:

                showButtonAdd(false);
                showButtonAddFinish(true);
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        final LatLng coordinate = marker.getPosition();
                        Posizione newPos = new Posizione("", "", coordinate, "", "", 0, percorsoAttivo.getId());
                        Intent intent = new Intent(getBaseContext(), ModificaPosizioneActivity.class);
                        Bundle extra = new Bundle();
                        extra.putParcelable(ModificaPosizioneActivity.KEY_POSOZIONE_MODIFICARE, newPos);
                        extra.putInt(ModificaPosizioneActivity.KEY_NUMERO_POSIZIONI_PERCORSO, percorsoAttivo.getPosizioni().size());
                        intent.putExtras(extra);
                        startActivity(intent);
                        return true;
                    }
                });
                return true;

            case R.id.add_pos:
                final LatLng coordinate = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Posizione newPos = new Posizione("", "", coordinate, "", "", 0, percorsoAttivo.getId());
                Intent intent = new Intent(getBaseContext(), ModificaPosizioneActivity.class);
                Bundle extra = new Bundle();
                extra.putParcelable(ModificaPosizioneActivity.KEY_POSOZIONE_MODIFICARE, newPos);
                extra.putInt(ModificaPosizioneActivity.KEY_NUMERO_POSIZIONI_PERCORSO, percorsoAttivo.getPosizioni().size());
                intent.putExtras(extra);
                startActivity(intent);
                return true;
        }
        return false;
    }


    // TODO: 17/10/2016 controllo permessi

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.remove_marker) {
            mMap.clear();
        } else if (id == R.id.open_posizioni_activity) {
            if (isPercorsoAttivo()) {
                Intent intent = new Intent(this, PosizioniActivity.class);
                Bundle extra = new Bundle();
                extra.putParcelable(KEY_PERCORSO, percorsoAttivo);
                intent.putExtras(extra);
                startActivity(intent);
            }
        } else if (id == R.id.show_posizioni) {
            displayPosizioni();
        } else if (id == R.id.select_pecorso) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_percorso);
            String[] nomi = new String[percorsi.size()];
            for (int i = 0; i < percorsi.size(); i++) {
                nomi[i] = percorsi.get(i).getNome();
            }
            builder.setItems(nomi, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Percorso attivo = percorsi.get(which);
                    setPercorsoAttivo(attivo);
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();

        } else if (id == R.id.new_percorso) {
            Bundle extra = new Bundle();
            Percorso newperc = new Percorso("", "", "", "", "");
            extra.putParcelable(Utils.KEY_PERCORSO_MODIFICARE, newperc);
            Intent intent = new Intent(this, ModificaPercorsoActivity.class);
            intent.putExtras(extra);
            startActivity(intent);
        } else if (id == R.id.settings) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.heroessoftware.altervista.org/"));
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
        positionUpdate = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    mMap.setMyLocationEnabled(true);
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);

                } else {
                    Toast.makeText(getApplicationContext(), "non hai ancora i permessi",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        positionUpdate = false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    /**
     * aggiunge una posizione al percorso attivo e al database
     *
     * @param posizione
     * @return true se aggiunta, false altrimenti
     */
    private boolean addPosizione(Posizione posizione) {
        percorsoAttivo.getPosizioni().add(posizione);
        mMap.addMarker(new MarkerOptions().position(posizione.getCoordinate()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posizione.getCoordinate(), Utils.STANDARD_ZOOM));
        new DatatbaseInteraction(Utils.ADD_POSITION).execute(posizione);// TODO: 11/11/2016 aggiungere messaggio utente
        return true;
    }

    //mostra tutte le posizioni del percorso e zoomma sulla prima
    private void displayPosizioni() {
        mMap.clear();
        if (!isPercorsoAttivo())
            return;
        for (Posizione pos : percorsoAttivo.getPosizioni()) {
            mMap.addMarker(new MarkerOptions().position(pos.getCoordinate()).title(pos.getEtichetta()).snippet(pos.getIndirizzo())
                    .snippet(pos.getStringCoordinate()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(percorsoAttivo.getPosizioni().get(0).getCoordinate(), Utils.STANDARD_ZOOM));
        }
    }

    /**
     * refresh dell'interfaccia dal database
     */
    private void refreshInterface() {

        if (percorsoAttivo == null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            return;
        }else{
            getSupportActionBar().setTitle(percorsoAttivo.getNome());
        } displayPosizioni();
    }

    /**
     * refresh dell'interfaccia dal database
     */
    private void refreshComplete() {

        new DatatbaseInteraction(Utils.GET_PERCORSI_DISPLAY).execute();
        if (percorsoAttivo == null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            return;
        }
        boolean ok = false;
        for (Percorso p : percorsi)
            if (p.equals(percorsoAttivo))
                ok = true;
        if (ok) {
            new DatatbaseInteraction(Utils.GET_PERCORSO_COMPLETO).execute();
            getSupportActionBar().setTitle(percorsoAttivo.getNome());
        } else {
            percorsoAttivo = null;
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        displayPosizioni();
    }


    private boolean isPercorsoAttivo() {
        if (percorsoAttivo == null) {
            Toast.makeText(getApplicationContext(), R.string.warning_no_percorso_attivo,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * setta un percorso attivo e aziona tutti gli aggiornamenti necessari all'interfaccia
     * da chiamare prima di caricare il percorso dal database
     *
     * @param percorso attivo
     */
    private void setPercorsoAttivo(Percorso percorso) {
        percorsoAttivo = percorso;
        new DatatbaseInteraction(Utils.GET_PERCORSO_COMPLETO).execute();
        getSupportActionBar().setTitle(percorsoAttivo.getNome());
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
                case Utils.GET_PERCORSI_DISPLAY: {
                    percorsi = database.getListaPercorsi();
                    result = true;
                    break;
                }
                case Utils.GET_PERCORSO_COMPLETO: {
                    percorsoAttivo = database.getPercorsoCompleto(percorsoAttivo.getId());
                    result = true;
                    break;
                }
                case Utils.UPDATE_PERCORSO_INFO: {
                    database.updatePercorso(percorsoAttivo);
                    result = true;
                    break;
                }
            }
            database.close();
            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(operation==Utils.GET_PERCORSO_COMPLETO)
                displayPosizioni();
        }
    }
}
