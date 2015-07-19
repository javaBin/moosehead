angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory',
        function($scope, $http,$routeParams,workshopFactory) {
            $scope.showForm = true;
            $scope.showMessage = false;
            $scope.captchasrc = "captcha/img?cb=" + Date.now();
            $scope.workshopid = $routeParams.workshopid;
            $scope.showVeryFull = false;
            $scope.userObject = {};

            $http({method: "GET", url: "data/userLogin"})
                .success(function(userobj) {
                    $scope.userObject = userobj;
                });

            workshopFactory.then(function(workshopList) {
               $scope.workshop = _.find(workshopList,function(w) {
                  return w.id === $scope.workshopid;
               });
                if (!$scope.workshop) {
                    $scope.message = "This workshop is unknown";
                    $scope.showForm = false;
                    $scope.showMessage = true;
                } else if ($scope.workshop.status === "NOT_OPENED") {
                    $scope.message = "Registration has not opened yet. Come back later.";
                    $scope.showForm = false;
                    $scope.showMessage = true;
                } else if ($scope.workshop.status === "CLOSED") {
                    $scope.message = "Registrations for this workshop is now closed";
                    $scope.showForm = false;
                    $scope.showMessage = true;
                } else if ($scope.workshop.status === "VERY_FULL") {
                    $scope.showVeryFull = true;
                }
            });

            $scope.reloadCaptcha = function() {
                $scope.captchasrc = "captcha/img?cb=" + Date.now();
            };

            $scope.fetchGoogleData = function() {
                if (!$scope.userObject.id) {
                    return;
                }
                $scope.email = $scope.userObject.email;
                $scope.fullname = $scope.userObject.name;
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
                        $scope.message = "Registration registered. You will receive an email confirmation.";
                        $scope.showForm = false;
                    } else if (data.status === "CONFIRM_EMAIL") {
                        $scope.message = "Registration registered. You will receive an email with a link you must click to confirm your registration";
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

