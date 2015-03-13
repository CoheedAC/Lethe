package com.cs48.lethe.ui.more;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cs48.lethe.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 *  More Fragment that contains all accessory options
 *  (Link to facebook, link to instagram, etc. for the application.
 *
 *  @Dylan Lynch
 *  @version WIP
 */
public class MoreFragment extends Fragment {

    // Logcat tag
    public static final String TAG = MoreFragment.class.getSimpleName();

    @InjectView(R.id.facebookButton)
    Button mFacebookButton;
    @InjectView(R.id.instagramButton)
    Button mInstagramButton;
    @InjectView(R.id.contactButton)
    Button mContactButton;
    @InjectView(R.id.tosButton)
    Button mTermsOfServiceButton;
    @InjectView(R.id.ppButton)
    Button mPrivacyPolicyButton;

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity)
     * and before onCreateView(LayoutInflater, ViewGroup, Bundle). Note that this can be called
     * while the fragment's activity is still in the process of being created. As such, you can
     * not rely on things like the activity's content view hierarchy being initialized at this point.
     *
     * @param savedInstanceState If the fragment is being re-created from a
     *                           previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Called to have the fragment instantiate its user interface view. This
     * will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     * If you return a View from here, you will later be called in
     * onDestroyView() when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used
     *                 to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the
     *                  fragment's UI should be attached to. The fragment
     *                  should not add the view itself, but this can be
     *                  used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more, container, false);

        ButterKnife.inject(this, rootView);

        mFacebookButton.setOnClickListener(new OnFacebookButtonClick());
        mInstagramButton.setOnClickListener(new OnInstagramButtonClick());
        mContactButton.setOnClickListener(new OnContactUsButtonClick());

        return rootView;
    }

    /**
     * Listener for facebook, opens to facebook URL
     */
    private class OnFacebookButtonClick implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.facebook.com/I5555")));
        }
    }

    /**
     * Listener for Instagram, opens Instagram URL
     */
    private class OnInstagramButtonClick implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://instagram.com/lynchifer/")));

        }
    }

    /**
     * Listener for Contact Us button, prompts user to pick email app,
     * sends email with mailto field already filled in
     */
    private class OnContactUsButtonClick implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, "dlynch305@gmail.com");

            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        }
    }

    /**
     * Listener for showing the Privacy Policy when clicked
     */
    private class OnPrivacyPolicyButtonClick implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            //TODO Open Privacy Policy text
        }
    }

    /**
     * Listener for showing the Terms of Service when clicked
     */
    private class OnTermsOfServiceButtonClick implements View.OnClickListener {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            //TODO Open Terms of Service Text
        }
    }

}
