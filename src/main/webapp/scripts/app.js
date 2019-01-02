var app=angular.module('WorkflowManager', ['schemaForm','pascalprecht.translate','angular-growl','ngSchemaFormFile']);

app.config(['growlProvider', function (growlProvider) {
    growlProvider.globalDisableCountDown(true);
    growlProvider.globalPosition('bottom-right');
}]);

//For Checking Internet connection of the User
app.run(function ($window, $rootScope) {
    $rootScope.online = navigator.onLine;
    $window.addEventListener("offline", function () {
        $rootScope.$apply(function () {
            $rootScope.online = false;
        });
    }, false);
    $window.addEventListener("online", function () {
        $rootScope.$apply(function () {
            $rootScope.online = true;
        });
    }, false);
});

app.config(['$translateProvider', function ($translateProvider) {
    $translateProvider.translations('en', {});
    $translateProvider.preferredLanguage('en');
    // Using standard escaping (nothing)
    $translateProvider.useSanitizeValueStrategy('escape')
}]);