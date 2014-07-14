(function() {

    angular.module('mooseheadModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('moosehead', ['mooseheadModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider
                    .when('/', {
                        templateUrl: 'templates/workshopList.html',
                        controller: 'WorkshopListCtrl'
                    })
                    .when("/register/:workshopid", {
                        templateUrl: 'templates/register.html',
                        controller: 'RegisterCtrl'
                    }).when("/cancel/:workshopid", {
                        templateUrl: 'templates/cancel.html',
                        controller: 'CancelCtrl'
                    })
                    ;
        }]);
        
        angular.bootstrap(document,['moosehead']);
        
    };

    bootstrap();


}());
