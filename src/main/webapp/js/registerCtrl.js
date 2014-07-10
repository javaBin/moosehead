angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {

            $scope.workshopid = $routeParams.workshopid;
        }]);

