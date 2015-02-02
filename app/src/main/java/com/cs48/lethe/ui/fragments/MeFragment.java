package com.cs48.lethe.ui.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.cs48.lethe.R;
import com.cs48.lethe.ui.adapters.MeGridViewAdapter;
import com.cs48.lethe.utils.FileUtilities;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeFragment extends Fragment {

    private MeGridViewAdapter mGridAdapter;
    private GridView mGridView;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MeFragment.
     */
    public static MeFragment newInstance() {
        return new MeFragment();
    }

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.meGridView);
        mGridView.setAdapter(new MeGridViewAdapter(getActivity()));

        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.getItem(0).setVisible(true);
        menu.getItem(1).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_images) {
            FileUtilities.deleteAllImages(getActivity());
            updateGridView();
            Toast.makeText(getActivity(), "Deleted all images", Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_copy_images) {
            try {
                File[] files = FileUtilities.listFiles(getActivity());
                FileUtilities.copyFile(getActivity(), files[0].getAbsolutePath(), 50);
                updateGridView();
                Toast.makeText(getActivity(), "Copied first image", Toast.LENGTH_LONG).show();
                return true;
            } catch (Exception e) {
                Toast.makeText(getActivity(), "No image to copy", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateGridView() {
//        mGridAdapter.notifyDataSetChanged();
        mGridView.setAdapter(new MeGridViewAdapter(getActivity()));
//        mGridAdapter.notifyDataSetInvalidated();
    }

    // TODO: Rename method, update argument and hook method into UI event
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
