//for welcome page
app.filter('beginning_data', function () {
    return function (input, begin) {
        if (input) {
            begin = +begin;
            return input.slice(begin);
        }
        return [];
    }
});

app.controller('DiagramCtrl', ['$scope', '$rootScope', 'fileUpload', 'graphService', 'componentService', '$q', 'notificationService', '$http', '$location', '$timeout', 'growl', '$interval', function ($scope, $rootScope, fileUpload, graphService, componentService, $q, notify, $http, $location, $timeout, growl, $interval) {
    $scope.schema = null;
    $scope.form = [];
    $scope.model = {};


    $scope.workflow = {};
    $scope.json = JSON.stringify($scope.workflow, undefined, 4);
    $scope.content = $scope.json;
    $scope.paletteModel = [];
    $scope.textWFName = "";
    $scope.saveStatus = "";
    $scope.currentWorkflowName = "";
    $scope.selectedComponent = {};

    $scope.validGraphLinks = [];


    //converter for graph to gojs graph
    $scope.converter = function (graph) {
        var graphJson = {
            "class": "go.GraphLinksModel",
            "linkFromPortIdProperty": "fromPort",
            "linkToPortIdProperty": "toPort",
            "name": "",
            "nodeDataArray": [
                {
                    "key": "Start",
                    "category": "Start",
                    "loc": "175 0",
                    "text": "Start"
                },
                {
                    "key": "End",
                    "category": "End",
                    "loc": "175 407",
                    "text": "Stop!"
                }
            ],
            "linkDataArray": []
        };


        graphJson.name = graph.id;
        graphJson.wfid = graph.id;

        for (var i = 0; i < graph.nodes.length; i++) {
            // console.log(graph.nodes[i]);
            var comp = {};
            comp.text = graph.nodes[i].name;
            comp.key = graph.nodes[i].cid;
            comp.category = graph.nodes[i].category;
            comp.loc = "" + graph.nodes[i].x + " " + graph.nodes[i].y + "";


            graphJson.nodeDataArray.push(comp);

        }
        graphJson.linkDataArray = graph.links;


        // console.log(graphJson);
        return graphJson;
    };

    //for welcome page
    graphService.getAllWorkflows().then(function success(response) {
        // console.log(response.data);
        $scope.file = response.data;
        $scope.data_limit = 10;
        $scope.filter_data = $scope.file.length;
        $scope.entire_user = $scope.file.length;

    }, function error(response) {
        notify.showError("Error!!", "" + response.status);
    });

    $scope.filter = function () {
        $timeout(function () {
            $scope.filter_data = $scope.searched.length;
        }, 20);
    };
    $scope.sort_with = function (base) {
        $scope.base = base;
        $scope.reverse = !$scope.reverse;
    };


//	for database connection checking

    $scope.showAlert = function () {
        alert("hello to external scope");
    }
    $scope.testConn = function (form) {
        $scope.$broadcast('schemaFormValidate');
        var testBtn = document.querySelector('.testConBtn');
        if (form.$valid) {
            testBtn.innerHTML = "Checking Connection";
            testBtn.classList.add('spinning');

            graphService.checkDBConnection($scope.model).then(
                function success(response) {

                    notify.showSuccess("Success!", response.data[0]);
                    if (response.data[1] === "Collection exists") {
                        notify.showWarning("Warning!", response.data[1]);
                    } else {
                        notify.showSuccess("Success!", response.data[1]);
                    }
                    testBtn.classList.remove('spinning');
                    testBtn.innerHTML = "Test";
                }, function error(response) {
                    notify.showError("Error in Connection!", response.data);
                    testBtn.classList.remove('spinning');
                    testBtn.innerHTML = "Test";
                }
            );
        }
        /* else
        {
                 testBtn.innerHTML = "Checking Connection Failed";
                 testBtn.classList.add('spinning');
        }
        */
    };

//	for request

    $scope.handlePreviousRequest = function () {
        var url_string = window.location.href;
        var url = new URL(url_string);
        var create = url.searchParams.get("create");
        var load = url.searchParams.get("load");

        $scope.currentWorkflowName = url.searchParams.get("name");

        if (load == 1) {
            $scope.loadWf(url.searchParams.get("name"));

        }

    };


    /*
     * onSubmit() is used for saving the components configurations.
     */
    $scope.onSubmit = function (form) {
        // First we broadcast an event so all fields validate themselves
        $scope.$broadcast('schemaFormValidate');
        let compId = $scope.selectedComponent.key;
        let WFId = $scope.currentWorkflowName;
        $scope.model.WFId = WFId;
        $scope.model.ComponentId = compId;

        // Then we check if the form is valid
        if (form.$valid)    {
            componentService.setConfig(WFId, compId, $scope.model).then(
                function success(response) {
                    notify.showSuccess("Success", response.data.message);
                    if (response.data.config != null || response.data.config != undefined) {
                        $scope.model = response.data.config;
                    }
                },
                function error(response) {
                    notify.showError("Error", response.data.message);
                }
            );
            // $scope.selectedComponent = {};
        } else if (form.$invalid) {
            notify.showError("Error!!", "Invalid config for " + $scope.selectedComponent.category);
        }

        //$scope.selectedComponent = {};
        $scope.myDiagram.isModified = true;
        var button = document.getElementById("SaveButton");
        if (button) button.disabled = false;
        //$("#myModal").modal("hide");   //Hide form when config is saved
    };

//	run the workflow method

    $scope.wfStatus = {percent: 100, status: "Execution Started"};

    const serverUrl = '/WorkflowManager/socket'
    let stompClient;

    $scope.initializeWebSocketConnection = function () {
        let ws = new SockJS(serverUrl);
        stompClient = Stomp.over(ws);
        let self = this;
        $("#statusDiv").innerHTML="";
        stompClient.connect({}, function (frame) {
            stompClient.subscribe("/chat", (message) => {
                if (message.body) {
                    $("#statusDiv").append("<p class='message'>"+message.body+"</p>")
                    notify.showInfo("Executing Workflow", message.body);
                    console.log(message.body);
                }
            });
        });
    };

    function sendMessage() {
        var message = document.getElementById("inputTxt").value;
        stompClient.send("/app/send/message", {}, message);
        $('#input').val('');
    }

    $scope.runWorkflow = function () {
        var WFId = $scope.workflow.name; //Name is also Id
        $scope.initializeWebSocketConnection();
        notify.showInfo("Info:" + WFId, "Workflow checking and execution started.");

        //var progressStatus = document.getElementById("wfstatus");
        //progressStatus.style.display = "block";


        /*

        "<div class=\"progressDiv\" id=\"wfstatus\">\n"+
            "           <div class=\"progress\">\n" +
            "                <div class=\"progress-bar progress-bar-success active\" role=\"progressbar\"\n" +
            "                     aria-valuenow=\"{{wfStatus.percent}}\" aria-valuemin=\"0\" aria-valuemax=\"100\"\n" +
            "                     style=\"width:{{wfStatus.percent}}%\">\n" +
            "                    {{wfStatus.percent}}%\n" +
            "                </div>\n" +
            "            </div>\n" +
            "            <p class=\"text-center\"> {{wfStatus.status}}</p>\n" +
            "        </div>")

     ;*/


        //Start execution of workflow
        graphService.runWorkflow(WFId).then(
            function success(response) {
                //console.log(response.data);
                notify.showSuccess("Success!", "Workflow Execution Finished.");
                if (stompClient !== null) {
                    stompClient.disconnect();
                }

            },
            function error(response) {
                let errors = response.data.cause;
                // console.log("Error DATA", response.data);
                if (stompClient !== null) {
                    stompClient.disconnect();
                }
                try {
                    for (let i = 0; i < errors.length; i++) {
                        notify.showError("Error in Workflow!", errors[i]);
                    }
                } catch (error) {
                    console.log(error);
                }
            });




    };

    //Event for fileupload //Generated by CsvReader
    $rootScope.$on('uploadEvent', function (evt, data) {//Executed only for fileupload event

        $scope.model = data;


        /*var MD = $scope.myDiagram;
        var nodeDataArr = MD.model.nodeDataArray;
        var fileList = nodeDataArr.find(component => component.key == $scope.selectedComponent.key).config;
        if(fileList){
        	fileList=fileList.filePath;
        	if(fileList.length==0){
        		nodeDataArr.find(component => component.key == $scope.selectedComponent.key).output=[];
        	}
        }
        var curHeaders = nodeDataArr.find(component => component.key == $scope.selectedComponent.key).output;
        if(curHeaders && curHeaders.length!=0){
            var newHeaders = data.headers;
        	for(var i=0;i<curHeaders.length;i++){
        		console.log("cur:"+curHeaders[i].fieldName+", new: "+newHeaders[i].fieldName);
        		if(curHeaders[i].fieldName!=(newHeaders[i].fieldName)){
        			//call delete controller and return error msg
        			fileUpload.deleteFile(data.path).then(
        		            function success(response) {
        		                notify.showSuccess("Deleted", "File deleted.");
        		            },
        		            function error(response) {
        		            	notify.showError("Error!", "Cannot delete file");
        		            }
        		        );

        			notify.showError("Error!", "Incompatible headers");
        			return;
        		}
        	}
        	if(curHeaders.length!=newHeaders.length){
        		//call delete controller and return error msg
        		fileUpload.deleteFile(data.path).then(
        				function success(response) {
    		                notify.showSuccess("Deleted", "File deleted.");
    		            },
    		            function error(response) {
    		            	notify.showError("Error!", "Cannot delete file");
    		            }
        	        );

        		notify.showError("Error!", "Incompatible headers");
        		return;
        	}
        	$scope.model.filePath.push(data.path);*/
        /* }
         else{
             $scope.model.filePath=[];
             $scope.model.filePath.push(data.path);
             nodeDataArr.find(component => component.key == $scope.selectedComponent.key).output = data.headers; //Assign CSV Headers for output field of component;
         }*/
    });

    //getter for getting component defination by key
    $scope.getComponent = function (compKey) {
        return $scope.myDiagram.model.nodeDataArray.find(component => component.key == compKey);

    };

//	Workflow create function
    $scope.createWorkflow = function () {
        graphService.newGraph($scope.workflow.name).then(
            function success(response) {
                //alert(response.data)
                notify.showSuccess("Success!", "Workflow created Successfully.");
                graph = response.data.Graph;

                $scope.myDiagram.model = go.Model.fromJson($scope.converter(graph));

                //alert(graph)
                $("#newWorkflowModal").modal("hide");

            }, function error(response) {
                notify.showError("Error!", "Workflow exists with same name.");
            }
        );
    };

    // Show the diagram's model in JSON format that the user may edit
    $scope.save = function () {
        $scope.workflow = $scope.myDiagram.model;
        console.log("in save || testing sockets");
        let socket = new SockJS('workflow-execution-websocket');
        var progressStatus = document.getElementById("wfstatus");
        progressStatus.style.display = "block";
        $scope.stompClient = Stomp.over(socket);
        $scope.stompClient.connect({}, function onConnect(frame) {

                console.log('Connected: ' + frame);
            let subscription = $scope.stompClient.subscribe('/completion/status', function (response) {
                    notify.showInfo(WFId, JSON.parse(response.body).progressStatus);
                });
                console.log("Subscription ID", subscription);
            }, function onError(error) {
                console.log("STOMP connection error", error);
            }
        );
        /*console.log("Saving Workflow : ", $scope.workflow.name);
        if ($scope.workflow.name == "" || $scope.workflow.name == undefined) {
            $("#newWorkflowModal").modal("show");
        } else {
            graph = $scope.myDiagram.model.toJson();//.toJson();
            graphService.saveGraph(graph, $scope.currentWorkflowName).then(
                function success(response) {

                    notify.showSuccess("Success!", response.data.message);
                    $scope.myDiagram.isModified = false;
                    var button = document.getElementById("SaveButton");
                    if (button) button.disabled = !$scope.myDiagram.isModified;
                    var idx = document.title.indexOf("*");
                    if ($scope.myDiagram.isModified) {
                        if (idx < 0) document.title += "*";
                    } else {
                        if (idx >= 0) document.title = document.title.substr(0, idx);
                    }
                },
                function error(response) {
                    notify.showError("Error!!", response.statusCode + " : " + response.status);
                }
            );

        }*/

    };
    $scope.loadWorkflow = function () {

        //alert($scope.workflow.name);

        graphService.loadGraph($scope.currentWorkflowName).then(
            function success(response) {
                var graph = response.data.Graph;
                $scope.textWFName = graph.wfname;
                $scope.saveStatus = " (Autosaved)";
                graph = $scope.converter(graph);
                // console.log(JSON.stringify(graph));
                $scope.myDiagram.model = go.Model.fromJson(graph);
                //console.log("Diagram: ",$scope.myDiagram);
                //console.log($scope.myDiagram.model.toJson());  //Check graph configuration
                //component= $scope.myDiagram.model.nodeDataArray.find(component => component.text!=null);
                //console.log(component);
                notify.showSuccess("Success!", "Workflow loaded Successfully.");


            },
            function error(response) {
                notify.showError("Error!", "Loading Workflow!!");
            }
        );

    };

    //---- Function for loading workflow specified in URL(from welcomepage)----

    // load workflow
    $scope.loadWf = function (name) {

        $scope.workflow.name = name;
        $scope.loadWorkflow();
    };

    //delete workflow
    $scope.deleteWf = function (index, name) {
        graphService.deleteWorkflow(name).then(
            function success(response) {
                $scope.file.splice(index, 1);
                notify.showSuccess("Success!", "Workflow deleted Successfully.");
            }, function error(response) {
                notify.showError("Error while deleting workflow!");
            }
        );
    };


    $scope.loadComponents = function () {
        let result = [];
        componentService.getAll().then(
            function success(response) {

                $scope.palleteModel = response.data.pallete;
                //alert(JSON.stringify($scope.paletteModel));
                result = response.data.pallete;
                for (let i in result) {
                    if (!(result[i].category == "Start" || result[i].category == "End")) {
                        $scope.addNodeToPalette(result[i].category);
                    }
                }
                $scope.myPalette.model.nodeDataArray = result;

            },
            function error(response) {
                notify.showError("Error!", "Cannot Load Components!");

            });
        graphService.getValidLinks().then(
            function success(response) {
                $scope.validGraphLinks = response.data;
                //notify.showInfo("Info!", "valid Links Loaded")

            },
            function error(response) {

                notify.showError("Error!", response.cause);
            });

    };
    /*
    Function for handling mapper component (called from JSON Schema Form)
     */
    $scope.mapperHandler = function () {

        let fieldArray = $scope.model.field;// JSON.parse(JSON.stringify($scope.model.field));
        if ($scope.model.outputFields == null)
            $scope.model.outputFields = [];
        let outputFieldArray = $scope.model.outputFields;
        //loop for keeping previous and currently selected fields are in outputFields
        for (let i in fieldArray) {

            for (let j in outputFieldArray) {
                if (fieldArray[i].fieldName == outputFieldArray[j].fieldName && fieldArray[i].check == false) {
                    outputFieldArray.splice(j, 1);
                    break;
                }
            }
        }
        //console.log("output first array: ", JSON.stringify(outputFieldArray));
        for (let i in fieldArray) {
            if (fieldArray[i].check === true) {
                let flag = false;
                for (let j in outputFieldArray) {
                    if (fieldArray[i].fieldName == outputFieldArray[j].fieldName) {
                        flag = true; //flag indicates field that is already in outputfields
                        break;
                    }
                }
                if (flag == false) {
                    let obj = {
                        "fieldName": fieldArray[i].fieldName,
                        "newFieldName": fieldArray[i].fieldName,
                        "dataType": fieldArray[i].dataType
                    };
                    $scope.model.outputFields.push(obj);
                }
            }
        }
        //console.log("output second array: ", JSON.stringify(outputFieldArray));

        // $scope.model1=$scope.model;
    };

    $scope.mapperAddAllHandler = function () {
        let fieldArray = $scope.model.field;// JSON.parse(JSON.stringify($scope.model.field));
        if ($scope.model.outputFields == null)
            $scope.model.outputFields = [];
        let outputFieldArray = $scope.model.outputFields;
        //loop for keeping previous and currently selected fields are in outputFields

        //console.log("output first array: ", JSON.stringify(outputFieldArray));
        for (let i in fieldArray) {
            fieldArray[i].check = true;
            let flag = false;
            for (let j in outputFieldArray) {
                if (fieldArray[i].fieldName == outputFieldArray[j].fieldName) {
                    flag = true; //flag indicates field that is already in outputfields
                    break;
                }
            }
            if (flag == false) {
                let obj = {
                    "fieldName": fieldArray[i].fieldName,
                    "newFieldName": fieldArray[i].fieldName,
                    "dataType": fieldArray[i].dataType
                };
                $scope.model.outputFields.push(obj);
            }

        }
    };
    //Method for updating configuration of each component
    $scope.loadForm = function (compCategory, componentKey) {
        $scope.model = {};
        let WFId = $scope.currentWorkflowName;

        //Get configuration of components
        componentService.getConfig(WFId, componentKey).then(
            function success(response) {
                $scope.model = {};
                let curForm = JSON.parse(response.data.FORM,function (key, value) {
                    if (value && (typeof value === 'string') && value.indexOf("function") === 0) {
                        // we can only pass a function as string in JSON ==> doing a real function
                        var jsFunc = new Function('return ' + value)();
                        return jsFunc;

                    }

                    return value;
                });//JSON.parse(response.data.FORM);

                for (let i in curForm.form) {//For assigning WFId and compId (for specializing API)
                    if (curForm.form[i].endpoint) {
                        curForm.form[i].endpoint += "?WFId=" + WFId + "&compId=" + componentKey;
                    }
                }
                // console.log(curForm);
                //console.log("STRINGIFY", JSON.stringify(curForm));
                $scope.schema = curForm.schema;
                $scope.form = curForm.form;
                $scope.model = response.data.MODEL;
                if (!$scope.model || $scope.model == null) {
                    $scope.model = {};
                }

                $('#myModal').modal('show');
            },
            function error(response) {
                //Add notification show "Component not found"
                $scope.schema = null;
                $scope.form = null;
                // $scope.form.model = null;
                notify.showError("Error!", response.data.message);

            });
    };

    $scope.loadComponents(); //loading pallete content



    /*******************************************************************
     *        Context Menu Handler
     *******************************************************************/

    // This is the general menu command handler, parameterized by the name of the command.
    $scope.cxcommand = function ($event, val) {
        if (val === undefined) val = $event.currentTarget.id;
        var diagram = $scope.myDiagram;
        switch (val) {
            /*		      case "cut": diagram.commandHandler.cutSelection(); break;
                  case "copy": diagram.commandHandler.copySelection(); break;
                  case "paste": diagram.commandHandler.pasteSelection(diagram.lastInput.documentPoint); break;
             */
            case "delete":
                diagram.commandHandler.deleteSelection();
                break;
            case "componentConfig":
                $scope.loadForm($scope.selectedComponent.category, $scope.selectedComponent.key);
                break;
            case "componentOutput":
                $scope.loadInputOutput($scope.selectedComponent.category, $scope.selectedComponent.key, false);

                break;
            case "componentInput":
                $scope.loadInputOutput($scope.selectedComponent.category, $scope.selectedComponent.key, true);
                break;
            /*case "color": {
              var color = window.getComputedStyle(document.elementFromPoint(event.clientX, event.clientY).parentElement)['background-color'];
              changeColor(diagram, color); break;
          }*/
        }
        diagram.currentTool.stopTool();
    };


    /*------------------------------------------------------------------------
     * INIT FUNCTION WHICH Setups palate and handles all events related to workflow
     */
    $scope.init = function () {
        //if (window.goSamples) goSamples();  // init for these samples -- you don't need to call this
        var GO = go.GraphObject.make;  // for conciseness in defining templates

        $scope.myDiagram =
            GO(go.Diagram, "myDiagramDiv",  // must name or refer to the DIV HTML element
                {
                    initialContentAlignment: go.Spot.Center,
                    allowDrop: true,  // must be true to accept drops from the Palette
                    "LinkDrawn": showLinkLabel,  // this DiagramEvent listener is defined below
                    "LinkRelinked": showLinkLabel,
                    scrollsPageOnFocus: false,
                    "undoManager.isEnabled": true  // enable undo & redo
                });

//		----------- Context Menu Initialization Start ----------------------------------------
        var cxElement = document.getElementById("contextMenu");

        // Since we have only one main element, we don't have to declare a hide method,
        // we can set mainElement and GoJS will hide it automatically
        var myContextMenu = GO(go.HTMLInfo, {
            show: showContextMenu,
            mainElement: cxElement
        });

        $scope.myDiagram.contextMenu = myContextMenu;
        // We don't want the div acting as a context menu to have a (browser) context menu!
        cxElement.addEventListener("contextmenu", function (e) {

            e.preventDefault();
            return false;
        }, false);

        function hideContextMenu(obj, diagram, tool) {
            cxElement.style.display = "block";

        }
        function showContextMenu(obj, diagram, tool) {
            // Show only the relevant buttons given the current state.
            var cmd = diagram.commandHandler;
            /*
                document.getElementById("cut").style.display = cmd.canCutSelection() ? "block" : "none";
                document.getElementById("copy").style.display = cmd.canCopySelection() ? "block" : "none";
                document.getElementById("paste").style.display = cmd.canPasteSelection() ? "block" : "none";
             */
            document.getElementById("delete").style.display = (obj !== null && cmd.canDeleteSelection()) ? "block" : "none";
            document.getElementById("componentConfig").style.display = (obj !== null ? "block" : "none");
            document.getElementById("componentInput").style.display = (obj !== null ? "block" : "none");
            document.getElementById("componentOutput").style.display = (obj !== null ? "block" : "none");
            if (obj instanceof go.Node) {
                //Setting selected component for further processing
                $scope.selectedComponent.category = obj.data.category;
                $scope.selectedComponent.key = obj.data.key;

            }
            if (diagram instanceof go.Palette)      //don't show context menu for palette elements
                return;
            // Now show the ,whole context menu element
            cxElement.style.display = "block";
            // we don't bother overriding positionContextMenu, we just do it here:
            var mousePt = diagram.lastInput.viewPoint;
            cxElement.style.left = mousePt.x + "px";
            cxElement.style.top = mousePt.y + "px";
        }

//		--		  Context  Menu Initialization End -----------------------------//


        //Function to remove Invalid link added by user
        $scope.undoLink = function (e) {
            if (e.modelChange == "linkDataArray" && e.newValue != null) {
                let MD = $scope.myDiagram, linkDataArr = MD.model.linkDataArray;
                let linkFrom = e.newValue.from;
                let linkTo = e.newValue.to;
                let componentFrom = MD.model.nodeDataArray.find(component => component.key == linkFrom).category;
                let componentTo = MD.model.nodeDataArray.find(component => component.key == linkTo).category;
                let validLink = $scope.validGraphLinks.find(link => link.from == componentFrom && link.to == componentTo);
                let index = linkDataArr.length - 1;
                let duplicateLink = linkDataArr.indexOf(linkDataArr.find(link => link.to == linkTo && link.from == linkFrom));
                let inputLink = linkDataArr.find(link => link.to == linkTo).from;
                let outputLink = linkDataArr.find(link => link.from == linkFrom).to;

                //console.log("i:"+inputLink+"o:"+outputLink+"d:"+duplicateLink);

                if (!validLink || inputLink != linkFrom || outputLink != linkTo || duplicateLink != index) {
                    MD.model.removeLinkData(e.newValue);
                    notify.showError("Error!", "Invalid link");
                }
            }
        };

        var reqData = [{}];
        var oldNodeDataArr = [];

        $scope.myDiagram.addModelChangedListener(function (e) {
                // var cmdhandler= MD.commandHandler;
            // console.log("E",e);
            let WFName = $scope.currentWorkflowName;
            $scope.undoLink(e); //method to remove invalid links


            if (e.change === go.ChangedEvent.Insert) {
                if (e.modelChange == "nodeDataArray") {

                    reqData[0].type = "nodeAdd";
                    reqData[0].CId = e.newValue.key;
                    reqData[0].name = e.newValue.text;
                    reqData[0].category = e.newValue.category;

                } else if (e.modelChange == "linkDataArray") {
                    reqData[0].type = "linkAdd";
                    reqData[0].to = e.newValue.to;
                    reqData[0].from = e.newValue.from;


                    }
                } else if (e.change === go.ChangedEvent.Remove) {
                if (e.modelChange == "nodeDataArray") {

                    reqData[0].type = "nodeDelete";
                    reqData[0].CId = e.oldValue.key;
                    reqData[0].name = e.oldValue.text;
                    reqData[0].category = e.oldValue.category;
                    let coordinates = e.oldValue.loc.split(" ");
                    reqData[0].x = coordinates[0];
                    reqData[0].y = coordinates[1];

                } else if (e.modelChange == "linkDataArray") {
                    reqData[0].type = "linkDelete";
                    reqData[0].to = e.oldValue.to;
                    reqData[0].from = e.oldValue.from;
                }
            }


            if (e.propertyName == "loc") {
                let coordinates = e.newValue.split(" ");
                reqData[0].x = coordinates[0];
                reqData[0].y = coordinates[1];
            }
            if (e.Os == "Drag" && e.propertyName == "StartedTransaction") {
                //console.log("Node Data Array CT: ", JSON.stringify($scope.myDiagram.model.nodeDataArray));
                //Making copy of NodeDataArray before drag event
                oldNodeDataArr = JSON.parse(JSON.stringify($scope.myDiagram.model.nodeDataArray));
            }

            if (e.Os == "Move" && e.propertyName == "CommittedTransaction") {
                let ndArr = $scope.myDiagram.model.nodeDataArray;

                reqData[0].type = "coordinateUpdate";

                let index = 0;
                for (let i in ndArr) {
                    if (ndArr[i].loc != oldNodeDataArr[i].loc) {

                        let coordinates = ndArr[i].loc.split(" ");


                        reqData[index] = {
                            "CId": ndArr[i].key,
                            "x": coordinates[0],
                            "y": coordinates[1],
                            "type": "coordinateUpdate"
                        };
                        index++;
                    }
                }

            }

            if (e.change == go.ChangedEvent.Transaction && e.propertyName == "CommittedTransaction" && e.Os != "Initial Layout") {
                //  console.log("reqData", JSON.stringify(reqData) + " e.Os ==> " + JSON.stringify(e.Os));
                graphService.save(WFName, reqData);
                reqData = [{}];
            }
            // console.log("Property Name", e.propertyName + " e.Os " + e.Os + " event ==>" + e.change);

            let button = document.getElementById("SaveButton");
            if (button) button.disabled = !$scope.myDiagram.isModified;
            }
        );

        // when the document is modified, add a "*" to the title and enable the "Save" button
        $scope.myDiagram.addDiagramListener("Modified", function (e) {
            var button = document.getElementById("SaveButton");
            if (button) button.disabled = !$scope.myDiagram.isModified;
            var idx = document.title.indexOf("*");
            if ($scope.myDiagram.isModified) {
                if (idx < 0) document.title += "*";
            } else {
                if (idx >= 0) document.title = document.title.substr(0, idx);
            }

        });

        //Listener when diagram component is changed
        $scope.myDiagram.addDiagramListener("ObjectDoubleClicked", function (e) {
                let part = e.subject.part;

            let compCategory;
                if (!(part instanceof go.Link) && !(part.data.category === "Start" || part.data.category == "End")) {
                    // console.log(JSON.stringify(part.data.formData));
                    compCategory = part.data.category;

                    $scope.selectedComponent.category = compCategory;
                    $scope.selectedComponent.key = part.data.key;
                    $scope.loadForm(compCategory, part.data.key);
                }
            }
        );


        // helper definitions for node templates

        function nodeStyle() {
            return [
                // The Node.location comes from the "loc" property of the node data,
                // converted by the Point.parse static method.
                // If the Node.location is changed, it updates the "loc" property of the node data,
                // converting back using the Point.stringify static method.
                new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.stringify),
                {
                    // the Node.location is at the center of each node
                    locationSpot: go.Spot.Center
                }
            ];
        }

        // Define a function for creating a "port" that is normally transparent.
        // The "name" is used as the GraphObject.portId,
        // the "align" is used to determine where to position the port relative to the body of the node,
        // the "spot" is used to control how links connect with the port and whether the port
        // stretches along the side of the node,
        // and the boolean "output" and "input" arguments control whether the user can draw links from or to the port.
        function makePort(name, align, spot, output, input) {
            var horizontal = align.equals(go.Spot.Top) || align.equals(go.Spot.Bottom);
            // the port is basically just a transparent rectangle that stretches along the side of the node,
            // and becomes colored when the mouse passes over it
            return GO(go.Shape,
                {
                    fill: "transparent",  // changed to a color in the mouseEnter event handler
                    strokeWidth: 0,  // no stroke
                    width: horizontal ? NaN : 8,  // if not stretching horizontally, just 8 wide
                    height: !horizontal ? NaN : 8,  // if not stretching vertically, just 8 tall
                    alignment: align,  // align the port on the main Shape
                    stretch: (horizontal ? go.GraphObject.Horizontal : go.GraphObject.Vertical),
                    portId: name,  // declare this object to be a "port"
                    fromSpot: spot,  // declare where links may connect at this port
                    fromLinkable: output,  // declare whether the user may draw links from here
                    toSpot: spot,  // declare where links may connect at this port
                    toLinkable: input,  // declare whether the user may draw links to here
                    cursor: "pointer",  // show a different cursor to indicate potential link point
                    mouseEnter: function (e, port) {  // the PORT argument will be this Shape
                        if (!e.diagram.isReadOnly) port.fill = "rgba(255,0,255,0.5)";
                    },
                    mouseLeave: function (e, port) {
                        port.fill = "transparent";
                    }
                });
        }

        function textStyle() {
            return {
                font: "bold 11pt Helvetica, Arial, sans-serif",
                stroke: "whitesmoke"
            }
        }

        // define the Node templates for regular nodes

        $scope.addNodeToPalette = function (nodeName) {

            $scope.myDiagram.nodeTemplateMap.add(nodeName,  // the default category
                GO(go.Node, "Table", nodeStyle(),
                    // the main object is a Panel that surrounds a TextBlock with a rectangular Shape
                    GO(go.Panel, "Auto",
                        GO(go.Shape, "Rectangle",
                            {fill: "#00A9C9", strokeWidth: 0},
                            new go.Binding("figure", "figure")),
                        GO(go.TextBlock, textStyle(),
                            {
                                margin: 8,
                                maxSize: new go.Size(160, NaN),
                                wrap: go.TextBlock.WrapFit,
                                editable: false
                            },
                            new go.Binding("text", "key").makeTwoWay())
                    ),
                    {
                        contextMenu: myContextMenu
                    },
                    // four named ports, one on each side:
                    makePort("T", go.Spot.Top, go.Spot.TopSide, false, true),
                    makePort("L", go.Spot.Left, go.Spot.LeftSide, true, true),
                    makePort("R", go.Spot.Right, go.Spot.RightSide, true, true),
                    makePort("B", go.Spot.Bottom, go.Spot.BottomSide, true, false)
                ));
        };
        $scope.myDiagram.nodeTemplateMap.add("Start",
            GO(go.Node, "Table", nodeStyle(),
                GO(go.Panel, "Auto",
                    GO(go.Shape, "Circle",
                        {minSize: new go.Size(40, 40), fill: "#79C900", strokeWidth: 0}),
                    GO(go.TextBlock, "Start", textStyle(),
                        new go.Binding("text", "key"))
                ),
                // three named ports, one on each side except the top, all output only:
                makePort("L", go.Spot.Left, go.Spot.Left, true, false),
                makePort("R", go.Spot.Right, go.Spot.Right, true, false),
                makePort("B", go.Spot.Bottom, go.Spot.Bottom, true, false)
            ));

        $scope.myDiagram.nodeTemplateMap.add("End",
            GO(go.Node, "Table", nodeStyle(),
                GO(go.Panel, "Auto",
                    GO(go.Shape, "Circle",
                        {minSize: new go.Size(40, 40), fill: "#DC3C00", strokeWidth: 0}),
                    GO(go.TextBlock, "End", textStyle(),
                        new go.Binding("text", "key"))
                ),
                // three named ports, one on each side except the bottom, all input only:
                makePort("T", go.Spot.Top, go.Spot.Top, false, true),
                makePort("L", go.Spot.Left, go.Spot.Left, false, true),
                makePort("R", go.Spot.Right, go.Spot.Right, false, true)
            ));

        // replace the default Link template in the linkTemplateMap
        $scope.myDiagram.linkTemplate =
            GO(go.Link,  // the whole link panel
                {
                    routing: go.Link.AvoidsNodes,
                    curve: go.Link.JumpOver,
                    corner: 5, toShortLength: 4,
                    relinkableFrom: true,
                    relinkableTo: true,
                    reshapable: true,
                    resegmentable: true,
                    // mouse-overs subtly highlight links:
                    mouseEnter: function (e, link) {
                        link.findObject("HIGHLIGHT").stroke = "rgba(30,144,255,0.2)";
                    },
                    mouseLeave: function (e, link) {
                        link.findObject("HIGHLIGHT").stroke = "transparent";
                    }
                },
                new go.Binding("points").makeTwoWay(),
                GO(go.Shape,  // the highlight shape, normally transparent
                    {isPanelMain: true, strokeWidth: 8, stroke: "transparent", name: "HIGHLIGHT"}),
                GO(go.Shape,  // the link path shape
                    {isPanelMain: true, stroke: "gray", strokeWidth: 2}),
                GO(go.Shape,  // the arrowhead
                    {toArrow: "standard", strokeWidth: 0, fill: "gray"}),
                GO(go.Panel, "Auto",  // the link label, normally not visible
                    {visible: false, name: "LABEL", segmentIndex: 2, segmentFraction: 0.5},
                    new go.Binding("visible", "visible").makeTwoWay(),
                    GO(go.Shape, "RoundedRectangle",  // the label shape
                        {fill: "#F8F8F8", strokeWidth: 0}),
                    GO(go.TextBlock, "Yes",  // the label
                        {
                            textAlign: "center",
                            font: "10pt helvetica, arial, sans-serif",
                            stroke: "#333333",
                            editable: true
                        },
                        new go.Binding("text", "text" + "key").makeTwoWay())
                )
            );

        // Make link labels visible if coming out of a "conditional" node.
        // This listener is called by the "LinkDrawn" and "LinkRelinked" DiagramEvents.
        function showLinkLabel(e) {
            var label = e.subject.findObject("LABEL");
            if (label !== null) label.visible = (e.subject.fromNode.data.figure === "Diamond");
        }

        // temporary links used by LinkingTool and RelinkingTool are also orthogonal:
        $scope.myDiagram.toolManager.linkingTool.temporaryLink.routing = go.Link.Orthogonal;
        $scope.myDiagram.toolManager.relinkingTool.temporaryLink.routing = go.Link.Orthogonal;

        //$scope.loadWorkflow();  // load an initial diagram from some JSON text

        // Make sure the pipes are ordered by their key in the palette inventory
        function keyCompare(a, b) {
            var at = a.data.key;
            var bt = b.data.key;
            //update logic when categorizing components
            if (at < bt) return 1;
            if (at > bt) return 1;
            return 0;
        }

        // initialize the Palette that is on the left side of the page
        $scope.myPalette =
            GO(go.Palette, "myPaletteDiv",  // must name or refer to the DIV HTML element
                {
                    scrollsPageOnFocus: false,
                    contentAlignment: go.Spot.Top,
                    nodeTemplateMap: $scope.myDiagram.nodeTemplateMap,  // share the templates used by $scope.myDiagram
                    layout: GO(go.GridLayout, {
                            cellSize: new go.Size(1, 1),
                            wrappingColumn: 1, comparer: keyCompare
                        }),
                });

        $scope.handlePreviousRequest();

    }; // end init

    $scope.getOutput = function (WFID, compId) {
        componentService.getOutput(WFID, compId).then(
            function success(response) {
                return response.data;

            },
            function onError(error) {
                notify.showError("Error", error.data.message);
                return null;
            }
        )
    }
    $scope.inputOrOutput = "";
    // Start Dynamic Table generation for INPUT/OUTPUT Fields
    $scope.loadInputOutput = function (componentName, componentKey, isInput) {
        // EXTRACT VALUE FOR HTML HEADER.
        // ('Book ID', 'Book Name', 'Category' and 'Price')
        var divContainer = document.getElementById("showData");
        divContainer.innerHTML = "";
        let WFid = $scope.currentWorkflowName;
        var jsonData = [];


        // CREATE DYNAMIC TABLE from JSON.
        var table = document.createElement("table");
        table.setAttribute("class", "table  table-hover");

        // CREATE HTML TABLE HEADER ROW USING THE EXTRACTED HEADERS ABOVE.
        var header = table.createTHead();
        header.setAttribute("class", "thead-light");


        if (isInput) {
            $scope.inputOrOutput = $scope.selectedComponent.key + "'s Input";
            // jsonData = component.input; //replace this part with api services
            componentService.getInput(WFid, componentKey).then(
                function success(response) { //response should be jsonArray
                    jsonData = response.data;
                    if (!jsonData || jsonData.length == 0) {
                        if (isInput) {
                            divContainer.innerHTML = "No Input available for this component.";
                        } else {
                            divContainer.innerHTML = "No Output available for this component.";
                        }
                        $('#inputModal').modal('show');
                        return;
                    }
                    var col = [];
                    for (var i = 0; i < jsonData.length; i++) {
                        for (var key in jsonData[i]) {
                            if (col.indexOf(key) === -1) {
                                col.push(key);
                            }
                        }
                    }


                    var tr = header.insertRow(-1);                   // TABLE ROW.

                    for (var i = 0; i < col.length; i++) {
                        var th = document.createElement("th");      // TABLE HEADER.
                        th.innerHTML = col[i];
                        header.appendChild(th);
                    }

                    // ADD JSON DATA TO THE TABLE AS ROWS.
                    for (var i = 0; i < jsonData.length; i++) {

                        tr = table.insertRow(-1);

                        for (var j = 0; j < col.length; j++) {
                            var tabCell = tr.insertCell(-1);
                            tabCell.innerHTML = jsonData[i][col[j]];
                        }
                    }

                    // FINALLY ADD THE NEWLY CREATED TABLE WITH JSON DATA TO A CONTAINER.

                    divContainer.appendChild(table);

                    $('#inputModal').modal('show');
                },
                function error(response) {
                    notify.showError("Error!!", response.data.message);
                }
            );
        } else {
            $scope.inputOrOutput = $scope.selectedComponent.key + "'s Output";

            // jsonData = component.output;

            componentService.getOutput(WFid, componentKey).then(
                function success(response) {
                    jsonData = response.data;


                    if (!jsonData || jsonData.length == 0) {
                        if (isInput) {
                            divContainer.innerHTML = "No Input available for this component.";
                        } else {
                            divContainer.innerHTML = "No Output available for this component.";
                        }
                        $('#inputModal').modal('show');
                        return;
                    }
                    //table logic
                    var col = [];
                    for (var i = 0; i < jsonData.length; i++) {
                        for (var key in jsonData[i]) {
                            if (col.indexOf(key) === -1) {
                                col.push(key);
                            }
                        }
                    }


                    var tr = header.insertRow(-1);                   // TABLE ROW.

                    for (var i = 0; i < col.length; i++) {
                        var th = document.createElement("th");      // TABLE HEADER.
                        th.innerHTML = col[i];
                        header.appendChild(th);
                    }

                    // ADD JSON DATA TO THE TABLE AS ROWS.
                    for (var i = 0; i < jsonData.length; i++) {

                        tr = table.insertRow(-1);

                        for (var j = 0; j < col.length; j++) {
                            var tabCell = tr.insertCell(-1);
                            tabCell.innerHTML = jsonData[i][col[j]];
                        }
                    }

                    // FINALLY ADD THE NEWLY CREATED TABLE WITH JSON DATA TO A CONTAINER.

                    divContainer.appendChild(table);

                    $('#inputModal').modal('show');
                },
                function error(response) {
                    notify.showError("Error!!", response.data.message);
                }
            )
        }


    };
//End of Dynamic Table  generation for Input/Output of components

    //Watch for checking internet connection
    $scope.$watch('online', function (newStatus) {

        if (newStatus)
            notify.showSuccess("Connected!", "You are online.");
        else
            notify.showError("Not connected!", "You are offline.");
    });

}]);  //Diagram controller end
