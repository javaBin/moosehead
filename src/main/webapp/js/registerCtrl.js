angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory',
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

            $scope.register = function() {
                $scope.showMessage = true;
                if (!$scope.fullname || $scope.fullname == "") {
                    $scope.message = "Please enter your name";
                    return;
                }
                if (!$scope.email || $scope.email == "") {
                    $scope.message = "Please enter a valid email";
                    return;
                }
                $scope.message = "Please wait...";
                var postData = {
                    workshopid: $scope.workshopid,
                    email: $scope.email,
                    fullname: $scope.fullname,
                    captcha: $scope.captcha
                };
                $http({
                    method: "POST",
                    url: "data/reserve",
                    data: postData
                }).success(function(data) {
                    if (data.status === "OK") {
                        $scope.message = "Registration registered. You will receive an email to confirm your registration";
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

