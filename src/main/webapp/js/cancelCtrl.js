angular.module('mooseheadModule')
    .controller('CancelCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {
            $scope.showForm = true;
            $scope.showMessage = false;
            $scope.captchasrc = "captcha/img?cb=" + Date.now();
            $scope.workshopid = $routeParams.workshopid;

            workshopFactory.then(function(workshopList) {
                $scope.workshop = _.find(workshopList,function(w) {
                    return w.id === $scope.workshopid;
                });
            });

            $scope.reloadCaptcha = function() {
                $scope.captchasrc = "captcha/img?cb=" + Date.now();
            };
            $scope.cancel = function() {
                $scope.showMessage = true;

                if (!$scope.email || $scope.email == "") {
                    $scope.message = "Please enter a valid email";
                    return;
                }
                $scope.message = "Please wait...";
                var postData = {
                    workshopid: $scope.workshopid,
                    email: $scope.email,
                    captcha: $scope.captcha
                };
                $http({
                    method: "POST",
                    url: "data/cancel",
                    data: postData
                }).success(function(data) {
                    if (data.status === "OK") {
                        $scope.message = "Cancellation registered. You will receive an email to confirm your cancellation";
                        $scope.showForm = false;
                    } else if (data.status === "WRONG_CAPTCHA") {
                        $scope.message ="Captcha was wrong. Please try again";
                    } else {
                        $scope.message = data.message;
                    }

                }).error(function(data, status, headers, config) {
                    $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
                });
            }
        }]);

