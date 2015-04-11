package com.example.gbakie.solar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ResultActivity extends Activity {

    // domestic hot water comsumption in liters per person per day
    private final static double DHWC_PERSON = 94.6;
    // number of solar collectors
    private final static int NUM_COLLECTORS = 2;
    // temperature increase required in Kelvin
    private final static int DELTA_TEMP = 41;
    // water specific heat in K=kJ/(kg * K)
    private final static double WATER_SH = 4.187;
    // natural gas system efficiency
    private final static double NG_SYS_EFF = 0.56;
    // collector size in m^2
    private final static double COLLECTOR_SIZE = 3.7161;
    // solar system efficiency
    private final static double SOLAR_SYS_EFF[] = {0.35, 0.35, 0.4, 0.45, 0.5, 0.55, 0.55, 0.5, 0.45, 0.4, 0.4, 0.35};
    // natural gas price $ / kWh
    private final static double NG_PRICE[] = {0.04928309,0.04689402, 0.050989568, 0.053617544, 0.05003394, 0.050477624, 0.054846208, 0.055255763, 0.052423009, 0.044743858, 0.04928309, 0.050580013};
    private final static int DAYS_IN_MONTH[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private TextView tvPower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        double[] ac_montly = intent.getDoubleArrayExtra(MainActivity.AC_MONTHLY);
        double[] solrad_monthly = intent.getDoubleArrayExtra(MainActivity.SOLRAD_MONTHLY);
        int household_size = intent.getIntExtra(MainActivity.OCCUPANTS, 0);

        double daily_dhw_consumption = DHWC_PERSON * household_size;
        // energy demand for domestic hot water in kWh
        double thermal_daily_energy_demand =  daily_dhw_consumption * WATER_SH * DELTA_TEMP *  0.000278;

        double[] thermal_monthly_energy_demand = new double[12];
        double[] thermal_monthly_energy_savings = new double[12];
        double[] thermal_monthly_dollar_savings = new double[12];
        double[] thermal_monthly_percentage_savings = new double[12];

        double thermal_energy_demand_year = 0;
        double thermal_energy_savings_year = 0;
        double thermal_dollar_savings_year = 0;
        double thermal_percentage_savings_year = 0;

        // monthly calculations
        for (int i = 0; i < 12; i++) {
            thermal_monthly_energy_demand[i] = DAYS_IN_MONTH[i] * thermal_daily_energy_demand / NG_SYS_EFF;
            thermal_energy_demand_year += thermal_monthly_energy_demand[i];
            thermal_monthly_energy_savings[i] = SOLAR_SYS_EFF[i] * COLLECTOR_SIZE * NUM_COLLECTORS * solrad_monthly[i] * DAYS_IN_MONTH[i];
            thermal_energy_savings_year += thermal_monthly_energy_savings[i];
            thermal_monthly_dollar_savings[i] = NG_PRICE[i] * thermal_monthly_energy_savings[i];
            thermal_dollar_savings_year += thermal_monthly_dollar_savings[i];
            thermal_monthly_percentage_savings[i] = thermal_monthly_energy_savings[i] / thermal_monthly_energy_demand[i];
            thermal_percentage_savings_year += thermal_monthly_percentage_savings[i];
        }

        thermal_percentage_savings_year = thermal_percentage_savings_year / 12;
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
}
