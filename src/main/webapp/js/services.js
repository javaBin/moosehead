angular.module('mooseheadModule')
    .factory("workshopFactory",["$http","$q", function($http,$q) {
        var deferred = $q.defer();

        $http({method: "GET", url: "data/workshopList"})
            .success(function(workshopList) {
                deferred.resolve(workshopList);
            });

        return deferred.promise;
    }]);

