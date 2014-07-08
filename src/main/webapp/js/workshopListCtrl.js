angular.module('mooseheadModule')
    .controller('WorkshopListCtrl', ['$scope', '$http',
        function($scope, $http) {
            $scope.workshops = [];
            $http({method: "GET", url: "data/workshopList"})
                .success(function(workshopList) {
                    $scope.workshops = workshopList;
                });
        }]);

