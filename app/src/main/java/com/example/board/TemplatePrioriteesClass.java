package com.example.board;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class TemplatePrioriteesClass extends AppCompatActivity {
    /**
     * sectionName : "prochainement" "aujourd'hui", "taches terminees", "priorite 1".. qui sert au chargement du bon XML
     * nameXLM : nom du xml dans lequel on va enregistrer les données
     * gestionXML: instance sur le XML
     * bottomSheetDialog: instance sur la fenetre bottom sheet
     * toast: Toast de gestion de message d'erreurs
     */
    private String sectionName;
    private String nameXML;
    private GestionXML gestionXML;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template);
        setValeurs();
        loadXML();
        displayList();
        setTitle(this.sectionName);
    }


    /**
     * initialisation des valeurs fournie par l'intent
     */
    private void setValeurs(){

        // reprise de la main par la page
        Intent intent = getIntent();

        //récupération des données
        String sectionName = intent.getStringExtra("nomSection");
        if(!sectionName.isEmpty())
            this.sectionName = sectionName;


        String fileNameXML = intent.getStringExtra("nomFichierXML");
        if(!fileNameXML.isEmpty())
            this.nameXML = fileNameXML;

    }

    private void displayList()  {
        List<HashMap<String,String>> backList = gestionXML.lireXML(this.nameXML);
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);

        if(backList != null){
            // on va parcourir chaque liste contenant une tache et un ensemble d'élements associés à la tâche
            for( HashMap<String,String> list : backList){
                Button button = new Button(this);
                setInfoButton(button,list);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = splitName(button.getText().toString());

                        //on crée une instance avec le contexte de la page et le style du bottom sheet
                        bottomSheetDialog = new BottomSheetDialog(TemplatePrioriteesClass.this,R.style.CustomBottomSheetDialog);

                        View sheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout,
                                (ViewGroup) findViewById(R.id.bottom_sheet));
                        EditText editStartDate = sheetView.findViewById(R.id.editDateDebut);
                        EditText editEndDate = sheetView.findViewById(R.id.editDateFin);

                        // modification du nom de la bottom sheet
                        TextView nameBottomSheet = (TextView) sheetView.findViewById(R.id.titreBottomSheet);
                        nameBottomSheet.setText(name);

                        TextView task = (TextView) sheetView.findViewById(R.id.nomBottomSheetTache);
                        task.setText(name);

                        /* VARIABLES */
                        Spinner spinner;
                        EditText description;
                        Button delete = sheetView.findViewById(R.id.boutonSupprimerProjetSheet);
                        delete.setVisibility(View.GONE);

                        HashMap<String, String> taskElement = gestionXML.lireTacheXML(nameXML, name);
                        if(taskElement != null){
                            /*Priorité*/
                            spinner = sheetView.findViewById(R.id.prioriteesTache);
                            String myString = taskElement.get(getString(R.string.xml_prioritee));

                            ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
                            int spinnerPosition = myAdap.getPosition(myString);

                            spinner.setSelection(spinnerPosition);

                            /*Statut*/
                            spinner = sheetView.findViewById(R.id.statuts);
                            myString = taskElement.get(getString(R.string.xml_statut));

                            myAdap = (ArrayAdapter) spinner.getAdapter();
                            spinnerPosition = myAdap.getPosition(myString);

                            spinner.setSelection(spinnerPosition);

                            description = sheetView.findViewById(R.id.descriptionBottomSheetTache);
                            description.setText(taskElement.get(getString(R.string.xml_description)));

                            editStartDate.setText(taskElement.get(getString(R.string.xml_dateDebut)));
                            editEndDate.setText(taskElement.get(getString(R.string.xml_dateFin)));

                            task.setTextColor(TemplatePrioriteesClass.this.getColor(R.color.grisClair));
                            description.setTextColor(TemplatePrioriteesClass.this.getColor(R.color.grisClair));
                            editStartDate.setTextColor(TemplatePrioriteesClass.this.getColor(R.color.grisClair));
                            editEndDate.setTextColor(TemplatePrioriteesClass.this.getColor(R.color.grisClair));
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Erreur de récuperation des données de la tâche", Toast.LENGTH_SHORT).show();
                        }

                        setListenerEditsDate(editStartDate,editEndDate);
                        // on affiche la nouvelle tache à la fenetre
                        Button valider = sheetView.findViewById(R.id.boutonValiderProjetSheet);

                        valider.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                //récuperation de la nom de la tache
                                EditText editText = (EditText) sheetView.findViewById(R.id.nomBottomSheetTache);
                                String nameTask = editText.getText().toString();
                                //récuperation de la description
                                editText = (EditText) sheetView.findViewById(R.id.descriptionBottomSheetTache);
                                String describe = editText.getText().toString();

                                //récuperation de la prioritée
                                Spinner spinner = (Spinner) sheetView.findViewById(R.id.prioriteesTache);
                                String priority = spinner.getSelectedItem().toString();

                                //récuperation du statut
                                spinner = (Spinner) sheetView.findViewById(R.id.statuts);
                                String statut = spinner.getSelectedItem().toString();

                                // Récupération des dates de début et de fin
                                String dateDeb = editStartDate.getText().toString();
                                String dateFin = editEndDate.getText().toString();
                                if(nameTask.length() < 1){
                                    Toast toast = Toast.makeText(getApplicationContext(),"le nom de la tâche ne peut pas etre vide", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                                else{

                                    // enregistrement des données
                                    HashMap<String, String> listeElements = new HashMap<String, String>();
                                    listeElements.put("nomTache",nameTask);
                                    listeElements.put("nomProjet",list.get(getString(R.string.xml_nomProjet)));
                                    listeElements.put("ancienNomTache",name);
                                    listeElements.put("prioritee",priority);
                                    listeElements.put("description",describe);
                                    listeElements.put("statut",statut);
                                    listeElements.put("dateDebut",dateDeb);
                                    listeElements.put("dateFin",dateFin);

                                    setInfoButton(button,listeElements);
                                    editTacheXML(listeElements);

                                    //Fermeture de la bottom sheet
                                    bottomSheetDialog.dismiss();


                                }
                            }

                        });
                        bottomSheetDialog.setContentView(sheetView);
                        bottomSheetDialog.show();

                    }
                });

                button.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame);
                button.setTextColor(getApplicationContext().getColor(R.color.white));
                layout.addView(button);

            }
        }
    }

    /**
     * affichage de la liste des taches ayant cette prioritées
     * @param button : button qui contiendra la tâche
     * @param list: infos sur la prioritées (nom projet, date de fin, de début..)
     */
    private void setInfoButton(Button button, HashMap<String, String> list) {

        // les prioritées
        if(!list.get("nomProjet").isEmpty()){
            if(!list.get(getString(R.string.xml_nomProjet)).equals("vc_tasks_001"))
                button.setText(list.get("dateDebut") + ": " +  list.get("nomTache") + " - " + list.get("nomProjet"));
            else
                button.setText(list.get(getString(R.string.xml_dateDebut)) + " - " + list.get(getString(R.string.xml_dateFin)) + ": " +list.get(getString(R.string.xml_nomTache)) + " - " + getString(R.string.vie_courante));
        }
    }

    /**
     * Méthode de chargement du XML de la section concerné
     * si c'est la premiere que l'on vient dans la section le XML va se créer automatiquement
     */
    private void loadXML(){
        gestionXML = new GestionXML(this.nameXML,getApplicationContext());
    }

    private String splitName(String nom) {
        String chaineRetour = new String();
        String[] chaineTemp;
        chaineTemp = nom.split(":");
        chaineTemp = chaineTemp[1].split("-");
        chaineRetour = chaineTemp[0];
        chaineRetour = chaineRetour.trim();
        return chaineRetour;
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
                        TemplatePrioriteesClass.this,
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
                        TemplatePrioriteesClass.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListenerDateFin,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }

    /**
     *  méthode d'édition d'une tâche à une section
     * @param listeElements .get("nomTache") || get("nomProjet") || get("dateTache")
     */
    private void editTacheXML(HashMap<String, String> listeElements) {

        if(!listeElements.get(getString(R.string.xml_nomProjet)).isEmpty()){

            gestionXML = new GestionXML(getString(R.string.projets),getApplicationContext());
            gestionXML.editTacheProjetXML(listeElements);
        }
        loadXML();
        gestionXML.editXML(getString(R.string.toutesTaches),listeElements);
    }

}
