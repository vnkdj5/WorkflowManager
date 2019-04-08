package com.workflow.component;

import com.opencsv.CSVReader;
import com.workflow.annotation.wfComponent;
import org.json.JSONArray;
import org.json.JSONObject;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@wfComponent(complete=true)
public class MissingValuesHandler implements Component{
    Entity output;
    Entity input;
    Entity config;

    int classIndex = 0;
    List<String> filenames;
    HashMap<String,String> featuretostrategy;
    @Override
    public boolean init() {
        featuretostrategy = new HashMap<>();
        ArrayList<HashMap<String,String>> featuresconfig = (ArrayList<HashMap<String, String>>) this.config.getEntity().get("Features");
        for(HashMap<String,String> f : featuresconfig){
            featuretostrategy.put(f.get("FeatureName"),f.get("Strategy"));
        }
        return true;
    }

    @Override
    public Entity process(Entity input) {
        System.out.println("akkak");
        try {
            Table data = Table.read().csv((String)config.getEntity().get("datasets"));
            Table str = data.structure();
            System.out.println(str);
            StringColumn att = str.stringColumn(1);
            StringColumn type = str.stringColumn(2);

            classIndex = att.size()-1;

            for(int i=0;i<type.size();i++){
                String colType = type.get(i);

                if(i==classIndex){


                }else if(colType.equals("INTEGER")){
                    NumberColumn col = data.numberColumn(att.get(i));
                    System.out.println(col.mean());
                    Double sbValue = null;
                    if(featuretostrategy.get(att.get(i)).equals("Mean")){
                        sbValue = col.mean();
                    }else if(featuretostrategy.get(att.get(i)).equals("Median")){
                        sbValue = col.median();
                    }else if(featuretostrategy.get(att.get(i)).equals("Min")){
                        sbValue = col.min();
                    }else{
                        sbValue = col.max();
                    }
                    col.set(col.isMissing(),(int)Math.floor(sbValue));
                }else if(colType.equals("DOUBLE")){
                    DoubleColumn col = data.doubleColumn(att.get(i));
                    Double sbValue = null;
                    if(featuretostrategy.get(att.get(i)).equals("Mean")){
                        sbValue = col.mean();
                    }else if(featuretostrategy.get(att.get(i)).equals("Median")){
                        sbValue = col.median();
                    }else if(featuretostrategy.get(att.get(i)).equals("Min")){
                        sbValue = col.min();
                    }else{
                        sbValue = col.max();
                    }
                    col.set(col.isMissing(),sbValue);

                }else if(colType.equals("STRING")){
                    StringColumn col = data.stringColumn(att.get(i));
                    col.set(col.isMissing(),"NA");
                }

            }
            data.where(data.column(att.get(classIndex)).isNotMissing()).write().csv((String)config.getEntity().get("datasets"));


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void listFilesForFolder(final File folder) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                if(fileEntry.getName().contains(".csv"))
                    filenames.add(fileEntry.getPath());
            }
        }
    }
    @Override
    public Entity getConfig() {

        String list="";
        filenames=new ArrayList<>();
        if(this.config!=null) {
            File dir = new File("uploadfiles/" + config.getEntity().get("WFId") + "/");
            listFilesForFolder(dir);
            list = "\""+filenames.get(0)+"\"";
            for(int i=1;i<filenames.size();i++){
                list+= ",\""+filenames.get(i)+"\"";
            }
        }

        String Configform="{\n" +
                "\t\"schema\": {\n" +
                "\t\t\"type\": \"object\",\n" +
                "\t\t\"title\": \"Missing Values Component\",\n" +
                "\t\t\"properties\": {\n" +
                "\t\t\t\"datasets\": {\n" +
                "\t\t\t\t\"title\": \"Dataset\",\n" +
                "\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\"enum\": [\n" +
                list +
                "\t\t\t\t]\n" +
                "\t\t\t},\n" +
                "\t\t\t\"Features\": {\n" +
                "\t\t\t\t\"type\": \"array\",\n" +
                "\t\t\t\t\"items\": {\n" +
                "\t\t\t\t\t\"type\": \"object\",\n" +
                "\t\t\t\t\t\"properties\": {\n" +
                "\t\t\t\t\t\t\"FeatureName\": {\n" +
                "\t\t\t\t\t\t\t\"readonly\": true,\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"Strategy\": {\n" +
                "\t\t\t\t\t\t\t\"readonly\": false,\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\t\t\t\"enum\": [\"Mean\", \"Median\", \"Min\", \"Max\"]\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"required\": []\n" +
                "\t},\n" +
                "\t\"form\": [{\n" +
                "\t\t\t\"type\": \"select\",\n" +
                "\t\t\t\"key\": \"datasets\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"type\": \"submit\",\n" +
                "\t\t\t\"title\": \"Get features\",\n" +
                "\t\t\t\"style\": \"btn btn-info\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\"items\": [{\n" +
                "\t\t\t\t\"add\": null,\n" +
                "\t\t\t\t\"htmlClass\": \"\",\n" +
                "\t\t\t\t\"notitle\": true,\n" +
                "\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\"htmlClass\": \"form-row\",\n" +
                "\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\"notitle\": false,\n" +
                "\t\t\t\t\t\t\t\t\"key\": \"['Features'][].['FeatureName']\"\n" +
                "\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\"notitle\": false,\n" +
                "\t\t\t\t\t\t\t\t\"key\": \"['Features'][].['Strategy']\"\n" +
                "\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t}\n" +
                "\n" +
                "\t\t\t\t\t]\n" +
                "\t\t\t\t}],\n" +
                "\t\t\t\t\"key\": \"Features\",\n" +
                "\t\t\t\t\"remove\": null\n" +
                "\t\t\t}]\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"submit\",\n" +
                "\t\t\t\"title\": \"Save\",\n" +
                "\t\t\t\"style\": \"btn btn-info\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "\n" +
                "}";
        filenames=null;
        Entity config = new Entity();
        config.addKeyValue("FORM", Configform);

        //create model
        if(this.config==null){
            this.config = new Entity();
        }
        config.addKeyValue("MODEL", this.config.getEntity());

        return config;

    }

    @Override
    public Entity getOutput() {
        return null;
    }

    @Override
    public Entity getInput(Component component) {
        return null;
    }

    @Override
    public void setInput(Entity input) {

    }

    @Override
    public void setOutput(Entity output) {

    }

    @Override
    public void setConfig(Entity config) {
        this.config = config;
        if( this.config.getEntity().get("datasets")!=null) {
            String filepath = (String) this.config.getEntity().get("datasets");
            try {
                FileReader reader = new FileReader(new File(filepath));
                CSVReader readHeaders = new CSVReader(reader);
                String[] headers = readHeaders.readNext();
                System.out.println(headers[0]);
                JSONArray features = new JSONArray();
                for (int i = 0; i < headers.length; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("FeatureName", headers[i]);

                    features.put(obj);
                }
                if(((ArrayList<JSONObject>)this.config.getEntity().get("Features")).size()!=features.length()) {
                    this.config.getEntity().put("Features", features.toList());
                    this.config.getEntity().put("NoOfFeatures", features.length());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        setOutput(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
