package com.workflow.controller;

import com.workflow.component.Entity;
import com.workflow.exceptions.GenericRuntimeException;
import com.workflow.service.ComponentService;
import com.workflow.service.Helper;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import weka.core.Instance;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class UtilityController {

    @Autowired
    Helper helper;

    @RequestMapping(value="checkConnection",method= RequestMethod.POST)
    public ResponseEntity<ArrayList<String>> checkDatabaseConnection(@RequestBody HashMap data){
        String userName = (String) data.get("name");
        String database = (String) data.get("database");
        String collection = (String) data.get("collection");
        String p = (String) data.get("password");
        char[] password = p.toCharArray();
        String url = (String) data.get("url");
        ArrayList<String> response = new ArrayList<>();
        HashMap<String,Boolean> result = helper.checkMongoConnection(url,userName, password, database,collection);
        if(result.get("Connection")) {
            response.add("Connection Established");
            if(result.get("Collection")) {
                response.add("Collection exists");
            }else {
                response.add("Collection not exists");
            }
            return new ResponseEntity<ArrayList<String>>(response, HttpStatus.OK);
        }else {
            return new ResponseEntity<ArrayList<String>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping(value="/uploadfile", method=RequestMethod.POST)
    public ResponseEntity<HashMap> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("WFId")String WFId, @RequestParam("compId")String CompId){

        HashMap<String,Object> map = new HashMap<>();
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();

                // Creating the directory to store file

                File dir = new File("uploadfiles/"+WFId+"/"+CompId+"/");
                if (!dir.exists())
                    dir.mkdirs();

                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath()
                        + File.separator + file.getOriginalFilename());
                if(!serverFile.exists()) {
                    serverFile.createNewFile();
                }
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                stream.write(bytes);
                stream.close();
                JSONArray headers = helper.getHeaders(serverFile.getAbsolutePath());

                Entity model = helper.fileUploadConfig(WFId,CompId,serverFile.getAbsolutePath(),headers);

                if (model.getEntity().get("error")==null) {
                    model.getEntity().put("message", "File Uploaded successfully");
                    return new ResponseEntity<HashMap>(model.getEntity(), HttpStatus.OK);
                }
                else {
                    throw new GenericRuntimeException(CompId, "Incompatible headers");
                }


            } catch (Exception e) {
                e.printStackTrace();
                throw new GenericRuntimeException(CompId, "File upload exception.");
            }
        } else {
            throw new GenericRuntimeException(CompId, "File upload error at server.");

        }

        //return new ResponseEntity<String>("{\"message\":\"File Uploaded successfully\"}",HttpStatus.OK);
    }

    @RequestMapping(value="/deletefile", method=RequestMethod.POST)
    public ResponseEntity<String> deleteFile(@RequestBody HashMap data){
        File file=new File(data.get("file").toString());
        if(file.delete()) {
            return new ResponseEntity<String>("{\"message\":\"File deleted\"}",HttpStatus.OK);
        }
        throw new GenericRuntimeException("File deletion failed.");
    }

    @RequestMapping(value="/testQuery/{WFId}/{compId}", method=RequestMethod.GET)
    public ResponseEntity<ArrayList<String>> testQuery(@PathVariable("WFId")String WFId, @PathVariable("compId")String CompId) {
        ArrayList<String> ret=new ArrayList<>();
        try {
            ret=helper.testQuery(WFId, CompId);
            if(ret==null)
                ret.add("Invalid request");
        }catch (Exception e){

            throw new GenericRuntimeException(CompId, "Error in processing query");
        }
        return new ResponseEntity<>(ret,HttpStatus.OK);
    }

    @RequestMapping(value="/predict/{WFId}/{compId}", method=RequestMethod.POST)
    public ResponseEntity<HashMap<String,Object>> prediction(@RequestBody ArrayList<HashMap<String,Object>> map,@PathVariable("WFId")String WFId, @PathVariable("compId")String CompId) {
        HashMap<String,Object> predictresult = new HashMap<>();
        try{
            predictresult= helper.predict(map,WFId,CompId);
        }catch (Exception e){
            throw new GenericRuntimeException(CompId,"Error in Predicting Value");
        }
        return new ResponseEntity<>(predictresult,HttpStatus.OK);
    }


}
