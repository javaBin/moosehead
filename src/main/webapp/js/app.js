(function() {

    angular.module('mooseheadModule', []);

    var bootstrap;
    bootstrap = function() {
        angular.module('moosehead', ['mooseheadModule']).
        config(['$routeProvider', function($routeProvider) {
                $routeProvider.
                    when('/', {
                        templateUrl: 'templates/workshopList.html',
                        controller: 'WorkshopListCtrl'
                    })
                    ;
        }]);
        
        angular.bootstrap(document,['moosehead']);
        
    };

    bootstrap();


}());
