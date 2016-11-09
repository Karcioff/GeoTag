package com.heroessoftware.geotag.Fragment;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import com.heroessoftware.geotag.Posizione;

import java.util.Calendar;

/**
 * Created by Roberto on 02/11/2016.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    private Posizione posizione;

    public void initialize(Posizione unaPosizione) {
        posizione = unaPosizione;
    }

    public static interface OnSelectHourListener {
        void onHourSelected(String hour);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String minute1 = ""+ minute;
        if(minute<10)
            minute1= "0"+ minute;
        OnSelectHourListener listener= (OnSelectHourListener) getActivity();
        listener.onHourSelected(hourOfDay+":"+minute1);
    }
}
