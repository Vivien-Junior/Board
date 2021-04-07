package com.example.board;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    //Attributs
    private final Context context;
    private String nameCategory;
    private HashMap<String, List<String>> categoryElementList;
    private BottomSheetDialog bottomSheetDialog;

    public ExpandableListViewAdapter(Context context, String nameCategory, HashMap<String, List<String>> categoryElementList) {
        this.context = context;
        this.nameCategory = nameCategory;
        this.categoryElementList = categoryElementList;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.categoryElementList.get(nameCategory).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return nameCategory;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.categoryElementList.get(this.nameCategory).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String element = (String) getGroup(groupPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.nom_listes_expandables,null);
        }

        TextView category = convertView.findViewById(R.id.nom_listes_expandables);
        category.setText(element);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String element = (String) getChild(groupPosition,childPosition);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.liste_projets,null);
        }

        TextView elementCategory = convertView.findViewById(R.id.liste_projet);
        elementCategory.setText(element);

        if(element.equals(context.getResources().getString(R.string.ajouter_projet))) {
            elementCategory.setTypeface(null, Typeface.ITALIC);
            elementCategory.setTextColor(context.getColor(R.color.validerBouton));
        }
        else{
            elementCategory.setTypeface(null,Typeface.NORMAL);
            elementCategory.setTextColor(context.getColor(R.color.white));
        }


        // on ajoute un click listener sur les sous categories des categories "prioritées" "projets"
        elementCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(element.equals(context.getResources().getString(R.string.ajouter_projet))){
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    bottomSheetDialog = new BottomSheetDialog(context,R.style.CustomBottomSheetDialog);
                    View sheetView = inflater.inflate(R.layout.bottom_sheet_layout_ajout_projet,
                            null);
                    TextView tv = (TextView) sheetView.findViewById(R.id.titreBottomSheet);
                    tv.setText(context.getResources().getString(R.string.ajouter_projet_menu));

                    bottomSheetDialog.setContentView(sheetView);
                    bottomSheetDialog.show();

                    Button valider = (Button) sheetView.findViewById(R.id.boutonValiderProjetSheet);
                    //on fait ajouter le bouton au menu principal et on l'ajout dans le fichiers projets
                    valider.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView projectName = (TextView) sheetView.findViewById(R.id.nomProjetBottomSheet);
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("nomProjet", projectName.getText().toString());
                            intent.putExtra("dateDebutProjet", "");
                            intent.putExtra("dateFinProjet","");
                            context.startActivity(intent);
                        }
                    });
                }
                else if(nameCategory.equals(context.getResources().getString(R.string.liste_prioritees))){
                    Intent intent = new Intent(context, TemplatePrioriteesClass.class);
                    intent.putExtra("nomSection", element);
                    intent.putExtra("nomFichierXML",context.getResources().getString(R.string.toutesTaches));
                    context.startActivity(intent);
                }
                else{
                    Intent intent = new Intent(context, ScrumBoard.class);
                    intent.putExtra("nom", element);
                    intent.putExtra("nomSection",context.getResources().getString(R.string.projets));
                    context.startActivity(intent);
                }
            }
        });

        return convertView;
    }

    /*
     * méthode de suppression du dernier element de la liste
     * notifyDataSetChanged(): méthode utilisé pour notifier à l'observateur que l'on vient de modifier un élément
     */
    public void removeLast(){
        List<String> list = categoryElementList.get(getGroup(0));
        list.remove(getChildrenCount(0) - 1);
        notifyDataSetChanged();
        return;
    }

    /*
     * méthode d'ajout d'un élement à la liste
     * @param s: élément à rajouter
     * notifyDataSetChanged(): méthode utilisé pour notifier à l'observateur que l'on vient de modifier un élément
     */
    public void add(String s){
        List<String> list = categoryElementList.get(getGroup(0));
        list.add(s);
        notifyDataSetChanged();
        return;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
