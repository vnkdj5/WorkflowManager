var app=angular.module('WorkflowManager', ['schemaForm','pascalprecht.translate','angular-growl','ngSchemaFormFile']);
app.service('fileUpload', ['$q','$http', function ($q,$http) {
	 var deffered = $q.defer();
	 var responseData;
	 this.uploadFileToUrl = function(file, uploadUrl){
	 var fd = new FormData();
	 fd.append('file', file);
	 return $http.post(uploadUrl, fd, {
	 transformRequest: angular.identity,
	 headers: { 'Content-Type' : undefined}
	 }).then(
	 function success(response){
	/* $scope.errors = response.data.value; */
	  // console.log(response);
	   responseData = response.data;
	   deffered.resolve(response);
	   return deffered.promise;
	   },
	   function onError(error){
	   deffered.reject(error);
	   return deffered.promise;
	   });
	};
	this.getResponse = function() {
	 return responseData;
	 }
	}]);

app.service("componentService", function($http){
	this.getAll=function(){
		return $http.get("/WorkflowManager/components");
	};
	
	this.getFormData=function(formName)
	{
		return $http.get(formName);
	}
	
	
});