package com.workflow.component;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.workflow.annotation.wfComponent;

import com.workflow.exceptions.GenericRuntimeException;
import net.sf.javaml.tools.data.FileHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.query.Collation;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.NumberColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@wfComponent(complete=true)
public class KNNClassifier implements MLComponent{

    Entity output;
    Entity input;
    Entity config;

    List<String> filenames;
    Classifier classifier;
    Instances data;
    String evaluation;
    String[] labels;
    @Override
    public boolean init() {
        try {
            System.out.println(this.config.getEntity().get("datasets"));
            Table table = Table.read().csv((String) this.config.getEntity().get("datasets"));
            Table str = table.structure();
            StringColumn att = str.stringColumn(1);
            StringColumn type = str.stringColumn(2);
            int classIndex = att.size()-1;


            StringColumn col = table.stringColumn(classIndex);
            StringColumn ucol = col.unique();
            labels  = new String[ucol.size()];
            for(int i=0;i<ucol.size();i++){
                labels[i]=ucol.getString(i);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("init complete");
        return true;
    }

    @Override
    public Entity process(Entity input) {

        try {
            System.out.println(this.config.getEntity().get("datasets"));
            String f1 = (String) this.config.getEntity().get("datasets");
            CSVLoader loader = new CSVLoader();

            loader.setSource(new File(f1));
            loader.setNoHeaderRowPresent(false);

            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
            classifier = new IBk((int)config.getEntity().get("k"));

            classifier.buildClassifier(data);


            Evaluation eval = new Evaluation(data);
            eval.evaluateModel(classifier,data);

            evaluation = eval.toSummaryString();

            System.out.println(evaluation);

        }catch (Exception e){
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
        System.out.println(filenames);
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





        String Configform = "{\n" +
                "\t\"schema\": {\n" +
                "\t\t\"type\": \"object\",\n" +
                "\t\t\"title\": \"KNN Classifier\",\n" +
                "\t\t\"properties\": {\n" +
                "\t\t\t\"k\": {\n" +
                "\t\t\t\t\"title\": \"Number of clusters\",\n" +
                "\t\t\t\t\"type\": \"number\"\n" +
                "\t\t\t},\n" +
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
                "\t\t\t\t\t\t\"featureName\": {\n" +
                "\t\t\t\t\t\t\t\"readonly\": true,\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t\"featureValue\": {\n" +
                "\t\t\t\t\t\t\t\"readonly\": false,\n" +
                "\t\t\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t}\n" +
                "\n" +
                "\t\t\t},\n" +
                "\t\t\t\"knnOutput\": {\n" +
                "\t\t\t\t\"title\": \"Output\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"required\": [\"filename\"]\n" +
                "\t},\n" +
                "\t\"form\": [{\n" +
                "\t\t\t\"key\": \"k\",\n" +
                "\t\t\t\"placeholder\": \"00\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"type\": \"select\",\n" +
                "\t\t\t\"key\": \"datasets\"\n" +
                "\t\t}, {\n" +
                "\t\t\t\"type\": \"submit\",\n" +
                "\t\t\t\"title\": \"Save\",\n" +
                "\t\t\t\"style\": \"btn btn-info\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\"\n" +
                "\t\t},\n" +
                "\n" +
                "\n" +
                "\t\t{\n" +
                "\t\t\t\"htmlClass\": \"row\",\n" +
                "\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\"items\": [{\n" +
                "\t\t\t\t\"add\": null,\n" +
                "\t\t\t\t\"htmlClass\": \"\",\n" +
                "\t\t\t\t\"notitle\": false,\n" +
                "\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\"htmlClass\": \"form-row\",\n" +
                "\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\"notitle\": true,\n" +
                "\t\t\t\t\t\t\t\t\"key\": \"['Features'][].['featureName']\"\n" +
                "\t\t\t\t\t\t\t}]\n" +
                "\t\t\t\t\t\t},\n" +
                "\t\t\t\t\t\t{\n" +
                "\t\t\t\t\t\t\t\"htmlClass\": \"col-md-6\",\n" +
                "\t\t\t\t\t\t\t\"type\": \"section\",\n" +
                "\t\t\t\t\t\t\t\"items\": [{\n" +
                "\t\t\t\t\t\t\t\t\"notitle\": true,\n" +
                "\t\t\t\t\t\t\t\t\"key\": \"['Features'][].['featureValue']\"\n" +
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
                "\t\t\t\"onClick\": \"function(modelValue,myForm){ var scope1 = angular.element($('#mainScope')).scope();scope1.onSubmit(myForm); let WFID=scope1.currentWorkflowName; let compId=scope1.selectedComponent.key;var xhttp = new XMLHttpRequest();xhttp.onreadystatechange = function() {if (this.readyState == 4 && this.status == 200) {console.log(xhttp.responseText);document.getElementById('knnOutput').value=xhttp.responseText}};xhttp.open('POST', 'predict/'+WFID+'/'+compId, true);xhttp.setRequestHeader('Content-type', 'application/json');xhttp.send(JSON.stringify(scope1.model.Features));      }\",\n" +
                "\t\t\t\"htmlClass\": \"text-center\",\n" +
                "\t\t\t\"style\": \"btn-info\",\n" +
                "\t\t\t\"type\": \"button\",\n" +
                "\t\t\t\"title\": \"Predict\"\n" +
                "\t\t},\n" +
                "\t\t{\n" +
                "\t\t\t\"key\": \"knnOutput\",\n" +
                "\t\t\t\"type\": \"textarea\",\n" +
                "\t\t\t\"placeholder\": \"Prediction Output\",\n" +
                "\t\t\t\"readonly\": \"true\"\n" +
                "\t\t}\n" +
                "\t]\n" +
                "}";
        filenames=null;
        Entity config = new Entity();
        config.addKeyValue("FORM", Configform);

        //create model
        if(this.config==null){
            this.config = new Entity();
        }
        config.addKeyValue("MODEL", this.config.getEntity());
        System.out.println("end config");
        return config;
    }

    @Override
    public Entity getOutput() {
        return output;
    }

    @Override
    public Entity getInput(Component component) {
        input = new Entity();
        return input;
    }

    @Override
    public void setInput(Entity input) {
        this.input = new Entity();
    }

    @Override
    public void setOutput(Entity output) {
        this.output = new Entity();
    }

    @Override
    public void setConfig(Entity config) {
        this.config = config;

            String filepath = (String) this.config.getEntity().get("datasets");
            try {
                FileReader reader = new FileReader(new File(filepath));
                CSVReader readHeaders = new CSVReader(reader);
                String[] headers = readHeaders.readNext();
                System.out.println(headers[0]);
                JSONArray features = new JSONArray();
                for (int i = 0; i < headers.length - 1; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("featureName", headers[i]);

                    features.put(obj);
                }

                this.config.getEntity().put("Features", features.toList());
                this.config.getEntity().put("NoOfFeatures", features.length());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        setOutput(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public HashMap<String, Object> predict(HashMap<String,Object> df) {
        System.out.println(df.toString());
        init();
        process(null);

        ArrayList<Attribute> list = Collections.list(data.enumerateAttributes());
        String[] headers = new String[data.numAttributes()];
        String[] d = new String[data.numAttributes()];
        for(int i=0;i<list.size();i++){
            headers[i] = list.get(i).name();
            d[i]=(String)df.get(headers[i]);

        }
        headers[list.size()]=data.classAttribute().name();
        d[list.size()] = "1.0";
        HashMap<String,Object> map = new HashMap<>();
        try {

            File f = new File("uploadfiles/" + config.getEntity().get("WFId") + "/"+config.getEntity().get("ComponentId"));
            if(!f.exists()){
                f.mkdirs();
            }
            CSVWriter writer = new CSVWriter(new FileWriter(new File("uploadfiles/" + config.getEntity().get("WFId") + "/"+config.getEntity().get("ComponentId")+"/prediction.csv")));

            writer.writeNext(headers);

            writer.writeNext(d);

            writer.flush();

            CSVLoader loader = new CSVLoader();

            loader.setSource(new File("uploadfiles/" + config.getEntity().get("WFId") + "/"+config.getEntity().get("ComponentId")+"/prediction.csv"));
            loader.setNoHeaderRowPresent(false);

            Instances predI = loader.getDataSet();
            predI.setClassIndex(data.numAttributes()-1);
            Instance pred = predI.get(0);

            System.out.println(classifier.classifyInstance(pred));
            double[] val = classifier.distributionForInstance(pred);
            System.out.println(val[0]+" --- "+val[1]+" --- "+val[2] +" "+data.numClasses());
            int maxI=0;
            for(int i=0;i<val.length;i++){
                if(val[maxI]<val[i]){
                    maxI=i;
                }
            }

            map = new HashMap<>();
            map.put("prediction",data.classAttribute().name()+":"+labels[maxI]);
            // map.put("Evaluation",evaluation);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GenericRuntimeException("Prediction Error");
        }





        //System.out.println(instance.toString());

        return map;

    }
}
