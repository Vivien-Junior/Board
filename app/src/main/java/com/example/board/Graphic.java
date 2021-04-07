package com.example.board;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.RangeColumn;
import com.anychart.data.Mapping;
import com.anychart.data.Set;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graphic extends AppCompatActivity {

    private String projectName;
    private String sectionName;
    private boolean newMonth;
    private GestionXML gestionXML;
    private String month;
    private String year;
    List<String> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newMonth = true;

        //Toast.makeText(getApplicationContext(), "Vous avez cliqué", Toast.LENGTH_SHORT).show();
        Intent intent = getIntent();
        projectName = intent.getStringExtra("nom");
        sectionName = intent.getStringExtra("nomSection");

        loadXML();
        taskList = gestionXML.lireTachesProjetXML(this.projectName);

        //récupération des données
        if(intent.getStringExtra("diagramme").equals("gantt")){
            setTitle(projectName);
            setContentView(R.layout.gantt);

            month = intent.getStringExtra("mois");
            year = intent.getStringExtra("annee");

            Spinner spinner = (Spinner) findViewById(R.id.mois);
            ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
            int spinnerPosition = myAdap.getPosition(month);
            spinner.setSelection(spinnerPosition);

            spinner = (Spinner) findViewById(R.id.annee);
            myAdap = (ArrayAdapter) spinner.getAdapter();
            spinnerPosition = myAdap.getPosition(year);
            spinner.setSelection(spinnerPosition);

            setOnClickSpinner();
        }

        else{
            setTitle(projectName + " : " + getString(R.string.diagrammePie));
            setContentView(R.layout.pie);
            displayPie();
        }
    }

    /**
     * Méthode de chargement du XML de la section concerné
     * si c'est la premiere que l'on vient dans la section le XML va se créer automatiquement
     */
    private void loadXML(){
        gestionXML = new GestionXML(this.sectionName,getApplicationContext());

    }

    public void displayPie(){

        Pie pie = AnyChart.pie();
        int count[] = {0, 0, 0};

        if(taskList != null){
            for( String nom : taskList){

                /*Statut*/
                HashMap<String, String> elem = gestionXML.getElementTache(nom,projectName);
                String statut = elem.get("statut");

                /* Rajout dans le tableau*/
                if(statut.equals(getString(R.string.a_faire)))
                    count[0]++;
                else if(statut.equals(getString(R.string.en_cours)))
                    count[1]++;
                else
                    count[2]++;
            }
        }

        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry(getString(R.string.a_faire), count[0]));
        data.add(new ValueDataEntry(getString(R.string.en_cours), count[1]));
        data.add(new ValueDataEntry(getString(R.string.terminee), count[2]));

        pie.data(data);
        AnyChartView anyChartView = (AnyChartView) findViewById(R.id.any_chart_view);
        anyChartView.setChart(pie);

    }

    public void displayGantt(int month, int year){

        Spinner spinner = (Spinner) findViewById(R.id.mois);
        String m = spinner.getSelectedItem().toString();

        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        Cartesian cartesian = AnyChart.cartesian();
        cartesian.title("Diagramme de Gantt");
        List<DataEntry> data = new ArrayList<>();

        String[] startDate;
        String[] endDate;

        int startDay;
        int startMonth;
        int startYear;

        int endDay;
        int endMonth;
        int endYear;

        Number nbDay;

        /*Trouver le nombre de jours*/

        /* Cas février */
        if(month == 1) {
            /* Année bissextile*/
            if((year%4) == 0)
                nbDay = 29d;
            else
                nbDay = 28d;
        }
        else if(((month%2) == 0) && month < 6)
            nbDay = 31d;
        else
            nbDay = 30d;

        Toast.makeText(getApplicationContext(), "Mois : " + String.valueOf(month) + "/ Annee : " + String.valueOf(year), Toast.LENGTH_SHORT).show();

        if(taskList != null){

            for( String nom : taskList){

                HashMap<String, String> elem = gestionXML.getElementTache(nom,projectName);

                startDate = elem.get("dateDebut").split("/");
                endDate = elem.get("dateFin").split("/");

                Log.d("MyApp",nom + " : " + elem.get("dateDebut") + "-" + elem.get("dateFin"));

                startDay = Integer.parseInt(startDate[0]);
                startMonth = Integer.parseInt(startDate[1]);
                startYear = Integer.parseInt(startDate[2]);

                endDay = Integer.parseInt(endDate[0]);
                endMonth = Integer.parseInt(endDate[1]);
                endYear = Integer.parseInt(endDate[2]);


                if (startYear == year){
                    if(endYear > year){
                        if (month + 1 == startMonth)
                            data.add(new CustomDataEntry(nom, startDay, 31));
                        else if (month + 1 > startMonth)
                            data.add(new CustomDataEntry(nom, 0, 31));
                    }
                    /* Sur la même année*/
                    else if(endYear == year) {
                        /* Si le mois de début correspond au mois dans le Spinner */
                        if (startMonth == month + 1) {
                            /* Sur plusieurs mois */
                            if (endMonth > month + 1)
                                data.add(new CustomDataEntry(nom, startDay, 31));
                                /* Même mois */
                            else
                                data.add(new CustomDataEntry(nom, startDay, endDay));
                        }
                        /* Si le mois de fin correspond au mois dans le spinner */
                        else if (endMonth == month + 1)
                            data.add(new CustomDataEntry(nom, 0, endDay));
                        /* Si le mois dans le spinner est compris entre les mois de début et de fin*/
                        if (month + 1 > startMonth && month + 1 < endMonth)
                            data.add(new CustomDataEntry(nom, 0, 31));
                    }
                }

                else if(startYear < year){
                    if (endYear > year)
                        data.add(new CustomDataEntry(nom, 0, 31));
                    if (endYear == year){
                        if(endMonth > month + 1)
                            data.add(new CustomDataEntry(nom, 0, 31));
                        else if (endMonth == month + 1)
                            data.add(new CustomDataEntry(nom, 0, endDay));
                    }
                }

                /* Sur plusieurs années */
                else if(startYear < year && endYear > year)
                    data.add(new CustomDataEntry(nom, 0, 31));

            }
        }

        Set set = Set.instantiate();
        set.data(data);

        Mapping londonData = set.mapAs("{ x: 'x', high: 'debut', low: 'fin' }");

        RangeColumn columnLondon = cartesian.rangeColumn(londonData);
        columnLondon.name("Tâches");

        cartesian.xAxis(true);
        cartesian.yAxis(true);

        cartesian.yScale()
                .minimum(0d)
                .maximum(nbDay);

        cartesian.yGrid(true)
                .yMinorGrid(true);

        anyChartView.setChart(cartesian);
    }

    private class CustomDataEntry extends DataEntry {
        public CustomDataEntry(String x, Number start, Number end) {
            setValue("x", x);
            setValue("debut", start);
            setValue("fin", end);
        }
    }

    private void setOnClickSpinner(){
        Spinner spinnerMonth = (Spinner) findViewById(R.id.mois);
        Spinner spinnerYear = (Spinner) findViewById(R.id.annee);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ArrayAdapter myAdap = (ArrayAdapter) parent.getAdapter();
                String m = (String) myAdap.getItem(position);

                String y = spinnerYear.getSelectedItem().toString();

                Toast.makeText(getApplicationContext(), m + "/" + y, Toast.LENGTH_SHORT).show();
                if (!newMonth) {
                    finish();
                    startActivity(getIntent().putExtra("mois", m).putExtra("annee", y));
                }

                displayGantt(position, Integer.parseInt(y));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                ArrayAdapter myAdap = (ArrayAdapter) parent.getAdapter();
                String y = (String) myAdap.getItem(position);

                String m = spinnerMonth.getSelectedItem().toString();

                Toast.makeText(getApplicationContext(), m + "/" + y, Toast.LENGTH_SHORT).show();

                if (!newMonth) {
                    finish();
                    startActivity(getIntent().putExtra("mois", m).putExtra("annee", y));
                }
                newMonth = false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
