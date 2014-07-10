angular.module('mooseheadModule')
    .controller('WorkshopListCtrl', ['$scope', '$http','workshopFactory',
        function($scope, $http,workshopFactory) {
            workshopFactory.then(function(value) {
                $scope.dummy = value;
            }
            );
            $scope.workshops = [];
            $http({method: "GET", url: "data/workshopList"})
                .success(function(workshopList) {
                    $scope.workshops = workshopList;
                });
            $scope.workshopOpen = function(workshop) {
                return (workshop.status === "FREE_SPOTS" || workshop.status === "FEW_SPOTS");
            }
        }]);

