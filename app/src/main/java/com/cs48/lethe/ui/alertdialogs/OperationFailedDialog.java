package com.cs48.lethe.ui.alertdialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by maxkohne on 2/21/15.
 */
public class OperationFailedDialog extends DialogFragment {

    /**
     * Override to build your own custom Dialog container. This is typically
     * used to show an AlertDialog instead of a generic Dialog;
     *
     * @param savedInstanceState The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Operation Failed")
                .setMessage("Could not complete operation.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
