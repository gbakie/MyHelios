package com.example.gbakie.solar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ResultActivity extends Activity {

    private TextView tvPower;

    // Initial cost per Kw capacity
    private final static int INITIAL_COST_KW = 4240;

    // Cut rate on initial price
    private final static double CUT_RATE = 0.3;

    // avoid co2 emission in pounds per kwh
    private final static double CARBON_POUNDS_PER_KWH = 0.905;

    // KWH price
    private final static double PRICE_KWH = 0.8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.MESSAGE1);

        tvPower = (TextView)findViewById(R.id.tvPower);
        tvPower.setText(message);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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

    public double calculateEnergyYear(double[] powerMonth) {
        double total = 0;

        for (int i = 0; i < powerMonth.length; i++) {
            total += powerMonth[i];
        }

        return total;
    }

    public double calculateCarbon(double energy) {
        return energy * CARBON_POUNDS_PER_KWH;
    }

    public double calculateMoneySaving(double energy) {
        return energy * PRICE_KWH;
    }

}