package com.workflow.component;

import com.opencsv.CSVWriter;
import com.workflow.annotation.wfComponent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@wfComponent(complete=true)
public class CsvWriter implements Component {

    Entity output;
    Entity input;
    Entity config;

    CSVWriter writer;
    int numOfHeaders=0;
    String[] headers;
    String[] datatypes;


    @Override
    public boolean init() {
        File dir = new File("uploadfiles/"+config.getEntity().get("WFId")+"/"+config.getEntity().get("ComponentId")+"/");
        if (!dir.exists())
            dir.mkdirs();

        File filepath= new File("uploadfiles/"+config.getEntity().get("WFId")+"/"+config.getEntity().get("ComponentId")+"/"+config.getEntity().get("filename"));

        System.out.println(filepath.getAbsolutePath());
        try {
            FileWriter csvFile = new FileWriter(filepath);
            writer = new CSVWriter(csvFile,',',CSVWriter.NO_QUOTE_CHARACTER);

            ArrayList<HashMap<String,Object>> inputfields = (ArrayList<HashMap<String,Object>>) this.input.getEntity().get("input");

            numOfHeaders = inputfields.size();
            headers = new String[numOfHeaders];
            datatypes=new String[numOfHeaders];
            for(int i=0;i<numOfHeaders;i++){
                headers[i] = (String)inputfields.get(i).get("fieldName");
                datatypes[i] = (String)inputfields.get(i).get("dataType");
            }
            System.out.println(inputfields.toString());
            writer.writeNext(headers);
            writer.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    }

    @Override
    public Entity process(Entity input) {
        if(input==null){
            return null;
        }
        String data[] = new String[numOfHeaders];
        for(int i=0;i<numOfHeaders;i++){
            data[i]=(String)input.getEntity().get(headers[i]);
        }
        writer.writeNext(data);
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Entity getConfig() {
        String Configform = "{\"schema\":{\n" +
                "\t\t\"type\": \"object\",\n" +
                "\t\t\"title\": \"CsvWriter\",\n" +
                "\t\t\"properties\": {\n" +
                "\t\t\t\"filename\": {\n" +
                "\t\t\t\t\"title\": \"File Name\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"seperator\": {\n" +
                "\t\t\t\t\"title\": \"Separator\",\n" +
                "\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\"default\":\",\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"quotechar\": {\n" +
                "\t\t\t\t\"title\": \"Quote Character\",\n" +
                "\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\"default\":\"\\\"\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"escapechar\": {\n" +
                "\t\t\t\t\"title\": \"Escape Character\",\n" +
                "\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\"default\":\"\\\\\"\n" +
                "\t\t\t},\n" +
                "\t\t\t\"lineend\": {\n" +
                "\t\t\t\t\"title\": \"Line End Character\",\n" +
                "\t\t\t\t\"type\": \"string\",\n" +
                "\t\t\t\t\"default\":\"\\\\n\"\n" +
                "\t\t\t}\n" +
                "\t\t},\n" +
                "\t\t\"required\": [\"filename\"]\n" +
                "\t},\n" +
                "\t\t\"form\":[\n" +
                "    { \n" +
                "      \"key\":\"filename\",\"placeholder\":\"FileName.csv\"\n" +
                "    },\n" +
                "    {\n" +
                "    \"type\":\"section\",\n" +
                "    \"htmlClass\":\"row\",\n" +
                "    \"items\":[\n" +
                "      {\n" +
                "        \"type\":\"section\",\n" +
                "        \"htmlClass\":\"col-md-6\",\n" +
                "        \"items\":[{\"key\":\"seperator\", \"readonly\":true}]\n" +
                "      },\n" +
                "      {\n" +
                "        \"type\":\"section\",\n" +
                "        \"htmlClass\":\"col-md-6\",\n" +
                "        \"items\":[{\"key\":\"quotechar\", \"readonly\":true}]\n" +
                "      }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "    \"type\":\"section\",\n" +
                "    \"htmlClass\":\"row\",\n" +
                "    \"items\":[\n" +
                "      {\n" +
                "        \"type\":\"section\",\n" +
                "        \"htmlClass\":\"col-md-6\",\n" +
                "        \"items\":[{\"key\":\"escapechar\", \"readonly\":true}]\n" +
                "      },\n" +
                "      {\n" +
                "        \"type\":\"section\",\n" +
                "        \"htmlClass\":\"col-md-6\",\n" +
                "        \"items\":[{\"key\":\"lineend\", \"readonly\":true}]\n" +
                "      }\n" +
                "      ]\n" +
                "    },\n" +
                "    { \n" +
                "      \"type\":\"submit\",\n" +
                "      \"title\": \"Save\",\n" +
                "      \"style\":\"btn btn-info\",\n" +
                "      \"htmlClass\":\"text-center\"\n" +
                "    }\n" +
                "    ]\n" +
                "}";

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
        return output;
    }

    @Override
    public Entity getInput(Component component) {
        if(component!=null){
            setInput(component.getOutput());
        }else{
            setInput(null);
        }
        return input;
    }

    @Override
    public void setInput(Entity input) {

        if(input!=null) {

            this.input = new Entity();
            this.input.addKeyValue("input", input.getEntity().get("output"));

        }
        else{
            this.input=new Entity();
        }
    }

    @Override
    public void setOutput(Entity output) {
        this.output = new Entity();
    }

    @Override
    public void setConfig(Entity config) {
        this.config = config;
        setOutput(null);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
