package com.example.gbakie.solar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private EditText etZip;
    private EditText etCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etZip = (EditText) findViewById(R.id.etZip);
        etCapacity = (EditText) findViewById(R.id.etCapacity);
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

    public void onSendClick(View v) {
        String[] params = {etZip.getText().toString(), etCapacity.getText().toString()};
        new DataRequest().execute(params);
    }

    public void processData(String json) {
        try {
            JSONObject json_obj = new JSONObject(json);

            JSONArray ac_monthly_json = json_obj.getJSONObject("outputs").getJSONArray("ac_monthly");
            JSONArray solrad_monthly_json = json_obj.getJSONObject("outputs").getJSONArray("solrad_monthly");

            List<Double> ac_monthly = new ArrayList<Double>();
            if (ac_monthly_json != null)
                for (int i = 0; i < ac_monthly_json.length(); i++)
                    ac_monthly.add(ac_monthly_json.getDouble(i));


            List<Double> solrad_monthly = new ArrayList<Double>();
            if (solrad_monthly_json != null)
                for (int i = 0; i < solrad_monthly_json.length(); i++)
                    solrad_monthly.add(solrad_monthly_json.getDouble(i));


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public class DataRequest extends AsyncTask<String, Void, String> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Getting Data ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            String result = "";

            String zipcode = params[0];
            String key = "gnAQjxSNfEU5RJ24RESvkn4r38H8keBvJI4f8rIn";
            String capacity = params[1];
            String azimuth = "180";
            String tilt = "40";
            String aType = "1";
            String mType = "1";
            String loss = "10";

            String url = String.format("http://developer.nrel.gov/api/pvwatts/v5.json?address=%s&api_key=%s&system_capacity=%s&azimuth=%s&tilt=%s&array_type=%s&module_type=%s&losses=%s", zipcode, key, capacity, azimuth, tilt, aType, mType, loss);
            try {

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                HttpGet get = new HttpGet(url);
                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(get);

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                result = convertStreamToText(inputStream);
            } catch (Exception e) {
                String error = e.toString();
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            processData(result);
        }

        protected String convertStreamToText(InputStream is) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            String result = "";
            try {
                while ((line = bufferedReader.readLine()) != null)
                    result += line;
            } catch (IOException e) {
                e.printStackTrace();
            }

            is.close();
            return result;
        }
    }
}
