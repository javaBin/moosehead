angular.module('mooseheadModule')
    .controller('AddWorkshopCtrl', ['$scope', '$http',
        function($scope, $http) {
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
                });
        }]);


