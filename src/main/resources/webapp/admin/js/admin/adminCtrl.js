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
            };

            $scope.queNum = function(workshop,participant) {
                var pos = workshop.participants.indexOf(participant);
                return pos+1;
            };
        }]);

