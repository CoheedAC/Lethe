package com.cs48.lethe.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by maxkohne on 2/24/15.
 */
public class DeleteImageWarningDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("DO NOT DELETE PHOTO")
                .setMessage("The copy feature does not copy the images. It makes image objects (not stored " +
                        "in the database). If you go into full screen mode and click the delete button " +
                        "the grid will only delete one grid item (not all 50). If you try to open the full " +
                        "screen view again, the app will crash. If you want to clear the grid, use the button on " +
                        "the top right. This will not crash the app. This copy feature is for TESTING purposes only.")
                .setPositiveButton("Okay", null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
