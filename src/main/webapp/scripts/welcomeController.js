var app = angular.module('myApp', ['datatables', 'datatables.buttons', 'angular-growl']);
app.config(['growlProvider', function (growlProvider) {
    growlProvider.globalDisableCountDown(true);
    growlProvider.globalPosition('bottom-right');
}]);

app.service("welcomeService", function($http){
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

this.deleteWorkflow = function(name){

		return $http({
			url:"delete/"+name,
			method:"POST",
			headers : {
	            'Content-Type' : 'application/json'
	        }
		});
	}
});
app.service("notificationService", ['growl', function (growl) {
    this.showError = function (messageTitle, message) {
        growl.error(message, {title: messageTitle, ttl: 10000});
    };
    this.showSuccess = function (messageTitle, message) {
        growl.success(message, {title: messageTitle, ttl: 5000});
    };

    this.showInfo = function (messageTitle, message) {
        growl.info(message, {title: messageTitle, ttl: 5000});
    };

    this.showWarning = function (messageTitle, message) {
        growl.warning(message, {title: messageTitle, ttl: 5000});
    }
}]);

app.filter('beginning_data', function() {
    return function(input, begin) {
        if (input) {
            begin = +begin;
            return input.slice(begin);
        }
        return [];
    }
});
app.filter('prettyDate', function () {
    return function (input) {
        if (input) {
            var date = new Date(input);

            return date.toLocaleString();
        }
        return [];
    }
});
app.controller('controller', ['$scope', '$http', '$timeout', 'welcomeService', 'DTOptionsBuilder', 'notificationService', function ($scope, $http, $timeout, welcomeService, DTOptionsBuilder, notify) {
	$scope.workflow={};
    $scope.dtOptions = DTOptionsBuilder.newOptions()
        .withDisplayLength(5)
        .withOption('bLengthChange', false)
        .withButtons([{
            text: 'Create Workflow',
            key: '1',
            action: function (e, dt, node, config) {
                $scope.createWorkflowPopup()
            }
        }]);
	$scope.deleteWf = function(index,name)
	{
		welcomeService.deleteWorkflow(name).then(
				function success(response){
					$scope.file.splice(index,1);
					notify.showSuccess("Success!", "Workflow deleted Successsfully.");
				},function error(response){
                notify.showError("Error!!", "Error while deleting workflow!");
				}
		);
	};
	$scope.createWorkflow = function(){

        welcomeService.newGraph($scope.workflow.name).then(
            function success(response) {
                console.log(response.data);
                window.location.href = "index.html?load=1&name=" + response.data.Graph.id;
            }, function error(response) {
                alert("Workflow exists with the name");
            }
        );

	};
	$scope.createWorkflowPopup = function(){
		console.log("workflow creaate");
		$("#newWorkflowModal").modal("show");
		
	};

	$scope.loadWorkflow = function(name){
		console.log("workflow creaate");
        window.location.href = "index.html?load=1&name=" + name;
	};
    //TODO: Refactoring needed below
    $http.get('/WorkflowManager/getAll').then(function success(user_data) {
        $scope.file = user_data.data;
        $scope.data_limit = 10;
        $scope.filter_data = $scope.file.length;
        $scope.entire_user = $scope.file.length;
            notify.showSuccess("Success", "Workflows list loaded.");
    }

    	,function error(response){
            notify.showError("Error!", "Error while loading workflows!");
		}
    );

    $scope.filter = function() {
        $timeout(function() {
            $scope.filter_data = $scope.searched.length;
        }, 20);
    };


}]);
