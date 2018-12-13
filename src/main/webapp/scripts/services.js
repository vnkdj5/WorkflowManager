app.service("graphService", function ($http) {
    //graph create service
    this.newGraph = function (name) {
        data = {
            "name": name
        };
        return $http({
            method: "POST",
            url: "create",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }
        });

    };

    //graph save service
    this.saveGraph = function (ngraph, name) {
        data = {
            name: name,
            jgraph: JSON.parse(ngraph)
        }
        return $http({
            method: "POST",
            url: "save",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }
        });
    };

    this.getAll = function () {
        return $http.get("/WorkflowManager/components");
    };

    this.loadGraph = function (name) {
        data = {
            "name": name
        };

        console.log(data);
        return $http({
            method: "POST",
            url: "open",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }
        });
    };

    this.runWorkflow = function (name) {
        data = {
            "name": name
        };
        return $http({
            method: "POST",
            url: "run",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }
        });
    };

    this.checkDBConnection = function (data) {
        return $http({
            method: "POST",
            url: "checkConnection",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }

        });
    };


    this.getValidLinks = function () {
        return $http.get("/WorkflowManager/getValidLinks");
    };

    this.getAllWorkflows = function () {
        return $http({
            url: "getAll",
            method: "GET"
        });

    };

    this.deleteWorkflow = function (name) {

        return $http({
            url: "delete/" + name,
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            }
        });
    }
});

//================== Component Service
app.service("componentService", function ($http) {
    this.getAll = function () {
        return $http.get("/WorkflowManager/components");
    };

    this.getFormData = function (formName) {
        return $http.get(formName);
    }


});


/****************************************************
 Notification Service
 *****************************************************/
app.service("notificationService", ['growl', function (growl) {
    this.showError = function (messageTitle, message) {
        growl.error(message, {title: messageTitle, ttl: 10000});
    }
    this.showSuccess = function (messageTitle, message) {
        growl.success(message, {title: messageTitle, ttl: 5000});
    }

    this.showInfo = function (messageTitle, message) {
        growl.info(message, {title: messageTitle, ttl: 5000});
    }

    this.showWarning = function (messageTitle, message) {
        growl.warning(message, {title: messageTitle, ttl: 5000});
    }
}]);


//TODO: Remove below code and check usability
app.service('fileUpload', ['$q', '$http', function ($q, $http) {
    var deffered = $q.defer();
    var responseData;
    this.uploadFileToUrl = function (file, uploadUrl) {
        var fd = new FormData();
        fd.append('file', file);
        return $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).then(
            function success(response) {
                /* $scope.errors = response.data.value; */
                // console.log(response);
                responseData = response.data;
                deffered.resolve(response);
                return deffered.promise;
            },
            function onError(error) {
                deffered.reject(error);
                return deffered.promise;
            });
    };
    this.getResponse = function () {
        return responseData;
    }
    this.deleteFile=function(fileUrl){
    	let data={
    		"file":fileUrl	
    	};
    	return $http({
            method: "POST",
            url: "deletefile",
            data: angular.toJson(data),
            headers: {
                'Content-Type': 'application/json'
            }

        });
    }
}]);


