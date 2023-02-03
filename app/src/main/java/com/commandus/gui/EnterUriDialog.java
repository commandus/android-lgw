package com.commandus.gui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.commandus.lgw.LgwSettings;
import com.commandus.lgw.R;

public class EnterUriDialog extends DialogFragment {
    private final String mLastUri;
    private final LgwSettings lgwSettings;

    public EnterUriDialog() {
        lgwSettings = LgwSettings.getSettings(getContext());
        mLastUri = lgwSettings.getLoadLastUri();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        if (context == null)
            return null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editTextField = new EditText(this.getContext());
        builder.setTitle(R.string.enter_uri_dialog_title)
                .setView(editTextField)
                // Set the action buttons
                .setPositiveButton(R.string.load, (dialog, id) -> {
                    if (listener != null) {
                        listener.onSetUri(editTextField.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                });

        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EnterUriListener {
        void onSetUri(String value);
    }

    // Use this instance of the interface to deliver action events
    EnterUriListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (EnterUriListener) context;
        } catch (ClassCastException ignored) {
        }
    }
}
