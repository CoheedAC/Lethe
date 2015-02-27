package com.cs48.lethe.ui.fragments;

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

    public static final String LOG_TAG = MoreFragment.class.getSimpleName();

    @InjectView(R.id.facebookButton)
    Button mFacebookButton;
    @InjectView(R.id.instagramButton)
    Button mInstagramButton;
    @InjectView(R.id.contactButton)
    Button mContactButton;
    @InjectView(R.id.tosButton)
    Button mTOSButton;
    @InjectView(R.id.ppButton)
    Button mPPButton;

    /**
     * onCreate() is used for normal static setup of the activity
     *
     * @param savedInstanceState Saved state data passed into onCreate
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_more, container, false);

        ButterKnife.inject(this, rootView);

        mFacebookButton.setOnClickListener(new OnFacebookClickListener());
        mInstagramButton.setOnClickListener(new OnInstagramClickListener());
        mContactButton.setOnClickListener(new OnContactUsClickListener());

        return rootView;
    }

    /**
     * Listener for facebook, opens to facebook URL
     */
    class OnFacebookClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.facebook.com/I5555")));
        }
    }

    /**
     * Listener for Instagram, opens Instagram URL
     */
    class OnInstagramClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://instagram.com/lynchifer/")));

        }
    }

    /**
     * Listener for Contact Us button, prompts user to pick email app,
     * sends email with mailto field already filled in
     */
    class OnContactUsClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, "dlynch305@gmail.com");

            startActivity(Intent.createChooser(intent, "Send Email"));
        }
    }

    /**
     * Listener for showing the Privacy Policy when clicked
     */
    class OnPrivacyPolicyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //TODO Open Privacy Policy text
        }
    }

    /**
     * Listener for showing the Terms of Service when clicked
     */
    class OnTermsOfServiceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //TODO Open Terms of Service Text
        }
    }

}
