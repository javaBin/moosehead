angular.module('mooseheadModule')
    .controller('ConfirmEmailCtrl', ['$scope', '$http','$routeParams',
        function($scope, $http,$routeParams) {
            $scope.message = "Please wait...";
            var postData = {
                token: $routeParams.token
            };
            $http({
                method: "POST",
                url: "data/confirmEmail",
                data: postData
            }).success(function (data) {
                if (data.status === "OK") {
                    if (data.message && data.message !== null) {
                        $scope.message = data.message;
                    } else {
                        $scope.message = "Your email address has been confirmed";
                    }

                } else {
                    $scope.message = data.message;
                }
            }).error(function (data, status, headers, config) {
                $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
            });
        }]);

