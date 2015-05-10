package com.example.yunsuphong.weathergrid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            throw new RuntimeException();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        double zipCode   = getDouble("Zip Code",   "Enter a zip code");

        //Documentation about parameters such as q= and units= is at
        //http://openweathermap.org/current

        String urlString = "http://api.openweathermap.org/data/2.5/forecast/daily"
                + "?q="+zipCode+",US"     //The Woolworth Building has its own zip code.
                + "&cnt=7"          //number of days
                + "&units=imperial" //fahrenheit, not celsius
                + "&mode=json";     //vs. xml or html

        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(urlString);
    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {
        String[] date = new String[7]; //length of array must agree with cnt un URL
        int[] temperature = new int[7];
        int[] humidity = new int[7];

        //This method is executed by the second thread.

        @Override
        protected Void doInBackground(String... urlString) {
            String json;
            //Must be declared outside of try block,
            //so we can mention them in finally block.
            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL(urlString[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                InputStream inputStream = httpURLConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }

                StringBuffer buffer = new StringBuffer();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }

                if (buffer.length() == 0) {
                    return null;
                }
                json = buffer.toString();
            } catch (Exception exception) {
                Log.e("myTag", "doInBackground", exception);
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException exception) {
                        Log.e("myTag", "doInBackground", exception);
                    }
                }
            }

            //Parse the JSON string.
            try {
                JSONObject jSONObject = new JSONObject(json); //place in object to parse
                JSONArray list = jSONObject.getJSONArray("list"); //name of array is "list" in the JSON
                SimpleDateFormat dateFormat = new SimpleDateFormat("E MMM d");

                for (int i = 0; i < list.length(); ++i) {
                    JSONObject day = (JSONObject)list.get(i);
                    long seconds = day.getLong("dt");
                    JSONObject temp = (JSONObject) day.getJSONObject("temp");
                    double max = temp.getDouble("max");
                    date[i] = dateFormat.format(1000L * seconds);
                    temperature[i] = (int)Math.round(max);
                    humidity[i] = day.getInt("humidity");
                }
            } catch (JSONException exception) {
                Log.e("myTag", "doInBackground", exception); //prints message in logcat with these tags. e = error
            }
            return null;
        }

        //This method is executed by the UI thread.

        @Override
        protected void onPostExecute(Void v) {
            ReportAdapter reportAdapter = new ReportAdapter(MainActivity.this, date, humidity, temperature);
            GridView gridView = (GridView)findViewById(R.id.gridView);
            gridView.setAdapter(reportAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private double mResult;

    public double getDouble(String title, String message) {

        //A builder object can create a dialog object.
        //Builder has the last name AlertDialog.  Need to plug in an activity object.  Can use 'this' because already in activity
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        //This inflator reads the dialog.xml and creates the objects described therein.
        //Pass null as the parent view because it's going in the dialog layout.
        //Inflate a file of XML when ready to display on the screen (?)
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog, null);
        builder.setView(view);

        //Must be final to be mentioned in the anonymous inner class.
        final EditText editText = (EditText)view.findViewById(R.id.editText);

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER) {

                    Editable editable = editText.getText();
                    String string = editable.toString();
                    mResult = Double.parseDouble(string);

                    //Sending this message will break us out of the loop below.
                    Message message = handler.obtainMessage();
                    handler.sendMessage(message);
                }
                return false;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //Loop until the user presses the EditText's Done button.
        try { //Is on the lookout for flying objects
            Looper.loop();  //Places app in trance state until woken up
        }
        catch(RuntimeException runtimeException) { //Write code between these brackets for what should happen when object is caught
        }

        alertDialog.dismiss();
        return mResult;
    }

    public void displayString(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Sending this message will break us out of the loop below.
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        //Loop until the user presses the EditText's Done button.
        try {
            Looper.loop();
        }
        catch(RuntimeException runtimeException) {
        }
    }
}
