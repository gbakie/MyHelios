package com.example.gbakie.solar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;


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

    // Initial cost per Kw capacity
    private final static int INITIAL_COST_KW = 4240;

    // Cut rate on initial price
    private final static double CUT_RATE = 0.3;

    // avoid co2 emission in pounds per kwh
    private final static double CARBON_POUNDS_PER_KWH = 0.905;

    // KWH price
    private final static double PRICE_KWH = 0.8;

    private TextView tvEnergyMonth;
    private TextView tvEnergyYear;
    private Spinner spMonth;
    private TextView tvSavingMonth;

    private double[] ac_montly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        ac_montly = intent.getDoubleArrayExtra(MainActivity.AC_MONTHLY);
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

        calculateEnergyYear(solrad_monthly);

        tvEnergyMonth = (TextView) findViewById(R.id.tvEnergyMonth);
        spMonth = (Spinner) findViewById(R.id.spMonth);
        tvSavingMonth = (TextView) findViewById(R.id.tvSavingMonth);
        //tvEnergyYear = (TextView) findViewById(R.id.tvEnergyYear);


        ArrayAdapter<CharSequence> adaptMonth = ArrayAdapter.createFromResource(this,
                R.array.month, android.R.layout.simple_spinner_item);
        spMonth.setAdapter(adaptMonth);


        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH);

        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                int m = spMonth.getSelectedItemPosition();
                double ac = ac_montly[m];
                double es = calculateMoneySaving(ac);
                DecimalFormat df = new DecimalFormat("#.00");

                tvEnergyMonth.setText(df.format(ac) + " kWh");
                tvSavingMonth.setText("$" + df.format(es));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        spMonth.setSelection(month);
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
