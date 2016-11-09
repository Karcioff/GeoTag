package com.heroessoftware.geotag.Activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.ViewAnimationUtils;
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
    private GestoreDatabase database = new GestoreDatabase(this);
    private Percorso percorsoAttivo;
    private ArrayList<Percorso> percorsi;
    private Location mLastLocation;
    private boolean positionUpdate;

    public static String KEY_PERCORSO = "percorsoAttivo";
    public static int STANDARD_ZOOM = 11;


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
        refresh();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

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
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
        // TODO: 01/11/2016 errore
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
                        extra.putInt(ModificaPosizioneActivity.KEY_NUMERO_POSIZIONI_PERCORSO,percorsoAttivo.getPosizioni().size());
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
                extra.putInt(ModificaPosizioneActivity.KEY_NUMERO_POSIZIONI_PERCORSO,percorsoAttivo.getPosizioni().size());
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
                    String lista;
                    lista = attivo.getNome();
                    Toast.makeText(getApplicationContext(), lista,
                            Toast.LENGTH_SHORT).show();
                }
            });

            final AlertDialog dialog = builder.create();
            /*dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    revealShow(dialogView, true, null);
                }
            });
            dialogView.findViewById(R.id.btn_annulla).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    revealShow(dialogView, false, dialog);
                }
            });
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));*/
            dialog.show();
        } else if (id == R.id.new_percorso) {
            Bundle extra= new Bundle();
            Percorso newperc= new Percorso("","","","","");
            extra.putParcelable(ModificaPercorsoActivity.KEY_PERCORSO_MODIFICARE,newperc);
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
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }


    //REVEAL DIALOGUE

    private AlertDialog makeDialogue() {
        final View dialogView = View.inflate(this, R.layout.dialog_posizione_info, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                revealShow(dialogView, true, null);
            }
        });
        dialogView.findViewById(R.id.btn_annulla).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealShow(dialogView, false, dialog);
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();
        return dialog;
    }

    private void revealShow(View rootView, boolean reveal, final AlertDialog dialog) {
        final View view = rootView.findViewById(R.id.reveal_view);
        int w = view.getWidth();
        int h = view.getHeight();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            float maxRadius = (float) Math.sqrt(w * w / 4 + h * h / 4);

            if (reveal) {
                Animator revealAnimator = null;
                revealAnimator = ViewAnimationUtils.createCircularReveal(view,
                        w / 2, h / 2, 0, maxRadius);
                view.setVisibility(View.VISIBLE);
                revealAnimator.start();
            } else {
                Animator anim = ViewAnimationUtils.createCircularReveal(view, w / 2, h / 2, maxRadius, 0);

                anim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.INVISIBLE);
                        dialog.dismiss();
                    }
                });

                anim.start();
            }
        }
        // TODO: 21/10/2016 add animation pre lollipop
    }

    /**
     * aggiunge una posizione al percorso attivo
     *
     * @param posizione
     * @return true se aggiunta, false altrimenti
     */
    private boolean addPosizione(Posizione posizione) {
        try {
            database.openToWrite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (percorsoAttivo.addPosizione(posizione, database)) {
            Toast.makeText(getApplicationContext(), getString(R.string.posizione_memorizzata),
                    Toast.LENGTH_SHORT).show();
            database.close();
            percorsoAttivo.getPosizioni().add(posizione);
            mMap.addMarker(new MarkerOptions().position(posizione.getCoordinate()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posizione.getCoordinate(), STANDARD_ZOOM));
            return true;
        } else
            Toast.makeText(getApplicationContext(), getString(R.string.posizione_non_memorizzata),
                    Toast.LENGTH_SHORT).show();
        database.close();
        return false;
    }

    //mostra tutte le posizioni del percorso e zoomma sulla prima
    private void displayPosizioni() {
        if (!isPercorsoAttivo())
            return;
        for (Posizione pos : percorsoAttivo.getPosizioni()) {
            mMap.addMarker(new MarkerOptions().position(pos.getCoordinate()).title(pos.getEtichetta()).snippet(pos.getIndirizzo())
                    .snippet(pos.getStringCoordinate()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(percorsoAttivo.getPosizioni().get(0).getCoordinate(), STANDARD_ZOOM));
        }
    }

    /**
     * refresh dell'interfaccia
     */
    private void refresh() {
        //carica dal database e aggiorna l'interfaccia // TODO: 19/10/2016 implementare scelta
        try {
            database.openToRead();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        percorsi = database.getListaPercorsi();
        if (percorsoAttivo == null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
            return;
        }
        boolean ok = false;
        for (Percorso p : percorsi)
            if (p.equals(percorsoAttivo))
                ok = true;
        if (ok) {
            percorsoAttivo = database.getPercorsoCompleto(percorsoAttivo.getId());
            getSupportActionBar().setTitle(percorsoAttivo.getNome());
        } else {
            percorsoAttivo = null;
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
        database.close();
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
     *
     * @param percorso attivo
     */
    private void setPercorsoAttivo(Percorso percorso) {
        try {
            database.openToRead();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        percorsoAttivo = database.getPercorsoCompleto(percorso.getId());
        database.close();
        getSupportActionBar().setTitle(percorsoAttivo.getNome());
        mMap.clear();
        displayPosizioni();
    }

}
