# Workflow Manager 

## Problem Statement 

To build a workflow manager that will help Predii to generate intelligent insights from raw data by creating and executing tasks using Graphical User Interface. Platform should have ability to create, manage and monitor workflows. 

## Objectives 

  - Simplify data integration and handling. 

  - Eradicate the need of manual coding for integration of modules. 

  - Speedup the development process 

 

## Software Requirements 

  - IntelliJ IDEA (with Spring Framework support). 

  - MongoDB 3.2.2

  - Java 1.8 and above 

  - Apache Tomcat server 

## Technologies Used 

### Client Side Technologies

  - Angular JS 1.7.2 
  - Angular Schema-form
  - Go JS 1.8.29


### Server Side Technologies

  - Spring 5
  - Java 8
  - Apache Tomcat 
  - Web Socket 
  
### Database

  - Mongo DB  
 

## Steps to run  

  -  Clone the project from 

https://github.com/vnkdj5/WorkflowManager.git 

  - Import the project in IntelliJ IDEA as maven project. 

  - Set up apache tomcat server configurations. 

  - Set up mongodb database: 
     -  Update MongoDB URI in 'config.properties' file(src/main/resources/config.properties).
     -  Use 'dump.zip' to populate the database.

  - Build and deploy the project. 

  - Run http://localhost:8080/WorkflowManager/ 

  - Create new workflow  

  - Drag and drop the required components 

  - Setup the component configurations (input, valid links, config,output) 

   - Check for the validations of component 

  - Save and run the workflow 
 

## Component Designing Protocols 

  - Create a java class file with component name as class name. 

  - Implement the Component Interface. 

  - Provide definitions to methods:  

  - Init(): Configuration initialization 

  - Process(): provide actual processing of component. 

  - Implement required component specific APIs in Utility. 

  - Update the valid links of added component in "validLinks" collection in database.  

 

 

 

 

 

 

 

 
