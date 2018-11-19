package com.workflow.controller;

import java.io.*;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

	@RequestMapping(value="/uploadfile", method=RequestMethod.POST)
	public ResponseEntity<HashMap> uploadFile(@RequestParam("file") MultipartFile file){
		
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
				BufferedOutputStream stream = new BufferedOutputStream(
						new FileOutputStream(serverFile));
				stream.write(bytes);
				stream.close();
				map.put("message", "File Uploaded successfully");
				map.put("path",serverFile.getAbsolutePath());
				return new ResponseEntity<HashMap>(map,HttpStatus.OK);

				
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


}
