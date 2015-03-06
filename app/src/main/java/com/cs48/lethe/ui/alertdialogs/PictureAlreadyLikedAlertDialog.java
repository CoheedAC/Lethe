package com.cs48.lethe.ui.alertdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * An alert dialog pertaining to a picture like error.
 */
public class PictureAlreadyLikedAlertDialog extends DialogFragment {

    /**
     * Builds an alert dialog that displays an error that the picture
     * has already been liked by the user.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Photo Already Liked")
                .setMessage("You have already liked this photo.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
