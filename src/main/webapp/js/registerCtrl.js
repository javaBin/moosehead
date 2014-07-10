angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {
            workshopFactory.then(function(workshopList) {
               $scope.dummy = workshopList.length;
            });
            $scope.workshopid = $routeParams.workshopid;
        }]);

