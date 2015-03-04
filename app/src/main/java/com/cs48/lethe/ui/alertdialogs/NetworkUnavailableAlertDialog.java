package com.cs48.lethe.ui.alertdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * An alert dialog pertaining to a network error.
 */
public class NetworkUnavailableAlertDialog extends DialogFragment {

    /**
     * Builds an alert dialog that displays an error that there
     * is no internet connection.
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     *                           or null if this is a freshly created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("No Internet Connection")
                .setMessage("Cannot connect to the internet.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }

}
