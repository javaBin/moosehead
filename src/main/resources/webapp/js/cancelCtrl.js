angular.module('mooseheadModule')
    .controller('CancelCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {
            $scope.showMessage = false;
            $scope.showButton = true;

            $scope.doCancel = function() {
                $scope.showButton = false;
                $scope.showMessage = true;
                $scope.message = "Please wait..."

                var postData = {
                    token: $routeParams.token
                };
                $http({
                    method: "POST",
                    url: "data/cancel",
                    data: postData
                }).success(function (data) {
                    if (data.status === "OK") {
                        $scope.message = "Cancellation registered. You will receive an email to confirm your cancellation";
                        $scope.showForm = false;
                    } else {
                        $scope.message = data.message;
                    }
                }).error(function (data, status, headers, config) {
                    $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
                });
            }

        }]);

