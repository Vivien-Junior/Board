package com.example.board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;

@SuppressLint("AppCompatCustomView")
public class Validate extends android.widget.Button {

    private GestionXML gestionXML;

    private Context context;
    private String nameSection;

    public Validate (Context c){
        super(c);
        context = c;
        nameSection = null;
    }
    public Validate(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        nameSection = null;
    }

    public Validate(Context c, AttributeSet attrs, int defStyle) {
        super(c, attrs, defStyle);
        context = c;
        nameSection = null;
    }

    public void setListenerButtonValidate(String nameSection, String ancienNomTache,Button buttonToChange, View sheetView, EditText editStartDate, EditText editEndDate, BottomSheetDialog bottomSheetDialog,Boolean ajout){

        this.nameSection = nameSection;
        //on fait ajouter le bouton au menu principal et on l'ajout dans le fichiers projets
        setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(context,"Le nom de la tâche doit être renseigné", Toast.LENGTH_LONG).show();
                    editTextName.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.supprimerBouton)));
                }
                else if(startDate.length() < 1){
                    Toast.makeText(context,"Veuillez renseigner la date de début", Toast.LENGTH_LONG).show();
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.supprimerBouton)));
                }
                else if(endDate.length() < 1){
                    Toast.makeText(context,"Veuillez renseigner la date de fin", Toast.LENGTH_LONG).show();
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.supprimerBouton)));
                }
                else if (Integer.parseInt(debut[2]) > Integer.parseInt(fin[2]) || Integer.parseInt(debut[1]) > Integer.parseInt(fin[1]) || Integer.parseInt(debut[0]) > Integer.parseInt(fin[0])) {
                    /* On compare les dates */
                    Toast.makeText(context,"La date de début ne peut être supérieure à la date de fin", Toast.LENGTH_LONG).show();
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.supprimerBouton)));
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.supprimerBouton)));
                }
                else{
                    editTextName.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.gris)));
                    editEndDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.gris)));
                    editStartDate.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.gris)));

                    if(ajout) {
                        // on affiche la nouvelle tache à la fenetre
                        Tache nouvelleTache = new Tache(context, nameSection);
                        nouvelleTache.setListenerNewButton(nameTask);
                        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View convertView = inflater.inflate(R.layout.viecourante,null);
                        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.linearLayoutVC);
                        layout.addView(nouvelleTache);
                    }
                    else {
                        if (buttonToChange != null) {
                            buttonToChange.setText(nameTask);
                            ((ViewGroup) buttonToChange.getParent()).removeView(buttonToChange);
                            LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutVC);
                            layout.addView(buttonToChange);
                        }
                    }

                    // enregistrement des données
                    HashMap<String, String> listeElements = new HashMap<String, String>();
                    listeElements.put("nomTache",nameTask);
                    listeElements.put("nomProjet","");
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
     *  méthode d'ajout d'une tâche à un projet
     * @param listeElements .get("nomTache")  get("nomProjet")  get("dateTache")
     */
    private void addTaskXML( HashMap<String, String> listeElements){
        gestionXML.appendXML("vc_tasks_001", listeElements);

        //ajout dans le fichier contenant toutes les tâches
        gestionXML = new GestionXML(context.getString(R.string.toutesTaches),context);
        listeElements.put("nomProjet",context.getString(R.string.vc_tasks_001));
        gestionXML.appendXML(context.getString(R.string.toutesTaches),listeElements);
        loadXML();
        return;
    }

    /**
     *  méthode d'édition d'une tâche à une section
     * @param listeElements .get("nomTache") || get("nomProjet") || get("dateTache")
     */
    private void editTaskXML(HashMap<String, String> listeElements) {
        //edition dans fichier projet
        gestionXML.editTacheProjetXML(listeElements);

        // modification dans toutes les taches
        gestionXML =  new GestionXML(context.getString(R.string.toutesTaches),context);
        gestionXML.editXML(context.getString(R.string.toutesTaches),listeElements);

        // rechargement du fichier projet
        loadXML();
    }

    /**
     * Méthode de chargement du XML de la section concerné
     * si c'est la premiere que l'on vient dans la section le XML va se créer automatiquement
     */
    private void loadXML(){
        if(nameSection == null)
            gestionXML = new GestionXML("vc_tasks_001",context);
        else
            gestionXML = new GestionXML(this.nameSection,context);
    }
}