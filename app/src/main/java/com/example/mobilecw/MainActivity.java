//Edward Davidson
//S1604249

package com.example.mobilecw;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private String result;
    private Button currentIncidentsBtn;
    private Button roadworksBtn;
    private Button plannedroadworksBtn;

    private String userSearch;
    private Button searchBtn;

    public ProgressBar progressBar;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    ArrayList<RoadData> roadDataArrayList;

    // Traffic Scotland URLs
    private String urlSourceRW = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    private String urlSourcePRW = "https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    private String urlSourceCI = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        currentIncidentsBtn = (Button) findViewById(R.id.currentIncidentsBtn);
        currentIncidentsBtn.setOnClickListener(this);
        roadworksBtn = (Button) findViewById(R.id.roadworksBtn);
        roadworksBtn.setOnClickListener(this);
        plannedroadworksBtn = (Button) findViewById(R.id.plannedroadworksBtn);
        plannedroadworksBtn.setOnClickListener(this);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(this);

        currentIncidentsBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
        roadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
        plannedroadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
        searchBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white

        expListView = (ExpandableListView) findViewById(R.id.lvExp);

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

                    Intent i = new Intent(MainActivity.this, mapActivity.class);
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

                    Intent i = new Intent(MainActivity.this, webviewActivity.class);
                    i.putExtra("url",url);
                    startActivity(i);

                }

                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {

        if (v == currentIncidentsBtn) {

            currentIncidentsBtn.setBackgroundColor(Color.parseColor("#95f035")); //green
            roadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
            plannedroadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            roadworksBtn.setEnabled(false);
            plannedroadworksBtn.setEnabled(false);
            currentIncidentsBtn.setEnabled(false);

            searchBtn.setEnabled(false);

            startProgress(urlSourceCI);

        }
        if (v==roadworksBtn){

            currentIncidentsBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
            roadworksBtn.setBackgroundColor(Color.parseColor("#95f035")); //green
            plannedroadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            roadworksBtn.setEnabled(false);
            plannedroadworksBtn.setEnabled(false);
            currentIncidentsBtn.setEnabled(false);

            searchBtn.setEnabled(false);

            startProgress(urlSourceRW);


        }

        if (v==plannedroadworksBtn){

            currentIncidentsBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
            roadworksBtn.setBackgroundColor(Color.parseColor("#ffffff")); //white
            plannedroadworksBtn.setBackgroundColor(Color.parseColor("#95f035")); //green

            searchBtn.setEnabled(false);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            roadworksBtn.setEnabled(false);
            plannedroadworksBtn.setEnabled(false);
            currentIncidentsBtn.setEnabled(false);
            startProgress(urlSourcePRW);
        }

        if (v==searchBtn){

            Intent i = new Intent(MainActivity.this, searchActivity.class);
            i.putParcelableArrayListExtra("array", (ArrayList<? extends Parcelable>) roadDataArrayList);
            startActivity(i);

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

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        roadworksBtn.setEnabled(true);
        plannedroadworksBtn.setEnabled(true);
        currentIncidentsBtn.setEnabled(true);

        //get button clicked and update search button text
        if(((ColorDrawable)roadworksBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
        {
            searchBtn.setText("Search " +  roadworksBtn.getText().toString() + " \uD83D\uDD0D");
        }
        if(((ColorDrawable)plannedroadworksBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
        {
            searchBtn.setText("Search " +  plannedroadworksBtn.getText().toString() + " \uD83D\uDD0D");
        }
        if(((ColorDrawable)currentIncidentsBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
        {
            searchBtn.setText("Search " +  currentIncidentsBtn.getText().toString() + " \uD83D\uDD0D");
        }

        searchBtn.setEnabled(true);
        searchBtn.setVisibility(View.VISIBLE);



    }

    private void prepareListData(ArrayList rdArr) {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        int i = 0;
        int counter = 0;


        while (rdArr.size() > i) {

            RoadData rd;
            rd = (RoadData) rdArr.get(i);


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


    public void startProgress(String urlSource)
    {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    } //

    private class Task implements Runnable
    {
        private String url;

        public Task(String aurl)
        {
            url = aurl;
        }
        @Override
        public void run()
        {

            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            //Log.e("MyTag","in run");

            try
            {
                //Log.e("MyTag","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                //
                // Throw away the first 2 header lines before parsing

                result = "";

                while ((inputLine = in.readLine()) != null)
                {
                    //don't take the unwanted lines
                    if(!inputLine.contains("<?xml ") && !inputLine.contains("<rss ") && !inputLine.contains("<channel") &&
                            !inputLine.contains("<channel") && !inputLine.contains("<title>Traffic Scotland - ") &&
                            !inputLine.contains("<description>Current incidents on the road network e.g. accidents</description>") &&
                            !inputLine.contains("<description>Future roadworks on the road network.</description>") && !inputLine.contains("<description>Roadworks currently being undertaken on the road network.</description>")
                            && !inputLine.contains("<link>https://trafficscotland.org/") && !inputLine.contains("<language") && !inputLine.contains("<copyright") && !inputLine.contains("<managingEditor") &&
                            !inputLine.contains("<webMaster") && !inputLine.contains("<lastBuildDate>") && !inputLine.contains("<docs>") && !inputLine.contains("<rating") && !inputLine.contains("<generator>") && !inputLine.contains("<ttl>") &&
                            !inputLine.contains("<item") && !inputLine.contains("</item")){

                        result = result + inputLine;
                        //Log.e("MyTag", inputLine);
                    }

                }

                if(!result.equals("  </channel></rss>")) {
                    //change "georss:point" to "georss" to remove parse error during parsing
                    result = result.replaceAll("georss:point", "georss");

                    //remove starting "null " from start of string
                    result = result.substring(5);

                    //remove "</rss>" from end of string
                    result = result.substring(0, result.length() - 6);

                    //remove "non item" part of the XML
                    //line removed as it causes large datasets to process for too long - very large if statement added instead
                    //                result = result.replaceAll(".+</ttl>", "");


                    //remove "</channel>" tag
                    result = result.substring(0, result.length() - 10);

                    //change <br /> tags for \n
                    result = result.replaceAll("&lt;br /&gt;", "\n");


                    //if traffic scotland didn't add a title then a title here
                    if (result.contains("</pubDate>      <description>")) {
                        result = result.replaceAll("</pubDate>      <description>", "</pubDate>      <title>No Title</title>      <description>");
                    }
                }
                else{
                    result="NO_DATA";
                }


                in.close();
            }
            catch (IOException ae)
            {
                Log.e("MyTag", "ioexception");
            }

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    //Log.d("UI thread", "I am the UI thread");

                    if(!result.equals("NO_DATA")){

                        ArrayList<RoadData> rdArr = new ArrayList<>();

                        rdArr = parseData(result);
                        roadDataArrayList = rdArr;
                        populateExpList(rdArr);

                    }
                    else{

                        String buttonClicked = "";

                        //get button clicked and update search button text
                        if(((ColorDrawable)roadworksBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
                        {
                            buttonClicked = roadworksBtn.getText().toString();
                        }
                        if(((ColorDrawable)plannedroadworksBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
                        {
                            buttonClicked = plannedroadworksBtn.getText().toString();
                        }
                        if(((ColorDrawable)currentIncidentsBtn.getBackground()).getColor() == Color.parseColor("#95f035"))
                        {
                            buttonClicked = currentIncidentsBtn.getText().toString();
                        }

                        Toast toast = Toast.makeText(getApplicationContext(), "No " + buttonClicked + " Found!", Toast.LENGTH_LONG);
                        toast.show();

                        progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.GONE);
                        roadworksBtn.setEnabled(true);
                        plannedroadworksBtn.setEnabled(true);
                        currentIncidentsBtn.setEnabled(true);

                        ArrayList<RoadData> roadArr = new ArrayList<>();

                        populateExpList(roadArr);

                        searchBtn.setEnabled(false);

                    }


                }
            });
        }

    }

    private ArrayList<RoadData> parseData(String dataToParse)
    {
        String foundData = "";
        ArrayList<RoadData> rdArr = new ArrayList<RoadData>();
        try
        {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput( new StringReader( dataToParse ) );
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                RoadData rd = new RoadData();

                // Found a start tag
                if(eventType == XmlPullParser.START_TAG)
                {
                    // Check which Tag we have
                    try{
                        if (xpp.getName().equalsIgnoreCase("title")) {
                            // Now just get the associated text
                            String temp = xpp.nextText();
                            // Do something with text
                            //Log.e("MyTag","title " + temp);
                            rd.setTitle(temp);
                        }
                    }catch (Exception e){
                        String temp = "No Title";
                        rd.setTitle(temp);
                    }

                    // Get the next event
                    eventType = xpp.next();

                    if (xpp.getName().equalsIgnoreCase("description"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","desc " + temp);

                        //format description
                        if(temp==null)
                        {
                            temp = "";
                        }else
                        {
                            temp = formatDesc(temp);
                        }
                        rd.setDescription(temp);
                    }

                    // Get the next event
                    eventType = xpp.next();

                    // Check which Tag we have
                    if (xpp.getName().equalsIgnoreCase("link")) {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","link " + temp);
                        rd.setLink("View online at Traffic Scotland:\n"+temp);
                    }

                    // Get the next event
                    eventType = xpp.next();

                    if (xpp.getName().equalsIgnoreCase("georss"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","geopnt " + temp);
                        rd.setGeorssPoint("View on Map:\n"+temp);
                    }

                    eventType = xpp.next();

                    if (xpp.getName().equalsIgnoreCase("author"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","author " + temp);
                        if(temp.equals("") || temp == null)
                        {
                            temp = "Anonymous";
                        }
                        rd.setAuthor("Author: "+temp);
                    }

                    eventType = xpp.next();

                    if (xpp.getName().equalsIgnoreCase("comments"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","comment " + temp);
                        if(temp.equals("")||temp == null){temp = "No Comments";}
                        rd.setComments(temp);
                    }

                    eventType = xpp.next();

                    if (xpp.getName().equalsIgnoreCase("pubDate"))
                    {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        //Log.e("MyTag","pubdate " + temp);
                        rd.setPubDate(temp);
                    }

                    rdArr.add(rd);

                }

                // Get the next event
                eventType = xpp.next();

            } // End of while


        }
        catch (XmlPullParserException ae1)
        {
            Log.e("MyTag","Parsing error" + ae1.toString());
        }
        catch (IOException ae1)
        {
            Log.e("MyTag","IO error during parsing");
        }

        //Log.e("MyTag","End document");

        return rdArr;
    }

    private String formatDesc(String temp) {

        if(temp.contains("End Date:"))
        {
            temp = temp.replaceAll("End Date:","End Date: ");
        }
        if(temp.contains("Works:"))
        {
            temp = temp.replaceAll("Works:","\nWorks: ");
        }
        if(temp.contains("Management:"))
        {
            temp = temp.replaceAll("Management:","\nManagement: ");
        }
        if(temp.contains("Delay Information:"))
        {
            temp = temp.replaceAll("Delay Information:","\nDelay Information:");
        }
        if(temp.contains("Diversion Information:"))
        {
            temp = temp.replaceAll("Diversion Information:","\nDiversion Information:");
        }
        if(temp.contains("Lane Closures :"))
        {
            temp = temp.replaceAll("Lane Closures :","\nLane Closures: ");
        }



        return temp;
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


} // End of MainActivity
