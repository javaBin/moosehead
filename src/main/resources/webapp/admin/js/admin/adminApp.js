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
                ;
        }]);
        
        angular.bootstrap(document,['moosehead']);
        
    };

    bootstrap();


}());
