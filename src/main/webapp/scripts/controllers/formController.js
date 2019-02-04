
app.controller('FormController',['$scope', '$q','$http', function($scope,$q,$http, $location) {
	$scope.schema=null;
	$scope.form=[];

	/*[
    "*",
    {
      type: "submit",
      title: "Save"
    }
  ];*/
	$scope.loadData= function(formName){
		$http.get(formName)
		.then(function onSuccess(response){
			$scope.schema=response.data.schema;
			$scope.form=response.data.form;
		},
		function onError(response){
			alert(response.status+"")
		}
		)
	};
	
	$scope.loadData("https://api.myjson.com/bins/6wfpe");
	$scope.onSubmit = function(form) {
		// First we broadcast an event so all fields validate themselves
		$scope.$broadcast('schemaFormValidate');

		// Then we check if the form is valid
		if (form.$valid) {
			alert("submitted");
		}
	}
	$scope.model = {};
	
}]);

