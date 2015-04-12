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
    // electricity price $ / kWh
    private final static double ELECTRICITY_PRICE[] = {0.24, 0.241, 0.241, 0.18, 0.18, 0.18, 0.18, 0.18, 0.189, 0.186, 0.185, 0.195};
    // PV system cost in $ / kW
    private final static double PV_SYSTEM_COST = 4240;
    // maintenance cost per year $ / kW
    private final static double MAINTENANCE_COST_YEAR = 20;
    // federal tax incentive
    private final static double TAX_INCENTIVE = 0.3;
    // yearly increase in electricity price
    private final static double ELECTRICITY_INCREASE_YEAR = 0.05;
    // yearly increase in maintenance price
    private final static double MAINTENANCE_INCREASE_YEAR = 0.02;
    // internal rate of return
    private final static double IRR = 0.07;
    // system lifespan in years
    private final static int SYSTEM_LIFESPAN = 20;
    // Cut rate on initial price
    private final static double CUT_RATE = 0.3;

    // avoid co2 emission in pounds per kwh
    private final static double CARBON_POUNDS_PER_KWH_PV = 0.905;
    // avoid co2 emission in pounds per kwh for thermal
    private final static double CARBON_POUNDS_PER_KWH_THERMAL = 0.905;

    private TextView tvEnergyMonth;
    private TextView tvEnergyYear;
    private Spinner spMonth;
    private TextView tvSavingMonth;
    private TextView tvCO2;
    private TextView tvSavingYear;
    private TextView tvCO2Year;
    private TextView tvPayback;
    private TextView tvThermalEnergy;
    private TextView tvThermalSavings;
    private TextView tvThermalCO2;

    private double[] solrad_monthly;
    private int household_size;
    private int capacity;

    private double thermal_energy_savings_year;
    private double thermal_dollar_savings_year;
    private double thermal_percentage_savings_year;
    private double thermal_carbon_savings_year;

    private double[] thermal_monthly_energy_savings = new double[12];
    private double[] thermal_monthly_dollar_savings = new double[12];
    private double[] thermal_monthly_percentage_savings = new double[12];
    private double[] thermal_monthly_carbon_savings = new double[12];

    private double[] pv_monthly_energy_savings;
    private double[] pv_monthly_carbon_savings = new double[12];
    private double[] pv_monthly_dollar_savings = new double[12];

    private double pv_carbon_savings_year;
    private double pv_dollar_savings_year;
    private double pv_energy_savings_year;
    private int payback_year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        pv_monthly_energy_savings = intent.getDoubleArrayExtra(MainActivity.AC_MONTHLY);
        solrad_monthly = intent.getDoubleArrayExtra(MainActivity.SOLRAD_MONTHLY);
        household_size = intent.getIntExtra(MainActivity.OCCUPANTS, 0);
        capacity = intent.getIntExtra(MainActivity.CAPACITY, 0);

        calculateThermal();
        calculatePV();
        calculatePVPayback();

        tvEnergyMonth = (TextView) findViewById(R.id.tvEnergyMonth);
        spMonth = (Spinner) findViewById(R.id.spMonth);
        tvSavingMonth = (TextView) findViewById(R.id.tvSavingMonth);
        tvCO2 = (TextView) findViewById(R.id.tvCO2);
        tvEnergyYear = (TextView) findViewById(R.id.tvEnergyYear);
        tvSavingYear = (TextView) findViewById(R.id.tvSavingYear);
        tvCO2Year = (TextView) findViewById(R.id.tvCO2Year);
        tvPayback = (TextView) findViewById(R.id.tvPayback);
        tvThermalEnergy = (TextView) findViewById(R.id.tvThermalEnergy);
        tvThermalSavings = (TextView) findViewById(R.id.tvThermalSavings);
        tvThermalCO2 = (TextView) findViewById(R.id.tvThermalCO2);

        DecimalFormat df = new DecimalFormat("#.00");
        tvEnergyYear.setText(df.format(pv_energy_savings_year) + " kW");
        tvSavingYear.setText("$" + df.format(pv_dollar_savings_year));
        tvCO2Year.setText(df.format(pv_carbon_savings_year) + " pounds");

        if (payback_year > 20)
            tvPayback.setText("20+ years");
        else
            tvPayback.setText(Integer.toString(payback_year) + " years");


        tvThermalEnergy.setText(df.format(thermal_energy_savings_year) + " kW");
        tvThermalSavings.setText("$" + df.format(thermal_dollar_savings_year));
        tvThermalCO2.setText(df.format(thermal_carbon_savings_year) + " pounds");

        ArrayAdapter<CharSequence> adaptMonth = ArrayAdapter.createFromResource(this,
                R.array.month, android.R.layout.simple_spinner_item);
        spMonth.setAdapter(adaptMonth);


        spMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int m = spMonth.getSelectedItemPosition();
                double ac = pv_monthly_energy_savings[m];
                double es = pv_monthly_dollar_savings[m];
                double co2 = pv_monthly_carbon_savings[m];
                DecimalFormat df = new DecimalFormat("#.00");

                tvEnergyMonth.setText(df.format(ac) + " kWh");
                tvSavingMonth.setText("$" + df.format(es));
                tvCO2.setText(df.format(co2) + " pounds");
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        int month = cal.get(Calendar.MONTH);
        spMonth.setSelection(month);
    }

    public void calculateThermal() {
        double daily_dhw_consumption = DHWC_PERSON * household_size;
        // energy demand for domestic hot water in kWh
        double thermal_daily_energy_demand =  daily_dhw_consumption * WATER_SH * DELTA_TEMP *  0.000278;

        double[] thermal_monthly_energy_demand = new double[12];

        thermal_energy_savings_year = 0;
        thermal_dollar_savings_year = 0;
        thermal_percentage_savings_year = 0;

        double thermal_energy_demand_year = 0;

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
            thermal_monthly_carbon_savings[i] = thermal_monthly_energy_savings[i] * CARBON_POUNDS_PER_KWH_THERMAL;
        }
        thermal_percentage_savings_year = thermal_percentage_savings_year / 12;
        thermal_carbon_savings_year = thermal_energy_savings_year * CARBON_POUNDS_PER_KWH_THERMAL;
    }

    public void calculatePV() {
        pv_carbon_savings_year = 0;
        pv_dollar_savings_year = 0;
        pv_energy_savings_year = 0;

        // monthly calculations
        for (int i = 0; i < 12; i++) {
            pv_energy_savings_year += pv_monthly_energy_savings[i];
            pv_monthly_dollar_savings[i] = pv_monthly_energy_savings[i] * ELECTRICITY_PRICE[i];
            pv_dollar_savings_year += pv_monthly_dollar_savings[i];
            pv_monthly_carbon_savings[i] = pv_monthly_energy_savings[i] * CARBON_POUNDS_PER_KWH_PV;
            pv_carbon_savings_year += pv_monthly_carbon_savings[i];
        }
    }

    public void calculatePVPayback() {
        double yearly_electricity_price = 0;
        for (int i = 0; i < 12; i++)
            yearly_electricity_price += ELECTRICITY_PRICE[i];
        yearly_electricity_price = yearly_electricity_price / 12;


        double[] electricity_price = new double[SYSTEM_LIFESPAN];
        double[] maintenance_cost = new double[SYSTEM_LIFESPAN];
        double[] cashflow = new double[SYSTEM_LIFESPAN];
        double[] dr_cashflow = new double[SYSTEM_LIFESPAN];
        double[] payback = new double[SYSTEM_LIFESPAN];

        double pv_investment = capacity * PV_SYSTEM_COST * (1 - TAX_INCENTIVE);
        electricity_price[0] = yearly_electricity_price;
        maintenance_cost[0] = MAINTENANCE_COST_YEAR;
        cashflow[0] = -pv_investment;
        dr_cashflow[0] = cashflow[0];
        payback[0] = dr_cashflow[0];

        for (int i = 1; i < SYSTEM_LIFESPAN; i++) {
            electricity_price[i] = electricity_price[i-1] * (1 + ELECTRICITY_INCREASE_YEAR);
            maintenance_cost[i] = maintenance_cost[i-1] * (1 + MAINTENANCE_INCREASE_YEAR);
            cashflow[i] = pv_energy_savings_year * electricity_price[i] - maintenance_cost[i];
            dr_cashflow[i] = cashflow[i] / Math.pow(1+ IRR, i);
            payback[i] = payback[i-1] + dr_cashflow[i];
        }

        payback_year = 0;
        while (payback_year < SYSTEM_LIFESPAN && payback[payback_year] <= 0)
            payback_year++;

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
