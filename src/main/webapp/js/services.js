angular.module('mooseheadModule')
    .factory("workshopFactory",["$http","$q", "$rootScope", function($http,$q,$rootScope) {
        var deferred = $q.defer();

        setTimeout(function() {
            $rootScope.$apply(function() {
                deferred.resolve("Hi " + new Date());
            });
        }, 2000);

        return deferred.promise;
    }]);

