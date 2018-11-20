var app = angular.module('myApp',[]);
app.service("welcomeService", function($http){

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
app.filter('beginning_data', function() {
    return function(input, begin) {
        if (input) {
            begin = +begin;
            return input.slice(begin);
        }
        return [];
    }
});
app.controller('controller', function($scope, $http, $timeout,welcomeService) {

	$scope.deleteWf = function(index,name)
	{
		welcomeService.deleteWorkflow(name).then(
				function success(response){
					$scope.file.splice(index,1);
					notify.showSuccess("Success!", "Workflow deleted Successsfully.");
				},function error(response){
					notify.showError("Error while deleting workflow!");
				}
		);
	}

	$scope.createWorkflow = function(){
		console.log("workflow creaate");
		window.location.href = "/WorkflowManager/index.html?create=true"
	};

	$scope.loadWorkflow = function(name){
		console.log("workflow creaate");
		window.location.href = "/WorkflowManager/index.html?load=true&name="+name;
	};
    $http.get('/WorkflowManager/getAll').then(function(user_data) {
        $scope.file = user_data.data;
        $scope.data_limit = 10;
        $scope.filter_data = $scope.file.length;
        $scope.entire_user = $scope.file.length;
    }
    	,function error(response){
			notify.showError("Error while deleting workflow!");
		}
    );

    $scope.filter = function() {
        $timeout(function() {
            $scope.filter_data = $scope.searched.length;
        }, 20);
    };
    $scope.sort_with = function(base) {
        $scope.base = base;
        $scope.reverse = !$scope.reverse;
    };
});
