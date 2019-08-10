angular.module('mooseheadModule')
    .controller('RegisterCtrl', ['$scope', '$http','$routeParams','workshopFactory','$location',
        function($scope, $http,$routeParams,workshopFactory,$location) {
            $scope.showForm = true;
            $scope.showMessage = false;
            $scope.captchasrc = "captcha/img?cb=" + Date.now();
            $scope.workshopid = $routeParams.workshopid;
            $scope.showVeryFull = false;
            $scope.userObject = {};
            $scope.loggedIn = false;
            $scope.showNotConfirmed = false;
            $scope.showWorkshopFull = false;
            $scope.numOnWaitingList = 0;

            $http({method: "GET", url: "data/userLogin"})
                .success(function(userobj) {
                    $scope.userObject = userobj;
                    if ($scope.userObject && $scope.userObject.id) {
                        $scope.email = $scope.userObject.email;
                        $scope.fullname = $scope.userObject.name;
                        $scope.loggedIn = true;
                    }
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
                    $scope.message = "Registration opens " + $scope.workshop.opensAt;
                    $scope.showForm = false;
                    $scope.showMessage = true;
                } else if ($scope.workshop.status === "CLOSED") {
                    $scope.message = "Registrations for this workshop is now closed";
                    $scope.showForm = false;
                    $scope.showMessage = true;
                } else if ($scope.workshop.status === "VERY_FULL") {
                    $scope.showVeryFull = true;
                }
                $scope.showMultiReservations = $scope.workshop.maxReservations > 1;
                if ($scope.workshop.onWaitingList >= 0) {
                    $scope.numOnWaitingList = $scope.workshop.onWaitingList;
                    $scope.showWorkshopFull = true;
                }
            });

            $scope.reloadCaptcha = function() {
                $scope.captchasrc = "captcha/img?cb=" + Date.now();
            };

            $scope.googleLogin = function() {
                var absloc = $location.absUrl();
                var ind=absloc.indexOf("#");
                var stratpart = absloc.substr(0,ind);
                var newloc = stratpart + "oauth2callback/login?sendMeTo=" + encodeURIComponent(absloc);
                window.location.href = newloc; // how do I do this with angular?
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
                var numReservations = "1";
                var orderedReservations = $scope.numReservations;
                if (_.isString(orderedReservations)) {
                    numReservations = orderedReservations;
                }
                if (_.isNumber(orderedReservations)) {
                    numReservations = "" + orderedReservations;
                }
                var postData = {
                    workshopid: $scope.workshopid,
                    email: $scope.email,
                    fullname: $scope.fullname,
                    captcha: $scope.captcha,
                    numReservations: numReservations,
                    additionalInfo: {
                        tshirts: $scope.shirts
                    }

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
                        $scope.message = "Registration registered. You will receive an email with a link you must click to confirm your registration.";
                        $scope.showForm = false;
                        $scope.showNotConfirmed = true;
                    } else if (data.status === "WRONG_CAPTCHA") {
                        $scope.message ="Captcha was wrong. Please try again";
                    } else {
                        $scope.message = data.message;
                    }

                }).error(function(data, status, headers, config) {
                    $scope.message = "Some unknown error occured. Error: " + data + " status: " + status;
                });

            }

            $scope.shirts = [];
            $scope.numResChanged = function() {
                console.log("xxx");
                $scope.shirts=_.map(_.range(0,$scope.numReservations),function(d) {
                    return {
                        size: "small"
                    };
                });
            };
        }]);

