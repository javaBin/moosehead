(function() {

    angular.module('mooseheadModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('moosehead', ['mooseheadModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider
                    .when('/', {
                        templateUrl: 'templates/admin.html',
                        controller: 'AdminCtrl'
                    })
                    .when('/addWorkshop', {
                        templateUrl: 'templates/addWorkshop.html',
                        controller: 'AddWorkshopCtrl'
                    })
                ;
        }]);
        
        angular.bootstrap(document,['moosehead']);
        
    };

    bootstrap();


}());
