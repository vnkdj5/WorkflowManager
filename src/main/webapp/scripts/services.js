app.service("graphService", function($http){
	//graph create service
	this.newGraph=function(name){
		data={
				"name":name
		};
		return $http({
			method: "POST",
			url: "create",
			data: angular.toJson(data),
			headers : {
	            'Content-Type' : 'application/json'
	        }
		});
		
	};
	
	//graph save service
	this.saveGraph=function(ngraph,name){
		data={
				name:name,
				jgraph:JSON.parse(ngraph)
		}
		$http({
			method: "POST",
			url: "save",
			data: angular.toJson(data),
			headers : {
	            'Content-Type' : 'application/json'
	        }
		}).then(_success,_error);
		function _success(response){
			return "Saved!";
		};
		function _error(response){
			return "Error";
		};
	};
	
	this.getAll=function(){
		return $http.get("/WorkflowManager/components");
	};
	
	this.loadGraph = function(name){
		data={
				"name":name
		};
		
		console.log(data);
		return $http({
			method: "POST",
			url: "open",
			data: angular.toJson(data),
			headers : {
	            'Content-Type' : 'application/json'
	        }
		});
	};
	
	this.runWorkflow = function(name){
		data = {
			"name":name	
		};
		return $http({
			method:"POST",
			url:"run",
			data: angular.toJson(data),
			headers : {
				'Content-Type' : 'application/json'
			}
		});
	};
	
	this.checkDBConnection = function(data){
		return $http({
			method: "POST",
			url:"checkConnection",
			data:angular.toJson(data),
			headers : {
				'Content-Type' : 'application/json'
			}
	
		});
	};
	
	
	this.getValidLinks=function(){
		return $http.get("/WorkflowManager/getValidLinks");
	};
	
	this.getAllWorkflows = function(){
		return $http({
			url:"getAll",
			method:"GET"
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


/****************************************************
	Notification Service
*****************************************************/
app.service("notificationService",['growl',function(growl)
{
	this.showError = function(messageTitle,message){
	    growl.error(message,{title: messageTitle,ttl:10000});
	}
	this.showSuccess = function(messageTitle,message){
	    growl.success(message,{title: messageTitle,ttl:5000});
	}
	
	this.showInfo = function(messageTitle,message){
	    growl.info(message,{title: messageTitle,ttl:5000});
	}
	
	this.showWarning = function(messageTitle,message){
	    growl.warning(message,{title: messageTitle,ttl:5000});
	}
}]);




