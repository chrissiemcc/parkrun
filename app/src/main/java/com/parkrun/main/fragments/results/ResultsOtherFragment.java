package com.parkrun.main.fragments.results;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.parkrun.main.R;
import com.parkrun.main.util.UtilAlertDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ResultsOtherFragment extends Fragment
{
    private int outcome = 0;
    // 0 = parkrunner found with results
    // 1 = parkrunner found with no results
    // 2 = no parkrunner found

    private int athleteId;

    private View layout;
    private TableLayout tableLayout;
    private TextView[] results = new TextView[7];

    private EditText txtSearchAthlete;
    private Button btnSearchAthlete;
    private ProgressBar progressBarSearchOther;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(outcome == 0)
            {
                progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
                progressBarSearchOther.setVisibility(View.INVISIBLE);

                FrameLayout myResultsFrame = layout.findViewById(R.id.otherResultsFrame);

                myResultsFrame.addView(tableLayout);
                //view results of parkrunner
            }
            else if(outcome == 1)
            {
                progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
                progressBarSearchOther.setVisibility(View.INVISIBLE);

                //parkrunner with no results found
            }
            else if(outcome == 2)
            {
                progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);
                searchFormVisibility(true);

                //no parkrunner found
                UtilAlertDialog utilAlertDialog = new UtilAlertDialog(getActivity().getApplicationContext());
                utilAlertDialog.getAlertDialog("No athlete found", "The athlete ID provided did not match a parkrunner.", getActivity());
            }
        }
    };

    public ResultsOtherFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_results_other, container, false);

        btnSearchAthlete = layout.findViewById(R.id.btnSearchAthlete);
        txtSearchAthlete = layout.findViewById(R.id.txtSearchAthlete);
        progressBarSearchOther = layout.findViewById(R.id.progressBarSearchOther);

        btnSearchAthlete.setEnabled(false);

        txtSearchAthlete.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count)
            {
                if(txtSearchAthlete.getText().toString().equals(""))
                {
                    btnSearchAthlete.setEnabled(false);
                }
                else
                {
                    btnSearchAthlete.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable)
            {

            }
        });

        btnSearchAthlete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                athleteId = Integer.parseInt(txtSearchAthlete.getText().toString());

                closeKeyboard();
                searchFormVisibility(false);

                runJsoupThread();
            }
        });

        return layout;
    }

    private void runJsoupThread()
    {
        Runnable jsoupRun = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    tableLayout = new TableLayout(getActivity().getApplicationContext());

                    TableRow tableRow;

                    boolean doHeader = true;

                    TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);
                    layoutParams.setMargins(0,8,0,0);

                    Log.d("Testing", athleteId+" is the id");

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/results/athleteeventresultshistory/?athleteNumber="+athleteId+"&eventNumber=0").get();
                    // Retrieve parkrun results html page

                    Element athleteCheck = jsoupDocument.selectFirst("h2");

                    Log.d("Testing", ""+athleteCheck.text().charAt(0));

                    if(athleteCheck.text().charAt(0) == '-')
                    {
                        outcome = 2;
                    }

                    Element resultsTable = jsoupDocument.selectFirst("caption:contains(All Results)").parent();
                    // Select the main results table

                    Elements rows = resultsTable.select("tr");

                    if((rows.last() != rows.first())&&(outcome != 2))
                    {
                        for (Element row : rows)
                        {
                            tableRow = new TableRow(getActivity().getApplicationContext());

                            for (int i=0;i<results.length;i++)
                                results[i] = new TextView(getActivity().getApplicationContext());

                            Elements cells = row.select("td");

                            if(doHeader)
                            {
                                String[] headings = {"parkrun","Date","Event #","Pos","Time","Age %","PB?"};
                                for(int i=0; i<results.length;i++)
                                    results[i].setText(headings[i]);
                            }
                            else
                                for (Element cell : cells)
                                    results[cell.elementSiblingIndex()].setText(cell.text());

                            for (TextView result : results)
                            {
                                result.setGravity(Gravity.CENTER);
                                result.setPadding(8, 0, 8, 0);
                                result.setLayoutParams(layoutParams); //set margins between rows
                                if(doHeader) result.setBackgroundColor(Color.CYAN); //set heading background colour
                                result.setTextSize(5, 2f);
                                tableRow.addView(result);
                            }

                            if(doHeader) doHeader = false;

                            tableLayout.addView(tableRow);
                            //Add row to table after it has finished populating

                            tableLayout.setStretchAllColumns(true);
                            //Makes table fills the screen
                        }
                    }
                    else if (outcome == 0)
                    {
                        //No results found
                        outcome = 1;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);
            }
        };

        Thread jsoupThread = new Thread(jsoupRun);
        jsoupThread.start();
    }
    //A separate method to run the thread for jsoup, which cannot access network on main thread

    private void searchFormVisibility (boolean visible)
    {
        if (!visible)
        {
            progressBarSearchOther.setVisibility(View.VISIBLE);
            txtSearchAthlete.setVisibility(View.INVISIBLE);
            btnSearchAthlete.setVisibility(View.INVISIBLE);
        }
        else if (visible)
        {
            progressBarSearchOther.setVisibility(View.INVISIBLE);
            txtSearchAthlete.setVisibility(View.VISIBLE);
            btnSearchAthlete.setVisibility(View.VISIBLE);
        }
    }

    private void closeKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}