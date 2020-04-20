//Edward Davidson
//S1604249

package com.example.mobilecw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class searchActivity extends AppCompatActivity implements View.OnClickListener{

    ArrayList<RoadData> roadDataArrayList;
    private String query = "";
    private boolean isSearch;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private TextView messageLbl;

    private Button searchBtn;
    private EditText searchTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        messageLbl = (TextView) findViewById(R.id.lblMessage);


        searchBtn.setOnClickListener(this);
        searchBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
        searchTxt.setBackgroundColor(Color.parseColor("#ffffff")); //white


        isSearch = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roadDataArrayList= getIntent().getParcelableArrayListExtra("array");

            // Run search in thread
            new Thread(new Task_Search()).start();
        }

        searchTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    isSearch = true;
                    query = searchTxt.getText().toString();

                    // Run search in thread
                    new Thread(new Task_Search()).start();
                }
            }
        });

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if(childPosition == 1)
                {
                    String desc = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition) + "ENDSTRING";

                    if(desc.contains("Start Date: ") && desc.contains("End Date: ")){

                        String start = desc.substring(desc.indexOf("Start Date: ") + 12, desc.indexOf(" - 00:00"));
                        start = start.replaceAll(".+, ","");


                        String end = desc.substring(desc.indexOf("End Date: ") + 10, desc.indexOf("ENDSTRING"));
                        end = end.replaceAll(".+, ","");
                        end = end.split(" - 00")[0];

                        String[] result = start.split(" ", 3);
                        String day = result[0];
                        String month = monthToNumber(result[1]);
                        String year = result[2];

                        String sDate = day+"/"+month+"/"+year;


                        result = end.split(" ", 3);
                        day = result[0];
                        month = monthToNumber(result[1]);
                        year = result[2];

                        String eDate = day+"/"+month+"/"+year;

                        sDate = sDate.trim();
                        eDate = eDate.trim();

                        if(sDate.equals(eDate))
                        {
                            String message = "Roadworks will last for 1 day.";

                            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                            TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);
                            toastView.setTextColor(Color.CYAN);
                            toast.show();
                        }
                        else{

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                            Date firstDate = null;
                            try {
                                firstDate = sdf.parse(sDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Date secondDate = null;
                            try {
                                secondDate = sdf.parse(eDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
                            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                            String message = "Roadworks will last for " + String.valueOf(diff+1);


                            message = message + " days.";

                            Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
                            TextView toastView = (TextView) toast.getView().findViewById(android.R.id.message);

                            if(diff >= 100){
                                toastView.setTextColor(Color.RED);
                            }
                            if(diff < 100){
                                toastView.setTextColor(Color.YELLOW);
                            }
                            if(diff <= 10){
                                toastView.setTextColor(Color.GREEN);
                            }

                            toast.show();

                        }


                    }

                }


                //geo points clicked
                if(childPosition == 2)
                {
                    String desc = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    String title = listDataHeader.get(groupPosition);
                    //get the geo points
                    String geoPnts = desc.replaceAll(".+:\n", "");

                    Intent i = new Intent(searchActivity.this, mapActivity.class);
                    i.putExtra("geoPnts",geoPnts);
                    i.putExtra("title", title);
                    startActivity(i);
                }

                //http link clicked
                if(childPosition == 3)
                {
                    String childTxt = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                    //get http link
                    String url = childTxt.replaceAll(".+:\n", "");

                    Intent i = new Intent(searchActivity.this, webviewActivity.class);
                    i.putExtra("url",url);
                    startActivity(i);

                }

                return true;
            }
        });


    }

    public void onClick(View v) {

        if (v == searchBtn) {

            isSearch = true;
            query = searchTxt.getText().toString();

            // Run search in thread
            new Thread(new Task_Search()).start();

        }
    }


    private class Task_Search implements Runnable
    {

        @Override
        public void run()
        {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    messageLbl.setText("");
                    messageLbl.setVisibility(View.GONE);

                }
            });


            //create new array list from original which title meets search criteria
            String title = "";
            ArrayList<RoadData> rdSearchArray = new ArrayList<RoadData>();

            int i = 0;
            while (roadDataArrayList.size() > i) {

                RoadData rd = new RoadData();
                rd = roadDataArrayList.get(i);

                title = rd.getTitle();

                if(isSearch){

                    if(title.toLowerCase().contains(query.toLowerCase())){
                        rdSearchArray.add(rd);
                    }

                }else{
                    rdSearchArray.add(rd);
                }



                i++;
            }

            final ArrayList<RoadData> rdSearchArray1 = rdSearchArray;

            //pass new array list to exp list adapter method
            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    populateExpList(rdSearchArray1);

                }
            });
        }
    }

    public void populateExpList(ArrayList rdArr){

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData(rdArr);

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchTxt.getWindowToken(), 0);

        //if no results show message
        if(rdArr.size()==0) { runOnUiThread(new Runnable() {

            @Override
            public void run() {

                messageLbl.setText("\nNo results for '" + query + "'");
                messageLbl.setVisibility(View.VISIBLE);

            }
        });
           // messageLbl.setText("No results for '" + query + "'.");
           // messageLbl.setVisibility(View.VISIBLE);
        }

    }

    private void prepareListData(ArrayList rdArr) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        int i = 0;
        int counter = 0;

        Boolean isDuplicate;
        while (rdArr.size() > i) {

            isDuplicate = false;
            RoadData rd;
            rd = (RoadData) rdArr.get(i);

            //check for duplicates
            for (String headerTitle : listDataHeader) {
                String rdTitle = rd.getTitle();

                if (headerTitle.equals(rdTitle))
                {
                    isDuplicate = true;
                }

            }

            if (isDuplicate) {
                i++;
            } else {

                // Add Header data
                listDataHeader.add(rd.getTitle());

                // Add child data
                List<String> detailList = new ArrayList<String>();
                detailList.add("Publish Date:  "+rd.getPubDate().replaceAll(" 00:00:00 GMT",""));
                detailList.add(rd.getDescription());
                //detailList.add(rd.getComments());
                //detailList.add(rd.getAuthor());
                detailList.add(rd.getGeorssPoint());
                detailList.add(rd.getLink());

                listDataChild.put(listDataHeader.get(counter), detailList);

                counter++;
                i++;
            }
        }
    }

    public static String monthToNumber(String month){

        String number = "";

        if(month.equals("January")){
            number = "01";
        }
        if(month.equals("February")){
            number = "02";
        }
        if(month.equals("March")){
            number = "03";
        }
        if(month.equals("April")){
            number = "04";
        }
        if(month.equals("May")){
            number = "05";
        }
        if(month.equals("June")){
            number = "06";
        }
        if(month.equals("July")){
            number = "07";
        }
        if(month.equals("August")){
            number = "08";
        }
        if(month.equals("September")){
            number = "09";
        }
        if(month.equals("October")){
            number = "10";
        }
        if(month.equals("November")){
            number = "11";
        }
        if(month.equals("December")){
            number = "12";
        }

        return number;
    }
}

