package com.commandus.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.commandus.lgw.R;

public class RegionDialog extends DialogFragment {
    private final String[] mNames;
    private int mSelected;

    public RegionDialog(String[] names, int regionIndex) {
        mNames = names;
        mSelected = regionIndex;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        if (context == null)
            return null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Set the dialog title
        builder.setTitle(R.string.region_dialog_title)
                .setSingleChoiceItems(mNames, mSelected, (dialogInterface, index) -> mSelected = index)
                // Set the action buttons
                .setPositiveButton(R.string.select, (dialog, id) -> {
                    if (listener != null) {
                        listener.onSetRegionIndex(mSelected);
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                });

        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface RegionSelectListener {
        void onSetRegionIndex(int selection);
    }

    // Use this instance of the interface to deliver action events
    RegionSelectListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RegionSelectListener) context;
        } catch (ClassCastException ignored) {
        }
    }
}
