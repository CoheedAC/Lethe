package com.cs48.lethe.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cs48.lethe.R;

/**
 *  More Fragment that contains all accessory options
 *  (Link to facebook, link to instagram, etc. for the application.
 *
 *  @Dylan Lynch
 *  @version WIP
 *
 *
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MoreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MoreFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    // Fragment Buttons
    Button mFacebookButton;
    Button mInstagramButton;
    Button mContactButton;
    Button mTOSButton;
    Button mPPButton;

    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
        return fragment;
    }

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * onCreate() is used for normal static setup of the activity
     *
     * @param savedInstanceState Saved state data passed into onCreate
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_more, container, false);

        // Initialize fragment buttons
        mFacebookButton = (Button) view.findViewById(R.id.facebookButton);
        mInstagramButton = (Button) view.findViewById(R.id.instagramButton);
        mContactButton = (Button) view.findViewById(R.id.contactButton);
        mTOSButton = (Button) view.findViewById(R.id.tosButton);
        mPPButton = (Button) view.findViewById(R.id.ppButton);

        // Listener for facebook, opens to facebook URL
        View.OnClickListener facebookHandler = new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.facebook.com/I5555")));
            }
        };
        // Listener for Instagram, opens Instagram URL
        View.OnClickListener instagramHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://instagram.com/lynchifer/")));

            }
        };

        // Listener for Contact Us button, prompts user to pick email app, sends email with mailto field already filled in
        View.OnClickListener contactUsHandler = new View.OnClickListener() {
           @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, "dlynch305@gmail.com");

                startActivity(Intent.createChooser(intent, "Send Email"));
            }
        };

        // Listener for showing the Terms of Service when clicked
        View.OnClickListener tosHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Open Terms of Service Text
            }
        };

        // Listener for showing the Privacy Policy when clicked
        View.OnClickListener ppHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Open Privacy Policy text
            }
        };



        mFacebookButton.setOnClickListener(facebookHandler);
        mInstagramButton.setOnClickListener(instagramHandler);
        mContactButton.setOnClickListener(contactUsHandler);

        return view;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
