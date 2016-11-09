package com.heroessoftware.geotag;

import android.os.Parcel;
import android.os.Parcelable;

import com.heroessoftware.geotag.Gestori.GestoreDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Roberto on 18/10/2016.
 */

public class Percorso implements Parcelable {
    private String nome;
    private String categoria;
    private String autista;
    private String mezzo;
    private String note;
    private long id;
    private ArrayList<Posizione> posizioni;

    /**
     * costruttore senza id
     *
     * @param unNome
     * @param unaCategoria
     * @param unAutista
     * @param unMezzo
     * @param delleNote
     */
    public Percorso(String unNome, String unaCategoria, String unAutista, String unMezzo, String delleNote) {
        nome = unNome;
        categoria = unaCategoria;
        autista = unAutista;
        mezzo = unMezzo;
        note = delleNote;
    }


    /**
     * costruttore con id, per interazioni con database
     *
     * @param unNome
     * @param unaCategoria
     * @param unAutista
     * @param unMezzo
     * @param delleNote
     * @param unId
     */
    public Percorso(String unNome, String unaCategoria, String unAutista, String unMezzo, String delleNote, long unId) {
        nome = unNome;
        categoria = unaCategoria;
        autista = unAutista;
        mezzo = unMezzo;
        note = delleNote;
        id = unId;
    }

    /**
     * costruttore completo
     *
     * @param unNome
     * @param unaCategoria
     * @param unAutista
     * @param unMezzo
     * @param delleNote
     * @param unId
     * @param dellePosizioni
     */
    public Percorso(String unNome, String unaCategoria, String unAutista, String unMezzo, String delleNote, long unId, ArrayList<Posizione> dellePosizioni) {
        nome = unNome;
        categoria = unaCategoria;
        autista = unAutista;
        mezzo = unMezzo;
        note = delleNote;
        id = unId;
        posizioni = dellePosizioni;
    }

    /**
     * restituisce il nome del percorso
     *
     * @return nome percorso
     */
    public String getNome() {
        return nome;
    }

    /**
     * assegna un nome percorso
     *
     * @param unNome
     */
    public void setNome(String unNome) {
        nome = unNome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getAutista() {
        return autista;
    }

    public void setAutista(String autista) {
        this.autista = autista;
    }

    public String getMezzo() {
        return mezzo;
    }

    public void setMezzo(String mezzo) {
        this.mezzo = mezzo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /**
     * restituisce l'id identificativo del percorso
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * assegna un id al percorso
     *
     * @param unId
     */
    public void setId(long unId) {
        id = unId;
    }

    /**
     * restituisce la lista delle posizioni appartenenti al percorso
     *
     * @return lista posizioni
     */
    public ArrayList<Posizione> getPosizioni() {
        return posizioni;
    }

    /**
     * assegna una lista di posizioni ad un percorso
     *
     * @param unaLista
     */
    public void setPosizioni(ArrayList<Posizione> unaLista) {
        posizioni = unaLista;
    }

    /**
     * aggiunge una posizione al percorso solo se non è già presente un altra dalle stesse coordinate.
     * sia all'oggetto sia al database.
     *
     * @param unaPosizione
     * @param database
     * @return true se inserito, false se non inserito
     */
    public boolean addPosizione(Posizione unaPosizione, GestoreDatabase database) {
        for (Posizione pos : posizioni) {
            if (pos.getCoordinate().equals(unaPosizione.getCoordinate()))
                return false;
        }
        if (database.addPosizione(unaPosizione) != -1) {
            posizioni.add(unaPosizione);
            return true;
        }
        return false;
    }

    /**
     * cancella una posizione dal percorso, sia dal database che dall'oggetto
     *
     * @param unaPosizione
     * @param database
     * @return true se rimossa false altrimenti
     */
    public boolean deletePosizione(Posizione unaPosizione, GestoreDatabase database) {
        if (database.deletePosizione(unaPosizione) != -1) {
            posizioni.remove(unaPosizione);
            return true;
        }
        return false;
    }

    /**
     * restituisce la stringa rappresentante un percorso per l'esportazione
     *
     * @return stringa da scrivere su file di testo
     */
    public String percorsoToString() {
        String result = "";
        for (Posizione pos : posizioni) {
            result = result + pos.posizioneToString()+"\n";
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (this.getClass() != obj.getClass()) return false;
        Percorso percorso = (Percorso) obj;
        if (this.getId() == percorso.getId())
            return true;
        return false;
    }

    //PARCELABLE
    public static final Parcelable.Creator<Percorso> CREATOR
            = new Parcelable.Creator<Percorso>() {
        public Percorso createFromParcel(Parcel in) {
            return new Percorso(in);
        }

        public Percorso[] newArray(int size) {
            return new Percorso[size];
        }
    };

    private Percorso(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        nome = in.readString();
        categoria = in.readString();
        autista = in.readString();
        mezzo = in.readString();
        note = in.readString();
        id = Long.parseLong(in.readString());
        posizioni = new ArrayList<Posizione>();
        in.readTypedList(posizioni, Posizione.CREATOR);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(nome);
        out.writeString(categoria);
        out.writeString(autista);
        out.writeString(mezzo);
        out.writeString(note);
        out.writeString("" + id);
        out.writeTypedList((List) posizioni);
    }
}
