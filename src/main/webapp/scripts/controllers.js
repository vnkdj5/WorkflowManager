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

app.controller('DiagramCtrl', ['$scope', '$rootScope', 'fileUpload', 'graphService', 'componentService', '$q', 'notificationService', '$timeout', function ($scope, $rootScope, fileUpload, graphService, componentService, $q, notify, $http, $location, $timeout) {
    $scope.schema = null;
    $scope.form = [];
    $scope.model = {};


    $scope.workflow = {};
    $scope.json = JSON.stringify($scope.workflow, undefined, 4);
    $scope.content = $scope.json;
    $scope.palleteModel = [];
    $scope.currentWorkflowName = "";
    $scope.selectedComponent = {};


    $scope.workflow = {};
    $scope.json = JSON.stringify($scope.workflow, undefined, 4);
    $scope.content = $scope.json;
    $scope.palleteModel = [];
    $scope.currentWorkflowName = "";
    $scope.validGraphLinks = [];


    //for welcome page
    graphService.getAllWorkflows().then(function success(response) {
        console.log(response.data);
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

    $scope.testConn = function (form) {
        $scope.$broadcast('schemaFormValidate');

        if (form.$valid) {
            graphService.checkDBConnection($scope.model).then(
                function success(response) {
                    notify.showSuccess("Success!", response.data.message);
                }, function error(response) {
                    notify.showError("Error in Connection!", response.data.message);
                }
            );
        }

    };

//	for request

    $scope.handlePreviousRequest = function () {
        let url_string = window.location.href;
        let url = new URL(url_string);
        let create = url.searchParams.get("create");
        let load = url.searchParams.get("load");

        if (load == 1) {
            $scope.loadWf(url.searchParams.get("name"));
        } else if (create == 1) {
            console.log($scope.myDiagram.model.nodeDataArray.length > 0 && $scope.myDiagram.model.linkDataArray.length > 0);
            $("#newWorkflowModal").modal("show");
        }


    };


    /*
     * onSubmit() is used for saving the components configurations.
     */
    $scope.onSubmit = function (form) {
        // First we broadcast an event so all fields validate themselves
        $scope.$broadcast('schemaFormValidate');
        let nodeDataArr = $scope.myDiagram.model.nodeDataArray;
        let searchedComponent = nodeDataArr.find(component => component.category == $scope.selectedComponent.category);
        if (searchedComponent.config != $scope.model) {
            searchedComponent.isModified = true;
        }

        searchedComponent.isModified = true;
        // Then we check if the form is valid
        if (form.$valid) {
            if ($scope.selectedComponent.category == "Mapper") //TODO: Add separate attribute for finding components having no Config
            {
                searchedComponent.config = $scope.model;
                //Now update checked fields into output of mapper
                searchedComponent.output = [];
                for (let i = 0; i < searchedComponent.config.field.length; i++) {
                    if (searchedComponent.config.field[i].check) {
                        searchedComponent.output.push(
                            {
                                "fieldName": searchedComponent.config.field[i].fieldName,
                                "dataType": searchedComponent.config.field[i].dataType
                            });
                    }
                }
            } else {
                searchedComponent.config = $scope.model;

            }
            searchedComponent.valid = true;

            $scope.selectedComponent = {};
            console.log(JSON.stringify(nodeDataArr));

        } else if (form.$invalid) {

            searchedComponent.valid = false;
            notify.showError("Error!!", "Invalid config for " + $scope.selectedComponent.category);
        }
        //Need to improve logic here
        //$scope.myDiagram.model.nodeDataArray.find(component => component.category!=null && component.category!=undefined && component.category=="CsvReader" && component.config!=null).valid=true;

        $scope.selectedComponent = {};
        $scope.myDiagram.isModified = true;
        $("#myModal").modal("hide");
    };
//	run the workflow method
    $scope.runWorkflow = function () {
        let name = $scope.workflow.name;
        //console.log("name   "+ name);
        notify.showInfo("Info:" + name, "Workflow checking and execution started.");


        graphService.runWorkflow(name).then(
            function success(response) {
                //console.log(response.data);
                notify.showSuccess("Success!", "Workflow Execution Finished.");

            },
            function error(response) {
                let errors = response.data.cause;
                for (let i = 0; i < errors.length; i++) {
                    notify.showError("Error in Workflow!", errors[i]);
                }
            }
        );

    };
    //Event for fileupload //Generated by CsvReader
    $rootScope.$on('uploadEvent', function (evt, data) {//Executed only for fileupload event
        $scope.model.filePath.push(data.path);
        //console.log("Current Component",$scope.selectedComponent);

        let MD = $scope.myDiagram;
        let nodeDataArr = MD.model.nodeDataArray;
        nodeDataArr.find(component => component.key == $scope.selectedComponent.key).output = data.headers; //Assign CSV Headers for output field of component;
    });


    $scope.getComponent = function (componentName) {
        //console.log($scope.myDiagram.model.nodeDataArray.find(component => component.text==componentName));
        return $scope.myDiagram.model.nodeDataArray.find(component => component.text == componentName);

    };
//	Workflow create function
    $scope.createWorkflow = function () {
        graphService.newGraph($scope.workflow.name).then(
            function success(response) {
                //alert(response.data)
                notify.showSuccess("Success!", "Workflow created Successfully.");
                graph = response.data.Graph.jgraph;

                if ($scope.myDiagram.model.nodeDataArray.length > 0 || $scope.myDiagram.model.linkDataArray.length > 0) {
                    $scope.save();
                } else {
                    $scope.myDiagram.model = go.Model.fromJson(graph);
                }


                $scope.currentWorkflowName = response.data.Graph.name;


                //alert(graph)
                $("#newWorkflowModal").modal("hide");

            }, function error(response) {
                notify.showError("Error!", "Workflow exists with same name.");
            }
        );
        //$scope.myDiagram.model = graphService.loadGraph(name)
    };
    // Show the diagram's model in JSON format that the user may edit
    $scope.save = function () {
        $scope.workflow = $scope.myDiagram.model;
        console.log("Saving Workflow : ", $scope.workflow.name);
        if ($scope.workflow.name == "" || $scope.workflow.name == undefined) {
            $("#newWorkflowModal").modal("show");
        } else {
            graph = $scope.myDiagram.model.toJson();//.toJson();
            graphService.saveGraph(graph, $scope.workflow.name).then(
                function success(response) {
                    notify.showSuccess("Success!", response.data.message);
                },
                function error(response) {
                    notify.showError("Error!!", response.statusCode + " : " + response.status);
                }
            );
            $scope.myDiagram.isModified = false;
        }

    };
    $scope.loadWorkflow = function () {

        //alert($scope.workflow.name);

        graphService.loadGraph($scope.workflow.name).then(
            function success(response) {
                let graph = response.data.Graph.jgraph;
                $scope.myDiagram.model = go.Model.fromJson(graph);
                //console.log($scope.myDiagram.model.toJson());  //Check graph configuration
                //component= $scope.myDiagram.model.nodeDataArray.find(component => component.text!=null);
                //console.log(component);
                notify.showSuccess("Success!", "Workflow loaded Successfully.");
                $("#welcomepage").modal("hide");

            },
            function error(response) {
                notify.showError("Error!", "Workflow not found!!");

            }
        );

    };

    //---- welcome page----

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
        this.result = [];
        componentService.getAll().then(
            function success(response) {

                $scope.palleteModel = response.data.pallete;
                //alert(JSON.stringify($scope.palleteModel));
                this.result = response.data.pallete;

                $scope.myPalette.model.nodeDataArray = this.result;

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
    //Method for updating configuration of each component
    $scope.loadForm = function (componentName, componentKey) {
        $scope.model = {};
        if (componentName === "Mapper") //test method for mapper
        {
            //Finding previous node
            let previousKey = componentKey + 1;
            let nodeList = $scope.myDiagram.model.nodeDataArray;
            let previousNode = null;
            for (let i = 0; i < nodeList.length; i++) {
                if (nodeList[i].key == previousKey) {
                    previousNode = nodeList[i];
                    break;
                }
            }
            console.log(previousNode.output);
            componentService.getFormData("https://api.myjson.com/bins/mgvee").then(
                function success(response) {
                    $scope.schema = response.data.schema;
                    $scope.form = response.data.form;
                    let component = ($scope.getComponent(componentName));

                    $scope.model.field = [];
                    let obj = previousNode.output;
                    console.log(obj);

                    if (component.config) {//previousNode.isModified==false&&
                        $scope.model = component.config;
                        //notify.showInfo("Info","form config debug");

                    } else {
                        for (let key in obj) {
                            if (obj.hasOwnProperty(key)) {
                                let dataType = obj[key];

                                $scope.model.field.push({
                                    "check": false,
                                    "fieldName": dataType.fieldName,
                                    "dataType": dataType.dataType
                                });
                            }
                        }
                    }
                    //$scope.model.field =previousNode.output;
                    console.log($scope.model.field);
                    $('#myModal').modal('show');
                },
                function error(response) {
                    //Add notification show "Component not found"
                    $scope.schema = null;
                    $scope.form = null;
                    notify.showError("Error!", "Config is not available fot this component!");

                });
        } else
            componentService.getFormData("/WorkflowManager/getConfig/" + componentName).then(
                function success(response) {
                    $scope.schema = response.data.schema;
                    $scope.form = response.data.form;
                    let component = ($scope.getComponent(componentName));
                    if (!(component.config == null || $scope.getComponent(componentName).config == undefined)) {
                        $scope.model = component.config;
                    }
                    $('#myModal').modal('show');
                },
                function error(response) {
                    //Add notification show "Component not found"
                    $scope.schema = null;
                    $scope.form = null;
                    notify.showError("Error!", "Config is not available fot this component!");

                });
    };

    $scope.loadComponents(); //
    // print the diagram by opening a new window holding SVG images of the diagram contents for each page
    $scope.printDiagram = function () {
        let svgWindow = window.open();
        if (!svgWindow) return;  // failure to open a new Window
        let printSize = new go.Size(700, 960);
        let bnds = $scope.myDiagram.documentBounds;
        let x = bnds.x;
        let y = bnds.y;
        while (y < bnds.bottom) {
            while (x < bnds.right) {
                let svg = $scope.myDiagram.makeSVG({scale: 1.0, position: new go.Point(x, y), size: printSize});
                svgWindow.document.body.appendChild(svg);
                x += printSize.width;
            }
            x = bnds.x;
            y += printSize.height;
        }
        setTimeout(function () {
            svgWindow.print();
        }, 1);
    };


    /*******************************************************************
     *        Context Menu Handler
     *******************************************************************/

    // This is the general menu command handler, parameterized by the name of the command.
    $scope.cxcommand = function ($event, val) {
        if (val === undefined) val = $event.currentTarget.id;
        let diagram = $scope.myDiagram;
        switch (val) {
            /*		      case "cut": diagram.commandHandler.cutSelection(); break;
                  case "copy": diagram.commandHandler.copySelection(); break;
                  case "paste": diagram.commandHandler.pasteSelection(diagram.lastInput.documentPoint); break;
             */
            case "delete":
                diagram.commandHandler.deleteSelection();
                break;
            case "componentConfig":
                $scope.loadForm($scope.selectedComponent.category);
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
        let cxElement = document.getElementById("contextMenu");

        // Since we have only one main element, we don't have to declare a hide method,
        // we can set mainElement and GoJS will hide it automatically
        let myContextMenu = GO(go.HTMLInfo, {
            show: showContextMenu,
            mainElement: cxElement
        });

        $scope.myDiagram.contextMenu = myContextMenu;
        // We don't want the div acting as a context menu to have a (browser) context menu!
        cxElement.addEventListener("contextmenu", function (e) {

            e.preventDefault();
            return false;
        }, false);

        function showContextMenu(obj, diagram, tool) {
            // Show only the relevant buttons given the current state.
            let cmd = diagram.commandHandler;
            /*
                document.getElementById("cut").style.display = cmd.canCutSelection() ? "block" : "none";
                document.getElementById("copy").style.display = cmd.canCopySelection() ? "block" : "none";
                document.getElementById("paste").style.display = cmd.canPasteSelection() ? "block" : "none";
             */
            document.getElementById("delete").style.display = cmd.canDeleteSelection() ? "block" : "none";
            document.getElementById("componentConfig").style.display = (obj !== null ? "block" : "none");
            if (obj instanceof go.Node) {
                //Setting selected component for further processing
                $scope.selectedComponent.category = obj.data.category;
                $scope.selectedComponent.key = obj.data.key;

            }

            // Now show the whole context menu element
            cxElement.style.display = "block";
            // we don't bother overriding positionContextMenu, we just do it here:
            let mousePt = diagram.lastInput.viewPoint;
            cxElement.style.left = mousePt.x + "px";
            cxElement.style.top = mousePt.y + "px";
        }

//		--		  Context  Menu Initialization End -----------------------------

        $scope.myDiagram.addModelChangedListener(function (e) {
                // var cmdhandler= MD.commandHandler;
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
            }
        );

        // when the document is modified, add a "*" to the title and enable the "Save" button
        $scope.myDiagram.addDiagramListener("Modified", function (e) {
            let button = document.getElementById("SaveButton");
            if (button) button.disabled = !$scope.myDiagram.isModified;
            let idx = document.title.indexOf("*");
            if ($scope.myDiagram.isModified) {
                if (idx < 0) document.title += "*";
            } else {
                if (idx >= 0) document.title = document.title.substr(0, idx);
            }
            //alert(JSON.stringify($scope.myDiagram.model.nodeDataArray[1].config));
        });

        //Listener when diagram component is changed
        $scope.myDiagram.addDiagramListener("ObjectDoubleClicked", function (e) {
                let part = e.subject.part;
                let componentName;
                if (!(part instanceof go.Link) && !(part.data.category === "Start" || part.data.category == "End")) {
                    // console.log(JSON.stringify(part.data.formData));
                    componentName = part.data.text;

                    $scope.selectedComponent.category = componentName;
                    $scope.selectedComponent.key = part.data.key;
                    $scope.loadForm(componentName, part.data.key);


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
            let horizontal = align.equals(go.Spot.Top) || align.equals(go.Spot.Bottom);
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

        $scope.myDiagram.nodeTemplateMap.add("CsvReader",  // the default category
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
                        new go.Binding("text").makeTwoWay())
                ),
                {contextMenu: myContextMenu},
                // four named ports, one on each side:
                makePort("T", go.Spot.Top, go.Spot.TopSide, false, true),
                makePort("L", go.Spot.Left, go.Spot.LeftSide, true, true),
                makePort("R", go.Spot.Right, go.Spot.RightSide, true, true),
                makePort("B", go.Spot.Bottom, go.Spot.BottomSide, true, false)
            ));

        $scope.myDiagram.nodeTemplateMap.add("MongoWriter",  // the default category
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
                        new go.Binding("text").makeTwoWay())
                ),
                {contextMenu: myContextMenu},
                // four named ports, one on each side:
                makePort("T", go.Spot.Top, go.Spot.TopSide, false, true),
                makePort("L", go.Spot.Left, go.Spot.LeftSide, true, true),
                makePort("R", go.Spot.Right, go.Spot.RightSide, true, true),
                makePort("B", go.Spot.Bottom, go.Spot.BottomSide, true, false)
            ));

        $scope.myDiagram.nodeTemplateMap.add("Mapper",  // the default category
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
                        new go.Binding("text").makeTwoWay())
                ),
                {contextMenu: myContextMenu},
                // four named ports, one on each side:
                makePort("T", go.Spot.Top, go.Spot.TopSide, false, true),
                makePort("L", go.Spot.Left, go.Spot.LeftSide, true, true),
                makePort("R", go.Spot.Right, go.Spot.RightSide, true, true),
                makePort("B", go.Spot.Bottom, go.Spot.BottomSide, true, false)
            ));


        $scope.myDiagram.nodeTemplateMap.add("Start",
            GO(go.Node, "Table", nodeStyle(),
                GO(go.Panel, "Auto",
                    GO(go.Shape, "Circle",
                        {minSize: new go.Size(40, 40), fill: "#79C900", strokeWidth: 0}),
                    GO(go.TextBlock, "Start", textStyle(),
                        new go.Binding("text"))
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
                        new go.Binding("text"))
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
                        new go.Binding("text").makeTwoWay())
                )
            );

        // Make link labels visible if coming out of a "conditional" node.
        // This listener is called by the "LinkDrawn" and "LinkRelinked" DiagramEvents.
        function showLinkLabel(e) {
            let label = e.subject.findObject("LABEL");
            if (label !== null) label.visible = (e.subject.fromNode.data.figure === "Diamond");
        }

        // temporary links used by LinkingTool and RelinkingTool are also orthogonal:
        $scope.myDiagram.toolManager.linkingTool.temporaryLink.routing = go.Link.Orthogonal;
        $scope.myDiagram.toolManager.relinkingTool.temporaryLink.routing = go.Link.Orthogonal;

        //$scope.loadWorkflow();  // load an initial diagram from some JSON text

        // initialize the Palette that is on the left side of the page
        $scope.myPalette =
            GO(go.Palette, "myPaletteDiv",  // must name or refer to the DIV HTML element
                {
                    scrollsPageOnFocus: false,
                    nodeTemplateMap: $scope.myDiagram.nodeTemplateMap  // share the templates used by $scope.myDiagram


                });

        // $scope.myPalette.model.nodeDataArray=$scope.loadComponents();
        // console.log($scope.loadComponents());
        console.log("load main welcome dialogue");
        //$("#welcomepage").modal("show");
        $scope.handlePreviousRequest();

    }; // end init

}]);


//prettify filter for displaying json on screen
app.filter('prettify', function () {

    function syntaxHighlight(json) {
        //console.log(json);
        json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
        return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
            let cls = 'number';
            if (/^"/.test(match)) {
                if (/:$/.test(match)) {
                    cls = 'key';
                } else {
                    cls = 'string';
                }
            } else if (/true|false/.test(match)) {
                cls = 'boolean';
            } else if (/null/.test(match)) {
                cls = 'null';
            }
            return '<span class="' + cls + '">' + match + '</span>';
        });
    }

    return syntaxHighlight;
});