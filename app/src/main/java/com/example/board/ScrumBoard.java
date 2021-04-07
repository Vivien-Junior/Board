package com.example.board;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Scrumboard des projets
 */
public class ScrumBoard extends AppCompatActivity {


    private GestionXML gestionXML;
    private String nameSection;
    private String nameProjet;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scrumboard);
        setListenerButtonChart();
        setListenerButtonAddTask();
        setValeurs();
        loadXML();
        displayList();
    }

    /**
     * récupéation des valeurs nom et nom de la section
     * nom: nom du projet
     * nomSection: "prochainement" "aujourd'hui", "taches terminees", "priorite 1".. qui sert au chargement du bon XML
     */
    private void setValeurs(){

        // reprise de la main par la page
        Intent intent = getIntent();
        String nameProjet = intent.getStringExtra("nom");
        String nameSection = intent.getStringExtra("nomSection");
        if(!nameProjet.isEmpty()){
            setTitle(nameProjet);
            this.nameProjet = nameProjet;
        }
        if(!nameSection.isEmpty())
            this.nameSection = nameSection;
    }

    /**
     * Méthode de chargement du XML de la section concerné
     * si c'est la premiere que l'on vient dans la section le XML va se créer automatiquement
     */
    private void loadXML(){
        gestionXML = new GestionXML(this.nameSection,getApplicationContext());
    }

    private void setListenerButtonChart(){
        FloatingActionButton boutonDiagramme = (FloatingActionButton) findViewById(R.id.diagrammeGantt);
        boutonDiagramme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Graphic.class);
                intent.putExtra("diagramme", "gantt");
                intent.putExtra("nom", nameProjet);
                intent.putExtra("nomSection", nameSection);
                startActivity(intent);
                }
        });
        boutonDiagramme = (FloatingActionButton) findViewById(R.id.diagrammePie);
        boutonDiagramme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Graphic.class);
                intent.putExtra("diagramme", "pie");
                intent.putExtra("nom", nameProjet);
                intent.putExtra("nomSection", nameSection);
                startActivity(intent);
            }
        });
    }

    private void setListenerButtonAddTask(){
        FloatingActionButton boutonAjout = (FloatingActionButton) findViewById(R.id.ajoutTache);

        boutonAjout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ScrumBoard.this, R.style.CustomBottomSheetDialog);
                View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout,
                        (ViewGroup) findViewById(R.id.bottom_sheet));
                TextView tv = (TextView) sheetView.findViewById(R.id.titreBottomSheet);
                tv.setText(getApplicationContext().getResources().getString(R.string.ajouter_tache));

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();

                Button delete = sheetView.findViewById(R.id.boutonSupprimerProjetSheet);
                delete.setVisibility(View.GONE);

                EditText editStartDate = (EditText) sheetView.findViewById(R.id.editDateDebut);
                EditText editEndDate = (EditText) sheetView.findViewById(R.id.editDateFin);

                setListenerEditsDate(editStartDate, editEndDate);

                Button valider = (Button) sheetView.findViewById(R.id.boutonValiderProjetSheet);
                setListenerButtonValidate("", valider, null, sheetView, editStartDate, editEndDate, bottomSheetDialog, true);
            }
        });
    }
    /**
     *  méthode d'ajout d'une tâche à un projet
     * @param listeElements .get("nomTache")  get("nomProjet")  get("dateTache")
     */
    private void addTaskXML( HashMap<String, String> listeElements){
        gestionXML.appendTacheProjet(listeElements, nameProjet);

        //ajout dans le fichier contenant toutes les tâches
        gestionXML = new GestionXML(getString(R.string.toutesTaches),getApplicationContext());
        listeElements.put("nomProjet",nameProjet);
        gestionXML.appendXML(getString(R.string.toutesTaches),listeElements);
        loadXML();
        return;
    }

    /**
     * affichage de la liste des taches dans la section
     */
    private void displayList() {
        List<String> backList = gestionXML.lireTachesProjetXML(this.nameProjet);

        if(backList != null){
            for( String name : backList){
                Button button = new Button(this);

                /*Statut*/
                HashMap<String, String> elem = gestionXML.getElementTache(name,nameProjet);
                String statut = elem.get("statut");

                //on ajoute un listener sur chaque tache du projet
                button = setListenerNewButton(button,name);

                LinearLayout layout;

                /* Rajout dans le tableau*/
                if(statut.equals(getString(R.string.a_faire)))
                    layout =  (LinearLayout) findViewById(R.id.linearLayoutAFaire);
                else if(statut.equals(getString(R.string.en_cours)))
                    layout =  (LinearLayout) findViewById(R.id.linearLayoutEnCours);
                else
                    layout =  (LinearLayout) findViewById(R.id.linearLayoutTerminee);

                layout.addView(button);
            }
        }
    }
    /**
     *  méthode de suppression d'une tâche à un projet et dans toutes les taches
     * @param listElements .get("nomTache")  get("nomProjet")  get("dateTache")
     */
    private void deleteTaskXML( HashMap<String, String> listElements){
        //suppression de la tache dans le projet
        gestionXML.deleteTacheProjet(listElements.get("nomTache"),listElements.get("nomProjet"));

        //suppression de toutes les taches
        gestionXML = new GestionXML(getString(R.string.toutesTaches),getApplicationContext());
        gestionXML.supprimerXML(getString(R.string.toutesTaches),listElements);

        loadXML();
        return;
    }

    private Button setListenerNewButton(Button button, String nameTask){
        button.setText(nameTask);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button bouton = (Button) v;

                //on crée une instance avec le contexte de la page et le style du bottom sheet
                bottomSheetDialog = new BottomSheetDialog(ScrumBoard.this,R.style.CustomBottomSheetDialog);
                View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout,
                        (ViewGroup) findViewById(R.id.bottom_sheet));

                // modification du nom de la bottom sheet
                TextView nomBottomSheet = (TextView) sheetView.findViewById(R.id.titreBottomSheet);
                nomBottomSheet.setText(bouton.getText().toString());

                EditText task = (EditText) sheetView.findViewById(R.id.nomBottomSheetTache);
                task.setText(bouton.getText().toString());

                EditText endDate =  sheetView.findViewById(R.id.editDateFin);
                EditText startDate = sheetView.findViewById(R.id.editDateDebut);
                HashMap<String, String> elementTask = gestionXML.getElementTache(bouton.getText().toString(),nameProjet);

                if(elementTask != null){
                    /*Priorité*/
                    Spinner spinner = sheetView.findViewById(R.id.prioriteesTache);
                    String myString = elementTask.get("prioritee");

                    ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
                    int spinnerPosition = myAdap.getPosition(myString);

                    spinner.setSelection(spinnerPosition);

                    /*Statut*/
                    spinner = sheetView.findViewById(R.id.statuts);
                    myString = elementTask.get("statut");

                    myAdap = (ArrayAdapter) spinner.getAdapter();
                    spinnerPosition = myAdap.getPosition(myString);

                    spinner.setSelection(spinnerPosition);

                    TextView description = sheetView.findViewById(R.id.descriptionBottomSheetTache);
                    description.setText(elementTask.get("description"));

                    startDate.setText(elementTask.get("dateDebut"));
                    endDate.setText(elementTask.get("dateFin"));

                    task.setTextColor(ScrumBoard.this.getColor(R.color.grisClair));
                    description.setTextColor(ScrumBoard.this.getColor(R.color.grisClair));
                    startDate.setTextColor(ScrumBoard.this.getColor(R.color.grisClair));
                    endDate.setTextColor(ScrumBoard.this.getColor(R.color.grisClair));
                }
                else{
                    Toast.makeText(getApplicationContext(),"erreur de recuperation des données de la tâche", Toast.LENGTH_SHORT).show();
                }

                setListenerEditsDate(startDate,endDate);

                Button delete = sheetView.findViewById(R.id.boutonSupprimerProjetSheet);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bouton.setVisibility(View.GONE);
                        deleteTaskXML(elementTask);
                        bottomSheetDialog.dismiss();
                    }
                });
                Button valider = (Button) sheetView.findViewById(R.id.boutonValiderProjetSheet);
                setListenerButtonValidate(bouton.getText().toString(),valider,button,sheetView,startDate,endDate,bottomSheetDialog,false);

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            }

        });
        button.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame);
        button.setTextColor(getApplicationContext().getColor(R.color.white));

        return button;
    }

    private void setListenerButtonValidate(String ancienNomTache,Button valider,Button buttonToChange, View sheetView, EditText editStartDate, EditText editEndDate, BottomSheetDialog bottomSheetDialog,Boolean ajout){

        //on fait ajouter le bouton au menu principal et on l'ajout dans le fichiers projets
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean edit = false;
                //récuperation de la nom de la tache
                EditText editTextName = (EditText) sheetView.findViewById(R.id.nomBottomSheetTache);
                String nameTask = editTextName.getText().toString();
                //récuperation de la description
                EditText editTextDesc = (EditText) sheetView.findViewById(R.id.descriptionBottomSheetTache);
                String describe = editTextDesc.getText().toString();

                //récuperation de la prioritée
                Spinner spinner = (Spinner) sheetView.findViewById(R.id.prioriteesTache);
                String priority = spinner.getSelectedItem().toString();

                //récuperation du statut
                spinner = (Spinner) sheetView.findViewById(R.id.statuts);
                String statut = spinner.getSelectedItem().toString();

                // Récupération des dates de début et de fin
                String startDate = editStartDate.getText().toString();
                String endDate = editEndDate.getText().toString();

                String debut[] = startDate.split("/");
                String fin[] = endDate.split("/");

                // vérification afin de ne pas enregistrer des tache avec des noms vides
                if(nameTask.length() < 1 ){
                    Toast.makeText(getApplicationContext(),"Le nom de la tâche doit être renseigné", Toast.LENGTH_LONG).show();
                    editTextName.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.supprimerBouton)));
                }
                else if(startDate.length() < 1){
                    Toast.makeText(getApplicationContext(),"Veuillez renseigner la date de début", Toast.LENGTH_LONG).show();
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.supprimerBouton)));
                }
                else if(endDate.length() < 1){
                    Toast.makeText(getApplicationContext(),"Veuillez renseigner la date de fin", Toast.LENGTH_LONG).show();
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.supprimerBouton)));
                }
                else if (Integer.parseInt(debut[2]) > Integer.parseInt(fin[2]) || Integer.parseInt(debut[1]) > Integer.parseInt(fin[1]) || Integer.parseInt(debut[0]) > Integer.parseInt(fin[0])) {
                    /* On compare les dates */
                    Toast.makeText(getApplicationContext(),"La date de début ne peut être supérieure à la date de fin", Toast.LENGTH_LONG).show();
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.supprimerBouton)));
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.supprimerBouton)));
                }
                else{
                    editTextName.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.gris)));
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.gris)));
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.gris)));

                    if(ajout) {
                        // on affiche la nouvelle tache à la fenetre
                        Button nouvelleTache = new Button(ScrumBoard.this);
                        nouvelleTache = setListenerNewButton(nouvelleTache,nameTask);

                        LinearLayout layout;

                        /* Rajout dans le tableau*/
                        if (statut.equals(getString(R.string.a_faire)))
                            layout = (LinearLayout) findViewById(R.id.linearLayoutAFaire);
                        else if (statut.equals(getString(R.string.en_cours)))
                            layout = (LinearLayout) findViewById(R.id.linearLayoutEnCours);
                        else
                            layout = (LinearLayout) findViewById(R.id.linearLayoutTerminee);

                        layout.addView(nouvelleTache);
                    }
                    else {
                        if (buttonToChange != null) {
                            buttonToChange.setText(nameTask);

                            ((ViewGroup) buttonToChange.getParent()).removeView(buttonToChange);

                            LinearLayout layout;
                            if (statut.equals(getString(R.string.a_faire)))
                                layout = (LinearLayout) findViewById(R.id.linearLayoutAFaire);
                            else if (statut.equals(getString(R.string.en_cours)))
                                layout = (LinearLayout) findViewById(R.id.linearLayoutEnCours);
                            else
                                layout = (LinearLayout) findViewById(R.id.linearLayoutTerminee);
                            layout.addView(buttonToChange);
                        }
                    }

                    // enregistrement des données
                    HashMap<String, String> listeElements = new HashMap<String, String>();
                    listeElements.put("nomTache",nameTask);
                    listeElements.put("nomProjet",nameProjet);
                    listeElements.put("ancienNomTache",ancienNomTache);
                    listeElements.put("prioritee",priority);
                    listeElements.put("description",describe);
                    listeElements.put("statut",statut);
                    listeElements.put("dateDebut",startDate);
                    listeElements.put("dateFin",endDate);

                    if(ajout)
                        addTaskXML(listeElements);
                    else
                        editTaskXML(listeElements);

                    //Fermeture de la bottom sheet
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }

    /**
     *  méthode d'édition d'une tâche à une section
     * @param listeElements .get("nomTache") || get("nomProjet") || get("dateTache")
     */
    private void editTaskXML(HashMap<String, String> listeElements) {
        //edition dans fichier projet
        gestionXML.editTacheProjetXML(listeElements);

        // modification dans toutes les taches
        gestionXML =  new GestionXML(getString(R.string.toutesTaches),getApplicationContext());
        gestionXML.editXML(getString(R.string.toutesTaches),listeElements);

        // rechargement du fichier projet
        loadXML();
    }

    private void setListenerEditsDate(EditText editStartDate, EditText editEndDate){
        DatePickerDialog.OnDateSetListener setListenerDateDebut = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editStartDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                editStartDate.setTextColor(getApplicationContext().getColor(R.color.gris));
            }
        };

        DatePickerDialog.OnDateSetListener setListenerDateFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editEndDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                editEndDate.setTextColor(getApplicationContext().getColor(R.color.gris));
            }
        };

        editStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ScrumBoard.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListenerDateDebut,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        editEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ScrumBoard.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListenerDateFin,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }

}

