package com.workflow.controller;
import com.workflow.component.Entity;
import com.workflow.service.ComponentService;
import com.workflow.service.Helper;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

	@Autowired
	Helper helper;

	@Autowired
	ComponentService componentService;

	@RequestMapping(value="/uploadfile", method=RequestMethod.POST)
	public ResponseEntity<HashMap> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("WFId")String WFId, @RequestParam("compId")String CompId){
		
		HashMap<String,Object> map = new HashMap<>();
		if (!file.isEmpty()) {
			try {
				byte[] bytes = file.getBytes();

				// Creating the directory to store file
				
				File dir = new File("uploadfiles/");
				if (!dir.exists())
					dir.mkdirs();

				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath()
						+ File.separator + file.getOriginalFilename());
				if(!serverFile.exists()) {
					serverFile.createNewFile();
				}
				System.out.println(serverFile);
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				List<Object> headers = helper.getHeaders(serverFile.getAbsolutePath()).toList();
				map.put("headers", helper.getHeaders(serverFile.getAbsolutePath()).toList());
				map.put("message", "File Uploaded successfully");
				map.put("path",serverFile.getAbsolutePath());

				Entity model = componentService.fileUploadConfig(WFId,CompId,map.get("path").toString(),headers);


				return new ResponseEntity<HashMap>(model.getEntity(),HttpStatus.OK);

				
			} catch (Exception e) {
				e.printStackTrace();
				map.put("message", "File Uploade exception");
				return new ResponseEntity<HashMap>(map,HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			//return "You failed to upload " + name
			//		+ " because the file was empty.";
			map.put("message", "File Uploade error");
			return new ResponseEntity<HashMap>(map,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		//return new ResponseEntity<String>("{\"message\":\"File Uploaded successfully\"}",HttpStatus.OK);
	}

	@RequestMapping(value="/deletefile", method=RequestMethod.POST)
	public ResponseEntity<String> deleteFile(@RequestBody HashMap data){
		File file=new File(data.get("file").toString());
		System.out.println("del path:"+data.get("file").toString());
		if(file.delete()) {
			return new ResponseEntity<String>("{\"message\":\"File deleted\"}",HttpStatus.OK);
		}
		return new ResponseEntity<String>("{\"message\":\"Cannot delete file\"}",HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
