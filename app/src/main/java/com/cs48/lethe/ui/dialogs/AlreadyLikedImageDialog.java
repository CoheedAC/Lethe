package com.cs48.lethe.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by maxkohne on 2/22/15.
 */
public class AlreadyLikedImageDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Photo Already Liked")
                .setMessage("You have already liked this photo.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
