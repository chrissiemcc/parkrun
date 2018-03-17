package com.parkrun.main.fragments.results;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.parkrun.main.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class ResultsYouFragment extends Fragment
{
    private View layout;

    private TableLayout tableLayout;

    private TextView[] results = new TextView[7];

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            FrameLayout myResultsFrame = layout.findViewById(R.id.my_results_frame);

            myResultsFrame.addView(tableLayout);
        }
    };

    public ResultsYouFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_results_you, container, false);

        runJsoupThread();

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

                    Document jsoupDocument = Jsoup.connect("http://www.parkrun.org.uk/results/athleteeventresultshistory/?athleteNumber=763139&eventNumber=0").get();
                    // Retrieve parkrun results html page

                    Element resultsTable = jsoupDocument.selectFirst("caption:contains(All Results)").parent();
                    // Select the main results table

                    Elements rows = resultsTable.select("tr");

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
}