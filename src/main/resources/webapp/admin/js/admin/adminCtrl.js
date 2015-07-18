angular.module('mooseheadModule')
    .controller('AdminCtrl', ['$scope', '$http','$location',
        function($scope, $http, $location) {
            $scope.workshops = [];
            $scope.showNoAccess=false;
            $scope.needLogin = false;
            $scope.needAccess = false;
            $http({method: "GET", url: "data/userLogin"})
                .success(function(userobj) {
                    if (!(userobj && userobj.id)) {
                        $scope.showNoAccess = true;
                        $scope.needLogin = true;
                        return;
                    }
                    if (!userobj.admin) {
                        $scope.showNoAccess = true;
                        $scope.needAccess = true;
                        $scope.googleid = userobj.id;
                        return;

                    }
                    $http({method: "GET", url: "data/alldata"})
                        .success(function(value) {
                            $scope.workshops = value;
                        });
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

