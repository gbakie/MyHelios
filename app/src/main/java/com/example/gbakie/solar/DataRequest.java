package com.example.gbakie.solar;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by gbakie on 4/11/15.
 */
public class DataRequest extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        InputStream inputStream = null;
        String result = "";

        String zipcode = params[0];
        String key = "DEMO_KEY";
        String capacity = "4";
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

    }

    protected String convertStreamToText(InputStream is) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(is));
        String line = "";
        String result = "";
        try {
            while((line = bufferedReader.readLine()) != null)
                result += line;
        } catch (IOException e) {
            e.printStackTrace();
        }

        is.close();
        return result;
    }

}
