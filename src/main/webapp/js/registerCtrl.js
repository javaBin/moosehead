angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams',
        function($scope, $http,$routeParams) {
            $scope.workshopid = $routeParams.workshopid;
        }]);

