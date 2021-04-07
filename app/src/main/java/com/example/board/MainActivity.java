package com.example.board;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final List<Button> buttonList = new ArrayList<Button>();
    private BottomSheetDialog bottomSheetDialog;


    private String categoryName;
    private HashMap<String, List<String>> categoryElementList;
    private ExpandableListView expandableListViewProjets;
    private ExpandableListView expandableListViewPrioritees;
    private GestionXML gestionXML;
    private ExpandableListViewAdapter listViewAdapterProjets;
    private ExpandableListViewAdapter listViewAdapterPrioritees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadButton();

        /** definitions puis affichages des menus expandables de la page d'accueil */
        expandableListViewProjets = findViewById(R.id.projets);
        showProjectList();
        listViewAdapterProjets = new ExpandableListViewAdapter(this,categoryName,categoryElementList);
        expandableListViewProjets.setAdapter(listViewAdapterProjets);

        expandableListViewPrioritees = findViewById(R.id.prioritees);
        showPriorityList();
        listViewAdapterPrioritees = new ExpandableListViewAdapter(this,categoryName,categoryElementList);
        expandableListViewPrioritees.setAdapter(listViewAdapterPrioritees);

        getValueBottomSheet();

    }

    /**
     * méthode d'affichage du menu expendable des projets
     */
    private void showProjectList() {
        categoryName = new String(getString(R.string.projets));
        categoryElementList = new HashMap<String, List<String>>();

        List<String> listeElements = new ArrayList<String>();
        //listeElements.add("Projet 1");
        listeElements.add(getString(R.string.ajouter_projet));

        categoryElementList.put(this.categoryName,listeElements);
        categoryElementList.put(this.categoryName,listeElements);
    }

    /**
     * méthode d'affichage du menu expendable des prioritées
     */
    private void showPriorityList() {
        categoryName = new String(getString(R.string.liste_prioritees));
        categoryElementList = new HashMap<String, List<String>>();

        List<String> listeElements = new ArrayList<String>();
        listeElements.add(getString(R.string.priorite1));
        listeElements.add(getString(R.string.priorite2));
        listeElements.add(getString(R.string.priorite3));
        listeElements.add(getString(R.string.priorite4));

        categoryElementList.put(this.categoryName,listeElements);
        categoryElementList.put(this.categoryName,listeElements);

    }

    /**
     * méthode de création des boutons de la page d'accueil
     */
    private void loadButton(){
        Button button = (Button) findViewById(R.id.prochainement);
        buttonList.add(button);
        button = (Button) findViewById(R.id.aujourd_hui);
        buttonList.add(button);
        button = (Button) findViewById(R.id.terminees);
        buttonList.add(button);
        button = (Button) findViewById(R.id.retard);
        buttonList.add(button);
        button = (Button) findViewById(R.id.vie_courante);
        buttonList.add(button);


        for (Button btn : buttonList) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    Button btn = (Button) v;
                    String name = btn.getText().toString();
                    Intent intent;

                    if(name.equals(getString(R.string.vie_courante))) {
                        intent = new Intent(getApplicationContext(), DailyLife.class);
                        intent.putExtra("nomSection", name);
                        intent.putExtra("nomFichierXML", "vie_courante");
                    }
                    else {
                        intent = new Intent(getApplicationContext(), TemplateClass.class);
                        intent.putExtra("nomSection", name);
                        intent.putExtra("nomFichierXML", getString(R.string.toutesTaches));
                    }
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * méthode de récuperation des données lors de l'ajout d'un nouveau projet par l'utilisateur
     */
    private void getValueBottomSheet() {

        // reprise de la main par la page
        Intent intent = getIntent();
        String projectName = intent.getStringExtra("nomProjet");
        String startDateProject = intent.getStringExtra("dateDebutProjet");
        String endDateProject = intent.getStringExtra("dateFinProjet");
        if(projectName != null){

            //sauvegarde du nouveau projet
            gestionXML = new GestionXML(getString(R.string.projets),getApplicationContext());
            HashMap<String, String> listeElements = new HashMap<String, String>();
            listeElements.put("nomProjet",projectName);
            listeElements.put("dateDebutProjet",startDateProject);
            listeElements.put("dateFinProjet",endDateProject);
            gestionXML.appendProjetXML(listeElements);

            List<String> projectList = gestionXML.lireProjetXML(getString(R.string.projets));
            listViewAdapterProjets.removeLast();
            if(projectList != null){
                for(String nom : projectList)
                    listViewAdapterProjets.add(nom);
            }
            listViewAdapterProjets.add(getString(R.string.ajouter_projet));
        }
        else{
            gestionXML = new GestionXML(getString(R.string.projets),getApplicationContext());
            List<String> projectList = gestionXML.lireProjetXML(getString(R.string.projets));
            listViewAdapterProjets.removeLast();
            if(projectList != null){
                for(String nom : projectList)
                    listViewAdapterProjets.add(nom);
            }
            listViewAdapterProjets.add(getString(R.string.ajouter_projet));
        }
    }
}