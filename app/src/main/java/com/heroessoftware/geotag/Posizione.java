package com.heroessoftware.geotag;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Roberto on 17/10/2016.
 */

public class Posizione implements Parcelable, Comparable<Posizione> {
    private LatLng coordinate;
    private String etichetta;
    private String indirizzo;
    private String utente;
    private String orario;  // TODO: 30/10/2016 orario come stringa?
    private int numero;
    private long posizioneId;
    private long percorsoId;

    //COSTRUTTORI

    /**
     * costruttore senza id posizione
     * da usare per inserire valori nel database
     *
     * @param unaEtichetta
     * @param unIndirizzo
     * @param delleCoordinate
     * @param unUtente
     * @param unOrario
     * @param unNumero
     * @param unPercorso
     */
    public Posizione(String unaEtichetta, String unIndirizzo, LatLng delleCoordinate, String unUtente, String unOrario, int unNumero, long unPercorso) {
        etichetta = unaEtichetta;
        indirizzo = unIndirizzo;
        coordinate = delleCoordinate;
        utente = unUtente;
        orario = unOrario;
        numero = unNumero;
        percorsoId = unPercorso;
    }

    /**
     * costruttore completo, per interagire con database
     *
     * @param unaEtichetta
     * @param unIndirizzo
     * @param delleCoordinate
     * @param unOrario
     * @param unNumero
     * @param unPercorso
     * @param unId
     */
    public Posizione(String unaEtichetta, String unIndirizzo, LatLng delleCoordinate, String unUtente, String unOrario, int unNumero, long unPercorso, long unId) {
        etichetta = unaEtichetta;
        indirizzo = unIndirizzo;
        coordinate = delleCoordinate;
        utente = unUtente;
        orario = unOrario;
        numero = unNumero;
        percorsoId = unPercorso;
        posizioneId = unId;
    }

    /**
     * ritorna le coordinate gps
     *
     * @return coordinate
     */
    public LatLng getCoordinate() {
        return coordinate;
    }

    /**
     * restituisce l'etichetta
     *
     * @return etichetta
     */
    public String getEtichetta() {
        return etichetta;
    }

    /**
     * cambia le coordinate
     *
     * @param coo
     */
    public void setCoordinate(LatLng coo) {
        coordinate = coo;
    }

    /**
     * cambia l'etichetta
     *
     * @param ett
     */
    public void setEtichetta(String ett) {
        etichetta = ett;
    }

    /**
     * restituisce l'indirizzo
     *
     * @return indirizzo
     */
    public String getIndirizzo() {
        return indirizzo;
    }

    /**
     * cambia l'indirizzo
     *
     * @param ind
     */
    public void setIndirizzo(String ind) {
        indirizzo = ind;
    }

    /**
     * cambia l'utente o lo aggiunge
     *
     * @param unUtente
     */
    public void setUtente(String unUtente) {
        utente = unUtente;
    }

    /**
     * restituisce l'utente
     *
     * @return utente assegnato alla posizione
     */
    public String getUtente() {
        return utente;
    }

    /**
     * setta l'orario
     *
     * @param unOrario
     */
    public void setOrario(String unOrario) {
        orario = unOrario;
    }

    /**
     * restituisce l'orario
     *
     * @return orario associato alla posizione
     */
    public String getOrario() {
        return orario;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    /**
     * restituisce il numero del percorso associato
     *
     * @return id percorso
     */
    public long getIdPercorso() {
        return percorsoId;
    }

    /**
     * cambia l'id del percorso associato
     *
     * @param id
     */
    public void setIdPercorso(long id) {
        percorsoId = id;
    }

    /**
     * restituisce l'id della posizione
     *
     * @return id posizione
     */
    public long getIdPosizione() {
        return posizioneId;
    }

    /**
     * restituisce una stringa con le coordinate
     *
     * @return
     */
    public String getStringCoordinate() {
        return Posizione.coordinatesToString(coordinate);
    }

    /**
     * converte una stringa in coordinate
     *
     * @param coor string
     * @return coordiante LatLng
     */
    public static LatLng stringToCoordinate(String coor) {
        String[] latlong = coor.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        LatLng location = new LatLng(latitude, longitude);
        return location;
    }

    /**
     * converte coordinate in stringhe dal formato lat,long
     *
     * @param delleCoordinate
     * @return stringa coordinate
     */
    public static String coordinatesToString(LatLng delleCoordinate) {
        String lat = "" + delleCoordinate.latitude;
        String lng = "" + delleCoordinate.longitude;
        String result = lat + "," + lng;
        return result;
    }

    /**
     * restituisce una stringa che rappresenta la posizione per l'esportazione
     * ogni attibuto è separato da ";"
     *
     * @return etichetta, indirizzo, coordinate, utente, orario
     */
    public String posizioneToString() {
        String result;
        result = etichetta + ";" + indirizzo + ";" + coordinatesToString(coordinate).replace(",", ";").replace(".", ",") + ";" + utente + ";" + orario;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (this.getClass() != obj.getClass()) return false;
        Posizione pos = (Posizione) obj;
        if (this.getIdPosizione() == pos.getIdPosizione())
            return true;
        return false;
    }

    //PARCELABLE
    public static final Parcelable.Creator<Posizione> CREATOR
            = new Parcelable.Creator<Posizione>() {
        public Posizione createFromParcel(Parcel in) {
            return new Posizione(in);
        }

        public Posizione[] newArray(int size) {
            return new Posizione[size];
        }
    };

    public Posizione(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        etichetta = in.readString();
        indirizzo = in.readString();
        coordinate = stringToCoordinate(in.readString());
        utente = in.readString();
        orario = in.readString();
        numero= Integer.parseInt(in.readString());
        posizioneId = Long.parseLong(in.readString());
        percorsoId = Long.parseLong(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(etichetta);
        out.writeString(indirizzo);
        out.writeString(Posizione.coordinatesToString(coordinate));
        out.writeString(utente);
        out.writeString(orario);
        out.writeString(""+numero);
        out.writeString("" + posizioneId);
        out.writeString("" + percorsoId);
    }

    @Override
    public int compareTo(Posizione o) {
        if (o.getNumero() == getNumero())
            return 0;
        if (o.getNumero() < getNumero())
            return -1;
        else
            return 1;
    }
}

