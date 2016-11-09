package com.heroessoftware.geotag.Gestori;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.heroessoftware.geotag.Percorso;
import com.heroessoftware.geotag.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by Roberto on 01/11/2016.
 */

public class ProcessoEsportazione extends AsyncTask<Percorso, Integer, String> {
    private Percorso percorso;
    private String nomeFile;
    private String content;
    private Context context;

    public ProcessoEsportazione(Context unContext) {
        context = unContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isExternalStorageWritable()) {
            Toast.makeText(context, context.getString(R.string.memoria_non_disponibile),
                    Toast.LENGTH_SHORT).show();
            cancel(true);
        }
    }

    @Override
    protected String doInBackground(Percorso... params) {
        percorso = params[0];
        nomeFile = percorso.getNome() + ".csv";
        content = percorso.percorsoToString();
        if (isCancelled())
            return null;
        //get the path to sdcard
        File pathToExternalStorage = Environment.getExternalStorageDirectory();
        //to this path add a new directory path and create new App dir (InstroList) in /documents Dir
        File appDirectory = new File(pathToExternalStorage.getAbsolutePath() + "/documents/GeoTag");
        // have the object build the directory structure, if needed.
        appDirectory.mkdirs();
        //Create a File for the output file data
        File saveFilePath = new File(appDirectory, nomeFile);
        try {
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            OutputStreamWriter out = new OutputStreamWriter(fos);
            out.append(content);
            out.close();
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getString(R.string.file_salvato) + " documents/GeoTag";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(context, s,
                Toast.LENGTH_SHORT).show();
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}


