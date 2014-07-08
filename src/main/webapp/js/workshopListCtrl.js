angular.module('mooseheadModule')
    .controller('WorkshopListCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.workshops = [];
            $http({method: "GET", url: "data/workshopList"})
                .success(function(workshopList) {
                    $scope.workshops = workshopList;
                });
            $scope.workshopOpen = function(workshop) {
                return (workshop.status === "FREE_SPOTS" || workshop.status === "FEW_SPOTS");
            }
        }]);

