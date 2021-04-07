package com.example.board;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class GestionXML {
    /**
     * fichier : instance sur le fichier
     * nomFichier: nom du fichier que l'on souhaite lire/écrire dedans
     * contextApplication: contexte de l'application qui sert à obtenir les variables etc
     */
    private File file;
    private String fileName;
    private Context contextApplication;

    /**
     * constructeur, créer ou ouvre un fichier,s'il est déjà existant, et initialise les variables d'instance
     * @param file:
     * @param context
     */
    public GestionXML(String file, Context context){
        this.contextApplication = context;
        this.fileName = file + ".xml";
        this.file = new File(context.getFilesDir(), file + ".xml");
        try{
            if(!this.file.exists()){
                this.file.createNewFile();
                createXML();
            }
            else
                System.out.println("le fichier " + file + " existe déjà");
        }catch(IOException e)
        {
            Log.e("IOException", "Exception in create new File()");
        }

    }

    // getters
    private File getfile(){
        return this.file;
    }
    private Context getContextApplication(){
        return this.contextApplication;
    }
    private String getfileName(){
        return this.fileName;
    }

    /** méthode d'initialisation des fichiers avec les balises qui contiendront le contenu des xml:
     *
     * < ?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
     * <Board>
     * </Board>
     */
    private void createXML(){

        FileOutputStream fileos = null;
        try{
            fileos = new FileOutputStream(getfile());

        }catch(FileNotFoundException e)
        {
            Log.e("FileNotFoundException",e.toString());
        }
        XmlSerializer serializer = Xml.newSerializer();
        try{
            serializer.setOutput(fileos, "UTF-8");
            serializer.startDocument(null, Boolean.valueOf(true));
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag(null, "Board");
                serializer.startTag(null, "baliseVideObligatoire");
                serializer.endTag(null, "baliseVideObligatoire");
            serializer.endTag(null,"Board");
            serializer.endDocument();
            serializer.flush();
            fileos.close();

        }catch(Exception e)
        {
            Log.e("Exception","Exception problème lors de l'ecriture dans le XML");
        }
    }

    /** fonction d'append dans le xml
     * @param nomSection "prochainement", "taches terminees", "priorite 1"..
     * @param listeElements .get("nomTache") || get("prioritee") || get("dateTache")
     */
    public void appendXML(String nomSection,HashMap<String, String> listeElements){

        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getfile());
            root = dom.getDocumentElement();

            //création de la balise à concatener au fichier
            Element element = dom.createElement(nomSection);

            element.setAttribute("nomProjet",listeElements.get("nomProjet"));

            element.setAttribute("nomTache",listeElements.get("nomTache"));

            element.setAttribute("prioritee",listeElements.get("prioritee"));

            element.setAttribute("description",listeElements.get("description"));

            element.setAttribute("statut",listeElements.get("statut"));

            element.setAttribute("dateDebut",listeElements.get("dateDebut"));

            element.setAttribute("dateFin",listeElements.get("dateFin"));

            // on ajoute la nouvelle tâche à la fin du xml
            root.appendChild(element);

            // affichage dans le terminal + sauvegarde dans le fichier
            prettyPrint(dom);
        }
        catch (Exception e) {
            e.printStackTrace();

        }

    }

    /**
     * méthode de récuperation de toutes les taches d'un XML
     * @param nomSection
     * @return une liste contenant des table de hachage qui elles memes contenant toutes les infos pour une seule tâche
     */
    public List<HashMap<String,String>> lireXML(String nomSection ){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        List<HashMap<String,String> > backList = new ArrayList<HashMap<String,String> >();
        if(items.getLength() > 0){
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                HashMap<String,String> list  = new HashMap<String,String>();

                list.put("nomTache", item.getAttributes().getNamedItem("nomTache").getNodeValue());
                list.put("nomProjet", item.getAttributes().getNamedItem("nomProjet").getNodeValue());
                list.put("prioritee", item.getAttributes().getNamedItem("prioritee").getNodeValue());
                list.put("description", item.getAttributes().getNamedItem("description").getNodeValue());
                list.put("statut", item.getAttributes().getNamedItem("statut").getNodeValue());
                list.put("dateDebut", item.getAttributes().getNamedItem("dateDebut").getNodeValue());
                list.put("dateFin", item.getAttributes().getNamedItem("dateFin").getNodeValue());

                backList.add(list);
            }
        }
        return backList == null ? null : backList;
    }

    /**
     * méthode de récuperation d'une tache dans un XML
     * @param nomSection: nom de la section: vie_courante, rappels, toutes_taches...
     * @param nomTache: nom de la tache à recuperer
     * @return une table de hachage qui contient toutes les infos pour la tâche
     *
     */
    public HashMap<String,String>  lireTacheXML(String nomSection,String nomTache ){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        HashMap<String,String> backList = new HashMap<String,String>();
        if(items.getLength() > 0){
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                if(item.getAttributes().getNamedItem("nomTache").getNodeValue().equals(nomTache)){
                    backList.put("nomTache", item.getAttributes().getNamedItem("nomTache").getNodeValue());
                    backList.put("nomProjet", item.getAttributes().getNamedItem("nomProjet").getNodeValue());
                    backList.put("prioritee", item.getAttributes().getNamedItem("prioritee").getNodeValue());
                    backList.put("description",item.getAttributes().getNamedItem("description").getNodeValue());
                    backList.put("statut",item.getAttributes().getNamedItem("statut").getNodeValue());
                    backList.put("dateDebut", item.getAttributes().getNamedItem("dateDebut").getNodeValue());
                    backList.put("dateFin", item.getAttributes().getNamedItem("dateFin").getNodeValue());

                    return backList;
                }


            }
        }
        return backList == null ? null : backList;
    }

    /**
     * methode de lecture dans une XML d'une tache avec une priorité précise
     * @param nomSection: nom de la session du fichier
     * @param nomTache: nom de la tache
     * @param prioritee: prioritée de la tache
     * @return une table de hachage qui contient toutes les infos pour la tâche
     */
    public HashMap<String,String>  lireTachePrioriteXML(String nomSection,String nomTache,String prioritee ){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        HashMap<String,String> backList = new HashMap<String,String>();
        if(items.getLength() > 0){
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                if(item.getAttributes().getNamedItem("nomTache").getNodeValue().equals(nomTache) && item.getAttributes().getNamedItem("prioritee").getNodeValue().equals(prioritee) ){
                    backList.put("nomTache", item.getAttributes().getNamedItem("nomTache").getNodeValue());
                    backList.put("nomProjet", item.getAttributes().getNamedItem("nomProjet").getNodeValue());
                    backList.put("prioritee", item.getAttributes().getNamedItem("prioritee").getNodeValue());
                    backList.put("description",item.getAttributes().getNamedItem("description").getNodeValue());
                    backList.put("statut",item.getAttributes().getNamedItem("statut").getNodeValue());
                    backList.put("dateDebut", item.getAttributes().getNamedItem("dateDebut").getNodeValue());
                    backList.put("dateFin", item.getAttributes().getNamedItem("dateFin").getNodeValue());

                    return backList;
                }


            }
        }
        return backList == null ? null : backList;
    }



    /** fonction d'append d'un projet dans le xml
     * @param infosProjet get("nomProjet")  get("dateDebutProjet")  get("dateFinProjet")
     */
    public void appendProjetXML(HashMap<String, String> infosProjet){

        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getfile());
            root = dom.getDocumentElement();

            //création de la balise projets
            Element projet = dom.createElement(getContextApplication().getString(R.string.projets));
            projet.setAttribute("nomProjet",infosProjet.get("nomProjet"));
            projet.setAttribute("dateDebutProjet",infosProjet.get("dateDebutProjet"));
            projet.setAttribute("dateFinProjet",infosProjet.get("dateFinProjet"));
            root.appendChild(projet);

            // affichage dans le terminal + sauvegarde dans le fichier
            prettyPrint(dom);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * méthode qui retourne la liste des noms de projets
     * @param nomSection: section dans le xml dont on va chercher les projets "projets"
     * @return liste de chaines de caractères correspondant aux noms des projets
     */
    public List<String> lireProjetXML(String nomSection){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        List<String> backList = new ArrayList<String>();
        if(items.getLength() > 0){
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);
                backList.add(item.getAttributes().getNamedItem("nomProjet").getNodeValue());
            }
        }
        return backList == null ? null : backList;
    }

    /**
     * méthode d'ajout d'une tache à un projet spécifié en paramètre
     * @param listeElements .get("nomTache") || get("nomProjet") || get("dateTache") || get("prioritee") ..
     * @param nomProjet: nom du projet dans lequel on veut ajouter la tâche
     */
    public void appendTacheProjet(HashMap<String,String> listeElements, String nomProjet){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getfile());
            root = dom.getDocumentElement();

            //récuperation de la liste des projets
            items = root.getElementsByTagName(getContextApplication().getString(R.string.projets));

            //création de la balise tâche
            Element task = dom.createElement("tache");
            task.setAttribute("nomTache",listeElements.get("nomTache"));

            Element element = dom.createElement("prioritee");
            element.setAttribute("valeur",listeElements.get("prioritee"));
            task.appendChild(element);

            element = dom.createElement("description");
            element.setAttribute("valeur",listeElements.get("description"));
            task.appendChild(element);

            element = dom.createElement("statut");
            element.setAttribute("valeur",listeElements.get("statut"));
            task.appendChild(element);

            element = dom.createElement("dateDebut");
            element.setAttribute("valeur",listeElements.get("dateDebut"));
            task.appendChild(element);

            element = dom.createElement("dateFin");
            element.setAttribute("valeur",listeElements.get("dateFin"));
            task.appendChild(element);

            //ajout dans le bon projet
            if(items.getLength() > 0){
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    if(item.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(nomProjet))
                        items.item(i).appendChild(task);
                }
            }
            // affichage dans le terminal + sauvegarde dans le fichier
            prettyPrint(dom);
        }
        catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * méthode qui retourne la liste des taches d'un projet donné
     * @param nomProjet: nom du projet dont on veut recuperer toutes les tâches
     * @return liste de chaines de caractères correspondant aux noms des tâches
     */
    public List<String> lireTachesProjetXML(String nomProjet){
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList projectList;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            projectList = root.getElementsByTagName(getContextApplication().getString(R.string.projets));
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        List<String> backList = new ArrayList<String>();
        if(projectList.getLength() > 0){
            for (int i = 0; i < projectList.getLength(); i++) {
                Node nodeProject = projectList.item(i);
                if(nodeProject.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(nomProjet)){
                    // s'il a des noeuds le projet
                    if(nodeProject.hasChildNodes()){
                        // on parcours tout les noeuds fils du noeud avec le bon nom de projet
                        for (int j = 0; j < nodeProject.getChildNodes().getLength(); j++) {
                            Node noeudDuProjet = nodeProject.getChildNodes().item(j);
                            // on récuperer toutes les taches
                            if(noeudDuProjet.getNodeName().equals("tache")){
                                if(noeudDuProjet.hasAttributes()){
                                    backList.add(noeudDuProjet.getAttributes().getNamedItem("nomTache").getNodeValue());
                                }
                            }
                        }
                        return backList;
                    }
                }
            }
        }
        return backList == null ? null : backList;
    }

    /**
     * méthode qui retourne les éléments d'une tache donnée: la priorité, description de la tâche, date de début, date de fin
     * @param nomTache : nom de la tache à chercher
     * @param nomProjet: nom du projet dans lequel on cherche la tache en paramètre
     * @return une table de hachage qui contient toutes les infos pour une tache d'un projet
     */
    public HashMap<String, String> getElementTache(String nomTache, String nomProjet) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList projectsList;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            projectsList = root.getElementsByTagName(getContextApplication().getString(R.string.projets));
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        HashMap<String, String> backList = new HashMap<String, String>();
        if(projectsList.getLength() > 0){
            for (int i = 0; i < projectsList.getLength(); i++) {
                Node nodeProject = projectsList.item(i);
                if(nodeProject.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(nomProjet)){
                    // s'il a des noeuds le projet
                    if(nodeProject.hasChildNodes()){
                        // on parcours tout les noeuds fils du noeud avec le bon nom de projet
                        for (int j = 0; j < nodeProject.getChildNodes().getLength(); j++) {
                            Node nodeInProject = nodeProject.getChildNodes().item(j);
                            // on récuperer toutes les taches
                            if(nodeInProject.getNodeName().equals("tache")){
                                if (nodeInProject.getAttributes().getNamedItem("nomTache").getNodeValue().equals(nomTache)) {
                                    //si la tache à des noeuds
                                    if(nodeInProject.hasChildNodes()){
                                        backList.put("nomTache",nomTache);
                                        backList.put("nomProjet",nomProjet);
                                        for (int k = 0; k < nodeInProject.getChildNodes().getLength(); k++) {
                                            Node nodeTask = nodeInProject.getChildNodes().item(k);

                                            // on recupere la prioritée, la description, la date de début et de fin de la tache
                                            if(nodeTask.getNodeName().equals("prioritee")){
                                                backList.put("prioritee",nodeTask.getAttributes().getNamedItem("valeur").getNodeValue());
                                            }
                                            if(nodeTask.getNodeName().equals("description")){
                                                backList.put("description",nodeTask.getAttributes().getNamedItem("valeur").getNodeValue());
                                            }
                                            if(nodeTask.getNodeName().equals("statut")){
                                                backList.put("statut",nodeTask.getAttributes().getNamedItem("valeur").getNodeValue());
                                            }
                                            if(nodeTask.getNodeName().equals("dateDebut")){
                                                backList.put("dateDebut",nodeTask.getAttributes().getNamedItem("valeur").getNodeValue());
                                            }
                                            if(nodeTask.getNodeName().equals("dateFin")){
                                                backList.put("dateFin",nodeTask.getAttributes().getNamedItem("valeur").getNodeValue());System.out.println("test date fin" );
                                            }
                                        }
                                        return backList;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        return backList == null ? null : backList;
    }

    /**
     * affichage dans la console texte +  ajout dans le fichier
     * @param xml: parser sur le document xml
     * @throws Exception
     */
    public void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tf.setOutputProperty(OutputKeys.STANDALONE, "yes");
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        //tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));

        // on append les nouvelles données ici
        StreamResult res = new StreamResult(new File(getContextApplication().getFilesDir(),getfileName()));//Destination
        tf.transform(new DOMSource(xml), res);

        // affichage dans le terminal
        System.out.println(out.toString());
    }

    /**
     * méthode de suppression d'une tache dans un xml
     * @param nomSection: nom de la section que l'on va parcourir afin de chercher la tache
     * @param listeElements: élements de la tache
     */
    public void supprimerXML(String nomSection,HashMap<String, String> listeElements) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);

            if(items.getLength() > 0){
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);

                    if(item.getAttributes().getNamedItem("nomTache").getNodeValue().equals(listeElements.get("nomTache")) &&
                            item.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(listeElements.get("nomProjet"))){
                        Element deleteItem = (Element) dom.getElementsByTagName(nomSection).item(i);
                        deleteItem.getParentNode().removeChild(deleteItem);

                        // affichage dans le terminal + sauvegarde dans le fichier
                        prettyPrint(dom);
                        return;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        return;
    }

    /**
     * méthode qui retourne les éléments d'une tache donnée: la priorité, description de la tâche, date de début, date de fin
     * @param nomTache : nom de la tache à chercher
     * @param nomProjet: nom du projet dans lequel on cherche la tache en paramètre
     *
     */
    public void deleteTacheProjet(String nomTache, String nomProjet) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList projectList;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            projectList = root.getElementsByTagName(getContextApplication().getString(R.string.projets));
            if(projectList.getLength() > 0){
                for (int i = 0; i < projectList.getLength(); i++) {
                    Node nodeProject = projectList.item(i);
                    if(nodeProject.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(nomProjet)){
                        // s'il a des noeuds le projet
                        if(nodeProject.hasChildNodes()){
                            // on parcours tout les noeuds fils du noeud avec le bon nom de projet
                            for (int j = 0; j < nodeProject.getChildNodes().getLength(); j++) {
                                Node nodeInProject = nodeProject.getChildNodes().item(j);
                                // on récuperer toutes les taches
                                if(nodeInProject.getNodeName().equals("tache")){
                                    if (nodeInProject.getAttributes().getNamedItem("nomTache").getNodeValue().equals(nomTache)) {

                                        Element deleteItem = (Element) nodeInProject;
                                        deleteItem.getParentNode().removeChild(deleteItem);
                                        prettyPrint(dom);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }

        return;
    }

    /**
     * méthode d'édition d'une tache d'un projet
     * @param listeElements: tâche à modifier dans un projet
     */
    public void editTacheProjetXML(HashMap<String, String> listeElements) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList listeProjets;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            listeProjets = root.getElementsByTagName(getContextApplication().getString(R.string.projets));

            if(listeProjets.getLength() > 0){
                for (int i = 0; i < listeProjets.getLength(); i++) {
                    Node noeudProjet = listeProjets.item(i);
                    if(noeudProjet.getAttributes().getNamedItem(getContextApplication().getString(R.string.xml_nomProjet)).getNodeValue().equals(listeElements.get(getContextApplication().getString(R.string.xml_nomProjet)))){

                        // s'il a des noeuds le projet
                        if(noeudProjet.hasChildNodes()){
                            // on parcours tout les noeuds fils du noeud avec le bon nom de projet
                            for (int j = 0; j < noeudProjet.getChildNodes().getLength(); j++) {
                                Node noeudDuProjet = noeudProjet.getChildNodes().item(j);
                                // on récuperer toutes les taches
                                if(noeudDuProjet.getNodeName().equals("tache")){
                                    if (noeudDuProjet.getAttributes().getNamedItem(getContextApplication().getString(R.string.xml_nomTache)).getNodeValue().equals(listeElements.get("ancienNomTache"))) {
                                        Element elem = (Element) noeudDuProjet;
                                        elem.setAttribute(getContextApplication().getString(R.string.xml_nomTache),listeElements.get(getContextApplication().getString(R.string.xml_nomTache)));
                                        //si la tache à des noeuds
                                        if(noeudDuProjet.hasChildNodes()){
                                            for (int k = 0; k < noeudDuProjet.getChildNodes().getLength(); k++) {
                                                Node noeudTache = noeudDuProjet.getChildNodes().item(k);

                                                // on recupere la prioritée, la description, la date de début et de fin de la tache
                                                if(noeudTache.getNodeName().equals(getContextApplication().getString(R.string.xml_prioritee))){
                                                    elem = (Element) noeudTache;
                                                    elem.setAttribute("valeur",listeElements.get("prioritee"));

                                                }
                                                if(noeudTache.getNodeName().equals(getContextApplication().getString(R.string.xml_description))){
                                                    elem = (Element) noeudTache;
                                                    elem.setAttribute("valeur",listeElements.get("description"));
                                                }
                                                if(noeudTache.getNodeName().equals(getContextApplication().getString(R.string.xml_statut))){
                                                    elem = (Element) noeudTache;
                                                    elem.setAttribute("valeur",listeElements.get("statut"));
                                                }
                                                if(noeudTache.getNodeName().equals(getContextApplication().getString(R.string.xml_dateDebut))){
                                                    elem = (Element) noeudTache;
                                                    elem.setAttribute("valeur",listeElements.get("dateDebut"));
                                                }
                                                if(noeudTache.getNodeName().equals(getContextApplication().getString(R.string.xml_dateFin))){
                                                    elem = (Element) noeudTache;
                                                    elem.setAttribute("valeur",listeElements.get("dateFin"));
                                                }
                                            }
                                            prettyPrint(dom);
                                            return;
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return ;
        }


        return ;
    }

    /**
     * méthode d'édition d'une tache dans un xml
     * @param nomSection: nom de la section que l'on va parcourir afin de chercher la tache
     * @param listeElements: élements de la tache
     */
    public void editXML(String nomSection, HashMap<String, String> listeElements) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document dom;
        Element root;
        NodeList items;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            dom = builder.parse(getContextApplication().openFileInput(getfileName()));
            root = dom.getDocumentElement();
            items = root.getElementsByTagName(nomSection);

            if(items.getLength() > 0){
                for (int i = 0; i < items.getLength(); i++) {
                    Node item = items.item(i);
                    if(item.getAttributes().getNamedItem("nomTache").getNodeValue().equals(listeElements.get("ancienNomTache"))
                            && item.getAttributes().getNamedItem("nomProjet").getNodeValue().equals(listeElements.get("nomProjet"))
                    ){

                        Element newItem = (Element) dom.getElementsByTagName(nomSection).item(i);
                        newItem.setAttribute("nomProjet",listeElements.get("nomProjet"));

                        newItem.setAttribute("nomTache",listeElements.get("nomTache"));

                        newItem.setAttribute("prioritee",listeElements.get("prioritee"));

                        newItem.setAttribute("description",listeElements.get("description"));

                        newItem.setAttribute("statut",listeElements.get("statut"));

                        newItem.setAttribute("dateDebut",listeElements.get("dateDebut"));

                        newItem.setAttribute("dateFin",listeElements.get("dateFin"));


                        // affichage dans le terminal + sauvegarde dans le fichier
                        prettyPrint(dom);
                        return;
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        return;
    }
}
