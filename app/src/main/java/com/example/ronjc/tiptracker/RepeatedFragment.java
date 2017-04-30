package com.example.ronjc.tiptracker;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ronjc.tiptracker.utils.BudgetGoalTabAdapter;
import com.example.ronjc.tiptracker.utils.FontManager;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.example.ronjc.tiptracker.utils.FontManager.BITTER;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RepeatedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RepeatedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RepeatedFragment extends Fragment {

    private final static String PERIOD_KEY = "period";

    private Typeface bitter;
    private String[] typeArray, frequencyArray;
    private String periodID,selectedType;

    private OnFragmentInteractionListener mListener;

    public RepeatedFragment() {
        // Required empty public constructor
    }

    public static RepeatedFragment newInstance(String periodID) {
        RepeatedFragment fragment = new RepeatedFragment();
        Bundle args = new Bundle();
        args.putString(PERIOD_KEY, periodID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.periodID = getArguments().getString(PERIOD_KEY);
        }
        typeArray = new String[]{"Income", "Expense"};
        frequencyArray = new String[] {"Weekly", "Monthly", "Annually"};
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.fragment_repeated, container, false);
        bitter = FontManager.getTypeface(mView.getContext(), BITTER);

        TextView addRepeatedHeader = (TextView) mView.findViewById(R.id.add_repeated_tv);
        Button addRepeatedButton = (Button) mView.findViewById(R.id.add_repeated_button);

        addRepeatedHeader.setTypeface(bitter);
        addRepeatedButton.setTypeface(bitter);
        addRepeatedButton.setOnClickListener(new RepeatedButtonListener());

        return mView;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * Listener sub class that shows add repeated dialog on click
     */
    private class RepeatedButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            displayAddRepeatedDialog();
        }
    }

    /**
     *
     */
    public void displayAddRepeatedDialog() {

        LayoutInflater mLayoutInflator = (LayoutInflater)getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        View mView = mLayoutInflator.inflate(R.layout.add_repeated_dialog, null);
        ((TextView)mView.findViewById(R.id.add_repeated_header)).setTypeface(bitter);

        TextInputLayout itemNameTextInput = (TextInputLayout)mView.findViewById(R.id.add_repeated_name_text_input);
        TextInputLayout itemAmountTextInput = (TextInputLayout)mView.findViewById(R.id.add_repeated_amount_text_input);
        EditText itemNameEditText = (EditText)mView.findViewById(R.id.add_repeated_name);
        EditText itemAmountEditText = (EditText)mView.findViewById(R.id.repeated_amount);
        Spinner repeatedSpinner = (Spinner)mView.findViewById(R.id.repeated_type_spinner);
        Spinner frequencySpinner = (Spinner)mView.findViewById(R.id.repeated_frequency_spinner);
        Button repeatedDialogButton = (Button) mView.findViewById(R.id.add_repeated_dialog_button);

        //Create array adapter for spinner
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, typeArray) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = super.getView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }
        };
        mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatedSpinner.setAdapter(mArrayAdapter);

        ArrayAdapter<String> mFrequencyAdapter = new ArrayAdapter<String>(getContext(), R.layout.custom_spinner_item, frequencyArray) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = super.getView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView  = super.getDropDownView(position, convertView, parent);
                ((TextView)mView).setTypeface(bitter);
                return mView;
            }
        };
        mFrequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(mFrequencyAdapter);

        //Set font styling to Bitter
        itemNameTextInput.setTypeface(bitter);
        itemAmountTextInput.setTypeface(bitter);
        itemNameEditText.setTypeface(bitter);
        itemAmountEditText.setTypeface(bitter);
        repeatedDialogButton.setTypeface(bitter);

        //set on item selected listener for spinner
        repeatedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedType = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        repeatedDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add repeated to user
            }
        });

        //Create and show dialog
        mBuilder.setView(mView);
        AlertDialog mAlertDialog = mBuilder.create();
        mAlertDialog.show();
    }
}
