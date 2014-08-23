angular.module('mooseheadModule')
    .controller('ListWorkshopCtrl', ['$scope', '$http','$routeParams',
        function($scope, $http, $routeParams) {
            $scope.title = "Noe her";
            $scope.showData = false;
            $scope.message = "Please wait...";
            $scope.showMessage = true;
            $scope.participants = [];



            $http({method: "GET", url: "data/teacherList?workshop=" + $routeParams.token})
                .success(function(data) {
                    $scope.title = data.title;
                    $scope.participants = data.participants;
                    $scope.showMessage = false;
                    $scope.showData = true;
                });
        }]);

