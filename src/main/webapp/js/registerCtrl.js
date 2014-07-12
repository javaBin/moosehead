angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {
            $scope.workshopid = $routeParams.workshopid;
            workshopFactory.then(function(workshopList) {
               $scope.workshop = _.find(workshopList,function(w) {
                  return w.id === $scope.workshopid;
               });
            });
        }]);

