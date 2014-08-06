angular.module('mooseheadModule')
    .controller('AdminCtrl', ['$scope', '$http','workshopFactory',
        function($scope, $http,workshopFactory) {
            $scope.workshops = [];
            workshopFactory.then(function(value) {
                    $scope.workshops = value;
                }
            );
        }]);

