package com.commandus.lgw;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialog extends DialogFragment {
    private final String mLastUri;
    private final LgwSettings lgwSettings;

    public ConfirmDeleteDialog(ConfirmationListener ctx) {
        lgwSettings = LgwSettings.getSettings(getContext());
        mLastUri = lgwSettings.getLoadLastUri();
        listener = ctx;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        if (context == null)
            return null;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirm_delete_dialog_title)
            .setMessage(R.string.msg_confirm_to_delete)
            .setPositiveButton(R.string.yes, (dialog, id) -> {
                if (listener != null) {
                    listener.confirmed();
                }
            })
            .setNegativeButton(R.string.no, (dialog, id) -> {
            });

        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EnterUriListener extends com.commandus.lgw.EnterUriListener {
        void onSetUri(String value);
    }

    // Use this instance of the interface to deliver action events
    ConfirmationListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}

