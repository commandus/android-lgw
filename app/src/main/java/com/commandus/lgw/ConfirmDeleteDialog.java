package com.commandus.lgw;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialog extends DialogFragment {

    public ConfirmDeleteDialog(ConfirmationListener ctx) {
        listener = ctx;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
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

    ConfirmationListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}

