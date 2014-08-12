angular.module('mooseheadModule')
    .controller('WorkshopListCtrl', ['$scope', '$http','workshopFactory',
        function($scope, $http,workshopFactory) {
            $scope.workshops = [];
            workshopFactory.then(function(value) {
                $scope.workshops = value;
            }
            );

            $scope.workshopOpen = function(workshop) {
                return (workshop.status === "FREE_SPOTS" || workshop.status === "FEW_SPOTS" || workshop.status === "FULL");
            }
        }]);

