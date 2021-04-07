package com.example.board;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.HashMap;

@SuppressLint("AppCompatCustomView")
public class Tache extends android.widget.Button {
    private String nameTask;
    private Context context;
    private String nameSection;
    private GestionXML gestionXML;

    public Tache(Context c, String nameSection){
        super(c);
        this.context = c;
        this.nameTask = nameTask;
        this.nameSection = nameSection;
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

    public void setListenerNewButton(String nameTask){
        this.setText(nameTask);
        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button bouton = (Button) v;

                //on crée une instance avec le contexte de la page et le style du bottom sheet
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.CustomBottomSheetDialog);
                View sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout,
                        (ViewGroup) findViewById(R.id.bottom_sheet));

                // modification du nom de la bottom sheet
                TextView nomBottomSheet = (TextView) sheetView.findViewById(R.id.titreBottomSheet);
                nomBottomSheet.setText(bouton.getText().toString());

                EditText task = (EditText) sheetView.findViewById(R.id.nomBottomSheetTache);
                task.setText(bouton.getText().toString());

                EditText endDate =  sheetView.findViewById(R.id.editDateFin);
                EditText startDate = sheetView.findViewById(R.id.editDateDebut);
                HashMap<String, String> elementTask = gestionXML.lireTacheXML("vc_tasks_001", getText().toString());

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

                    task.setTextColor(context.getColor(R.color.grisClair));
                    description.setTextColor(context.getColor(R.color.grisClair));
                    startDate.setTextColor(context.getColor(R.color.grisClair));
                    endDate.setTextColor(context.getColor(R.color.grisClair));
                }
                else{
                    Toast.makeText(context,"erreur de recuperation des données de la tâche", Toast.LENGTH_SHORT).show();
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
                Validate valider = sheetView.findViewById(R.id.boutonValiderProjetSheet);
                valider.setListenerButtonValidate(null, bouton.getText().toString(), Tache.this,sheetView,startDate,endDate,bottomSheetDialog,false);

                bottomSheetDialog.setContentView(sheetView);
                bottomSheetDialog.show();
            }

        });
        setBackgroundResource(android.R.drawable.dialog_holo_dark_frame);
        setTextColor(context.getColor(R.color.white));

    }

    private void setListenerEditsDate(EditText editStartDate, EditText editEndDate){
        DatePickerDialog.OnDateSetListener setListenerDateDebut = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editStartDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                editStartDate.setTextColor(context.getColor(R.color.gris));
            }
        };

        DatePickerDialog.OnDateSetListener setListenerDateFin = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                editEndDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                editEndDate.setTextColor(context.getColor(R.color.gris));
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
                        context,
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
                        context,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        setListenerDateFin,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
    }

    /**
     *  méthode de suppression d'une tâche à un projet et dans toutes les taches
     * @param listElements .get("nomTache")  get("nomProjet")  get("dateTache")
     */
    private void deleteTaskXML( HashMap<String, String> listElements){
        //suppression de la tache dans le projet
        gestionXML.deleteTacheProjet(listElements.get("nomTache"),listElements.get("nomProjet"));

        //suppression de toutes les taches
        gestionXML = new GestionXML(context.getString(R.string.toutesTaches),context);
        gestionXML.supprimerXML(context.getString(R.string.toutesTaches),listElements);

        loadXML();
        return;
    }
}
