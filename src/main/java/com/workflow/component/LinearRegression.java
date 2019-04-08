package com.workflow.component;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.workflow.annotation.wfComponent;
import com.workflow.exceptions.GenericRuntimeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

@wfComponent(complete=true)
public class LinearRegression implements MLComponent {

    Entity output;
    Entity input;
    Entity config;

    List<String> filenames;
    Classifier classifier;
    Instances data;
    String evaluation;
    @Override
    public boolean init() {

        return true;
    }

    @Override
    public Entity process(Entity input) {

        try{
            String f1 = (String) this.config.getEntity().get("datasets");
            // load the CSV file (input file)
            CSVLoader loader = new CSVLoader();

            loader.setSource(new File(f1));
            loader.setNoHeaderRowPresent(false);
            //String [] options = new String[1];
            //options[0]="-H";
            //loader.setOptions(options);
            data = loader.getDataSet();
            data.setClassIndex(data.numAttributes()-1);

            classifier = new weka.classifiers.functions.LinearRegression();

            classifier.buildClassifier(data);

            Evaluation eval= new Evaluation(data);
            eval.evaluateModel(classifier,data);
            System.out.println("** Linear Regression Evaluation with Datasets **");
            System.out.println(eval.toSummaryString());
            System.out.print(" the expression for the input data as per alogorithm is ");
            System.out.println(classifier);
            evaluation = eval.toSummaryString();
            Instance ins = data.get(20);
            double value =  classifier.classifyInstance(ins);

            System.out.println(value);
            //System.out.println(data);
            // save as an  ARFF (output file)
//            ArffSaver saver = new ArffSaver();
//            saver.setInstances(data);
//            saver.setFile(new File(f2));
//            saver.writeBatch();

        }
        catch(Exception e){
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
        //System.out.println(filenames);
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
                "\t\t\"title\": \"Logistic Regression\",\n" +
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
                "\t\"form\": [\n" +
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

        return config;
    }

    @Override
    public Entity getOutput()    {
        return output;
    }

    @Override
    public Entity getInput(Component component) {
        this.input = new Entity();
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
        System.out.println("in KNN COnfid ");
        this.config = config;
        if(this.config.getEntity().get("datasets")!=null) {
            String filepath = (String) this.config.getEntity().get("datasets");
            try {
                FileReader reader = new FileReader(new File(filepath));
                CSVReader readHeaders = new CSVReader(reader);
                String[] headers = readHeaders.readNext();
                System.out.println(headers[0]);
                JSONArray features = new JSONArray();
                for (int i = 0; i < headers.length-1; i++) {
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
        }
        setOutput(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public HashMap<String, Object> predict(HashMap<String, Object> df){
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
        d[list.size()] = "0.0";
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

            Instance pred = predI.get(0);

            System.out.println(classifier.classifyInstance(pred));
            map = new HashMap<>();
            map.put("prediction",data.classAttribute().name()+":"+classifier.classifyInstance(pred));
           // map.put("Evaluation",evaluation);

        } catch (Exception e) {
            e.printStackTrace();
            throw new GenericRuntimeException("Prediction Error");
        }





        //System.out.println(instance.toString());

        return map;

    }
}
