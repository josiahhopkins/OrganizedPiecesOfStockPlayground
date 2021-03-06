package com.example.josiah.organizedpiecesofstockplayground;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.josiah.organizedpiecesofstockplayground.UtilityClasses.Group;
import com.example.josiah.organizedpiecesofstockplayground.UtilityClasses.User;
import com.example.josiah.organizedpiecesofstockplayground.UtilityClasses.UserParticipationInGroup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class UsersWithinGroupListFragment extends Fragment {

    // TODO: Customize parameter argument names
    public static final String USERS_WITHIN_GROUP_URL = "http://cssgate.insttech.washington.edu/~josiah3/PHP_Code/PHP%20Code/list.php?cmd=usersInGroup";
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView mRecyclerView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UsersWithinGroupListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UsersWithinGroupListFragment newInstance(int columnCount) {
        UsersWithinGroupListFragment fragment = new UsersWithinGroupListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            DownloadTask d = new DownloadTask();
            d.execute(new String[]{
                    buildString()
            });
        }
        return view;
    }

    private String buildString() {
        Log.e("URL = :", USERS_WITHIN_GROUP_URL + "&group_name=" + ((MainActivity) this.getActivity()).getCurrentGroup().getMyName());
        return USERS_WITHIN_GROUP_URL + "&group_name=" + ((MainActivity) this.getActivity()).getCurrentGroup().getMyName();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(UserParticipationInGroup item);
    }


    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            HttpURLConnection urlConnection = null;
            for (String url : urls) {
                try {
                    URL urlObject = new URL(url);
                    urlConnection = (HttpURLConnection) urlObject.openConnection();
                    InputStream content = urlConnection.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }
                } catch (Exception e) {
                    response = "Unable to download the list of courses, Reason: " + e.getMessage();
                } finally {
                    if (urlConnection != null) urlConnection.disconnect();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Something wrong with the network or the URL.
            if (result.startsWith("Unable to")) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
                return;
            }
            List<UserParticipationInGroup> courseList = new ArrayList<>();
            result = UserParticipationInGroup.parseStockJSON(result, courseList);
            // Something wrong with the JSON returned.
            if (result != null) {
                Toast.makeText(getActivity().getApplicationContext(), result, Toast.LENGTH_LONG).show();
                return;
            }
            // Everything is good, show the list of courses.
            if (!courseList.isEmpty()) {
                mRecyclerView.setAdapter(new MyUserRecyclerViewAdapter(courseList, mListener));
            }
        }
    }
}
