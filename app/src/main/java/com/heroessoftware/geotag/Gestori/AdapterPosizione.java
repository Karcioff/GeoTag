package com.heroessoftware.geotag.Gestori;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.heroessoftware.geotag.Activities.ModificaPosizioneActivity;
import com.heroessoftware.geotag.Posizione;
import com.heroessoftware.geotag.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roberto on 19/10/2016.
 */

public class AdapterPosizione extends RecyclerView.Adapter<AdapterPosizione.ViewHolder>  {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView etichettaTextView, indirizzoTextView, utenteText, orarioText, coordinateText, numeroText;
        public Button modificaBt;
        public View details;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView) {
            super(itemView);
            details = itemView.findViewById(R.id.details);
            etichettaTextView = (TextView) itemView.findViewById(R.id.etichettaText);
            indirizzoTextView = (TextView) itemView.findViewById(R.id.indirizzoText);
            utenteText = (TextView) itemView.findViewById(R.id.utenteText);
            orarioText = (TextView) itemView.findViewById(R.id.orarioText);
            coordinateText = (TextView) itemView.findViewById(R.id.coodinateText);
            numeroText =(TextView) itemView.findViewById(R.id.numeroText);
            modificaBt = (Button) itemView.findViewById(R.id.modifica_bt);
        }


    }


    // ---------------------------------------------------------------------------------------------
    private List<Posizione> posizioni;
    private int mExpandedPosition = -1;

    // Pass in the contact array into the constructor
    public AdapterPosizione(List<Posizione> unaLista) {
        posizioni = unaLista;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View portView = inflater.inflate(R.layout.posizione_card, parent, false);
        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(portView);

        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Posizione pos = posizioni.get(position);
        // Set item views based on the data model
        TextView etichetta = viewHolder.etichettaTextView;
        etichetta.setText(pos.getEtichetta());
        TextView indirizzo = viewHolder.indirizzoTextView;
        indirizzo.setText(pos.getIndirizzo());
        TextView utente = viewHolder.utenteText;
        utente.setText(pos.getUtente());
        viewHolder.orarioText.setText(pos.getOrario());
        viewHolder.numeroText.setText(""+pos.getNumero());
        viewHolder.coordinateText.setText(Posizione.coordinatesToString(pos.getCoordinate()));
        final int position1 = viewHolder.getAdapterPosition();
        final boolean isExpanded = position == mExpandedPosition;
        viewHolder.details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        viewHolder.itemView.setActivated(isExpanded);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpandedPosition = isExpanded ? -1 : position1;
                // TODO: 01/11/2016 set transitions
                //TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });
        viewHolder.modificaBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ModificaPosizioneActivity.class);
                Bundle extra = new Bundle();
                extra.putParcelable(ModificaPosizioneActivity.KEY_POSOZIONE_MODIFICARE, pos);
                extra.putInt(ModificaPosizioneActivity.KEY_NUMERO_POSIZIONI_PERCORSO,posizioni.size());
                intent.putExtras(extra);
                v.getContext().startActivity(intent);
            }
        });
    }

    public List<Posizione> getLista(){
        return  posizioni;
    }
    /**
     * rimuove una posizione dalla lista
     *
     * @param position
     */
    public void remove(int position) {
        posizioni.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * inverte due posizioni della lista
     * @param fromPosition
     * @param toPosition
     * @return true se invertiti
     */
    public boolean move(int fromPosition, int toPosition){
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(posizioni, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(posizioni, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    /**
     * assegna un numero alle posizioni in base al loro ordinamento nell'adapter
     */
    public void setNumberPosizioni(){
        int i=1;
        for (Posizione pos: posizioni){
            pos.setNumero(i);
            i++;
        }
    }

    public void add(int position, Posizione posizione) {
        posizioni.add(position, posizione);
        notifyItemInserted(position);
    }

    /**
     * imposta la lista dell'adapter
     *
     * @param lista
     */
    public void setLista(ArrayList<Posizione> lista) {
        posizioni = lista;
        notifyDataSetChanged();
    }


    // Return the total count of items
    @Override
    public int getItemCount() {
        return posizioni.size();
    }
}
