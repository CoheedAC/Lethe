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
                .setTitle("Photo Already Liked")
                .setMessage("You have already liked this photo.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
