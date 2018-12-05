var app=angular.module('WorkflowManager', ['schemaForm','pascalprecht.translate','angular-growl','ngSchemaFormFile']);

app.config(['growlProvider', function (growlProvider) {
    growlProvider.globalDisableCountDown(true);
    growlProvider.globalPosition('bottom-right');
}]);

