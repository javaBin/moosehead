angular.module('mooseheadModule')
    .controller('AdminCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.workshops = [];
            $http({method: "GET", url: "data/alldata"})
                .success(function(value) {
                    $scope.workshops = value;
                });
            $scope.isConfirmed = function(participant) {
                if (participant.isEmailConfirmed) {
                    return "Confirmed";
                }
                return "Not confirmed";
            }
        }]);

